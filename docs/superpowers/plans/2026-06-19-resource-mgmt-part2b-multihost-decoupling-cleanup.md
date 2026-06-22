# Resource Management Part 2b — MultiHost Storage + NodeService Decoupling + Cluster Cleanup

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persist MultiHost/cluster CapabilityServices in the database, decouple the CapabilityService model classes from Consul `NodeService`, remove the remaining capability Consul clients, and reactivate the cluster read paths from the DB — leaving the AWX cluster-provisioning orchestration logic itself in place.

**Architecture:** Reuse the flat `CapabilityServiceEntity` (`serviceClass = MULTI_HOST`, JSON `memberMapping`). A `MultiHostCapabilityServicePersistence` DB component replaces `MultiHostCapabilitiesConsulClient`'s storage. The model classes (`CapabilityService`, `SingleHostCapabilityService`, `MultiHostCapabilityService`) stop extending `NodeService` and keep `serviceName`/`tags`/`meta`/`port`/`serviceId`/`memberMapping` as plain members. Cluster handlers keep their AWX/Vault orchestration but route storage to the DB component and access control to `AccessControlService`. `getClusters`/`getClusterMembers` resolve members from `memberMapping` resource IDs via `ResourceJpaRepository`.

**Tech Stack:** Java 17 + Kotlin, Spring Boot, Spring Data JPA, MariaDB (Testcontainers in tests), JUnit 5, Mockito.

**Reference spec:** `docs/superpowers/specs/2026-06-19-resource-mgmt-capability-services-db-design.md` (Part 2). Builds on Part 2a (entity/repository/mapper/SingleHost persistence/query service already exist).

**Module:** `resource_management/resource_management.features/resource_management.features.capabilities`. Always build with `-am`; run a single test class with `-Dsurefire.failIfNoSpecifiedTests=false`. To resolve deps first: `mvn -q -am -pl <module> install -Dmaven.test.skip=true`.

**Design decision (member resolution):** Today `Cluster.nodes` is a `List<org.eclipse.slm.common.consul.model.catalog.Node>`. Resources are no longer Consul nodes (Part 1). In this plan, cluster members are the **resource IDs** = keys of `memberMapping`. `Cluster.nodes` is replaced by `memberResourceIds: List<UUID>` (the `memberMapping` keys); existence/details, if needed, come from `ResourceJpaRepository`. The Consul `Node` dependency in the cluster read model is removed.

**Out of scope (unchanged):** the AWX job execution + Vault ACL/secret logic in the cluster handlers (orchestration). Only their **storage** and **access-control** side-effects move to DB. RemoteAccess (separate later part). `common.consul` dependency stays.

---

## File Structure

**Modified (model decoupling):**
- `…/model/CapabilityService.kt` — remove `NodeService` base; keep members as plain fields/getters; remove `createFromNodeService`/`createFromCatalogService`.
- `…/model/SingleHostCapabilityService.kt` — remove `createFromNodeService`.
- `…/clusters/MultiHostCapabilityService.kt` — remove `getMapOfNodeIdsAndCatalogServices` (Consul-only); keep `memberMapping`, `getTagsByNodeId`, `getServiceMetaByNodeId`, `applyScaleUp`.

**New:**
- `…/persistence/MultiHostCapabilityServicePersistence.java` — DB-backed MultiHost storage + access.
- Tests under `…/persistence/`.

**Modified (persistence):**
- `…/persistence/CapabilityServicePersistenceMapper.kt` — add MultiHost mapping.
- `…/persistence/CapabilityServiceQueryService.java` — dispatch SINGLE_HOST vs MULTI_HOST mapping.
- `…/CapabilitiesManager.java` — `isResourceClusterMember` via DB.

**Modified (cluster handlers — repoint storage/access, keep AWX/Vault):**
- `…/clusters/handler/AbstractClusterFunctions.java`, `ClusterGetFunctions.java`, `ClusterDeleteFunctions.java`, `ClusterScaleFunctions.java`, `ClusterCreateFunctions.java`
- `…/clusters/model/Cluster.kt`

**Deleted:**
- `…/clusters/MultiHostCapabilitiesConsulClient.java`
- `…/persistence/CapabilitiesConsulClient.java` (+ test `CapabilitiesConsulClientTest.java`, `MultiHostCapabilitiesConsulClientTest.java`)

---

## Task 1: Mapper — MultiHost entity ↔ domain (TDD)

**Files:**
- Modify: `…/persistence/CapabilityServicePersistenceMapper.kt`
- Test: `…/persistence/CapabilityServicePersistenceMapperMultiHostTest.java`

**Context:** `MultiHostCapabilityService` (Kotlin) ctor: `(resourceId: UUID, serviceId: UUID, capability: Capability, memberMapping: MutableMap<UUID,String>, status: CapabilityServiceStatus, managed: Boolean, customMetadata: Map<String,String>)`; property `memberMapping: MutableMap<UUID,String>?`. The entity has `serviceClass` + `memberMapping: MutableMap<UUID,String>?`.

- [ ] **Step 1: Write the failing test**

```java
package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CapabilityServicePersistenceMapperMultiHostTest {

    private final CapabilityJpaRepository capabilityJpaRepository = Mockito.mock(CapabilityJpaRepository.class);
    private final CapabilityServicePersistenceMapper mapper =
            new CapabilityServicePersistenceMapper(capabilityJpaRepository);

    @Test
    public void roundTripMultiHostPreservesMemberMapping() {
        UUID serviceId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID memberA = UUID.randomUUID();
        Capability capability = new DeploymentCapability();
        capability.setId(UUID.randomUUID());
        capability.setName("Kubernetes");

        Map<UUID, String> members = new HashMap<>();
        members.put(memberA, "control-plane");
        MultiHostCapabilityService domain = new MultiHostCapabilityService(
                resourceId, serviceId, capability, new HashMap<>(members),
                CapabilityServiceStatus.READY, true, new HashMap<>());

        CapabilityServiceEntity entity = mapper.toEntity(domain);
        assertEquals(CapabilityServiceClass.MULTI_HOST, entity.getServiceClass());
        assertEquals("control-plane", entity.getMemberMapping().get(memberA));

        Mockito.when(capabilityJpaRepository.findById(capability.getId())).thenReturn(Optional.of(capability));
        MultiHostCapabilityService back = mapper.toMultiHostDomain(entity);
        assertEquals(serviceId, back.getServiceId());
        assertEquals("control-plane", back.getMemberMapping().get(memberA));
        assertTrue(back.getManaged());
    }
}
```

- [ ] **Step 2: Run, verify FAIL**

Run: `mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test -Dtest=CapabilityServicePersistenceMapperMultiHostTest -Dsurefire.failIfNoSpecifiedTests=false`

- [ ] **Step 3: Add an overloaded `toEntity` + `toMultiHostDomain` to the mapper**

Add to `CapabilityServicePersistenceMapper.kt` (keep the existing SingleHost methods):
```kotlin
    fun toEntity(service: org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService): CapabilityServiceEntity {
        val entity = CapabilityServiceEntity(service.serviceId)
        entity.resourceId = service.resourceId
        entity.capabilityId = service.capability.id
        entity.status = service.status
        entity.managed = service.managed
        entity.port = service.port
        entity.customMeta = HashMap(service.customMeta)
        entity.serviceClass = CapabilityServiceClass.MULTI_HOST
        entity.memberMapping = service.memberMapping?.let { HashMap(it) } ?: HashMap()
        return entity
    }

    fun toMultiHostDomain(entity: CapabilityServiceEntity): org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService {
        val capabilityId = entity.capabilityId ?: throw CapabilityNotFoundException(entity.id)
        val capability = capabilityJpaRepository.findById(capabilityId)
            .orElseThrow { CapabilityNotFoundException(capabilityId) }
        val service = org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService(
            entity.resourceId!!,
            entity.id,
            capability,
            entity.memberMapping?.let { HashMap(it) } ?: HashMap(),
            entity.status,
            entity.managed,
            HashMap(entity.customMeta)
        )
        service.port = entity.port
        return service
    }
```

- [ ] **Step 4: Run, verify PASS**

Run: `mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test -Dtest=CapabilityServicePersistenceMapperMultiHostTest -Dsurefire.failIfNoSpecifiedTests=false`

- [ ] **Step 5: Commit**

```bash
git add resource_management/resource_management.features/resource_management.features.capabilities/src/main/java/org/eclipse/slm/resource_management/features/capabilities/persistence/CapabilityServicePersistenceMapper.kt \
        resource_management/resource_management.features/resource_management.features.capabilities/src/test/java/org/eclipse/slm/resource_management/features/capabilities/persistence/CapabilityServicePersistenceMapperMultiHostTest.java
git commit -m "feat(capabilities): map MultiHost capability service entity <-> domain"
```

---

## Task 2: MultiHostCapabilityServicePersistence (DB) (TDD)

**Files:**
- Create: `…/persistence/MultiHostCapabilityServicePersistence.java`
- Test: `…/persistence/MultiHostCapabilityServicePersistenceTest.java`

**Context:** Replaces `MultiHostCapabilitiesConsulClient`'s storage. Uses `CapabilityServiceJpaRepository`, the mapper, and `AccessControlService` (object type `CAPABILITY_SERVICE`). The cluster handlers call: add/update (save), getById, getAll, getByResource, delete, scale (mutate memberMapping + save), and an access-grant for a member group.

- [ ] **Step 1: Write the failing test**

```java
package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.common.access.AccessControlObjectType;
import org.eclipse.slm.resource_management.common.access.AccessControlService;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MultiHostCapabilityServicePersistenceTest {

    private final CapabilityServiceJpaRepository repository = Mockito.mock(CapabilityServiceJpaRepository.class);
    private final CapabilityJpaRepository capabilityJpaRepository = Mockito.mock(CapabilityJpaRepository.class);
    private final AccessControlService accessControlService = Mockito.mock(AccessControlService.class);
    private final CapabilityServicePersistenceMapper mapper = new CapabilityServicePersistenceMapper(capabilityJpaRepository);
    private final MultiHostCapabilityServicePersistence persistence =
            new MultiHostCapabilityServicePersistence(repository, mapper, accessControlService);

    private MultiHostCapabilityService mhcs(UUID serviceId, UUID resourceId, UUID memberA) {
        Capability capability = new DeploymentCapability();
        capability.setId(UUID.randomUUID());
        capability.setName("Kubernetes");
        HashMap<UUID, String> members = new HashMap<>();
        members.put(memberA, "control-plane");
        return new MultiHostCapabilityService(resourceId, serviceId, capability, members,
                CapabilityServiceStatus.READY, true, new HashMap<>());
    }

    @Test
    public void saveAndGetById() {
        UUID serviceId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID memberA = UUID.randomUUID();
        MultiHostCapabilityService domain = mhcs(serviceId, resourceId, memberA);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(capabilityJpaRepository.findById(domain.getCapability().getId()))
                .thenReturn(Optional.of(domain.getCapability()));

        persistence.save(domain, "/Org/CustomerA");

        verify(repository).save(any(CapabilityServiceEntity.class));
        verify(accessControlService).createSingleObjectPolicy(
                anyString(), eq("/Org/CustomerA"), eq(AccessControlObjectType.CAPABILITY_SERVICE), eq(serviceId));
    }

    @Test
    public void getAllReturnsOnlyMultiHost() {
        UUID serviceId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID memberA = UUID.randomUUID();
        CapabilityServiceEntity entity = mapper.toEntity(mhcs(serviceId, resourceId, memberA));
        when(repository.findByServiceClass(CapabilityServiceClass.MULTI_HOST)).thenReturn(List.of(entity));
        when(capabilityJpaRepository.findById(entity.getCapabilityId()))
                .thenReturn(Optional.of(new DeploymentCapability()));

        var all = persistence.getAll();

        assertEquals(1, all.size());
        assertEquals(serviceId, all.get(0).getServiceId());
    }

    @Test
    public void getServicesOfResourceMatchesMemberMappingOrOwner() {
        UUID serviceId = UUID.randomUUID();
        UUID ownerResource = UUID.randomUUID();
        UUID memberA = UUID.randomUUID();
        CapabilityServiceEntity entity = mapper.toEntity(mhcs(serviceId, ownerResource, memberA));
        when(repository.findByServiceClass(CapabilityServiceClass.MULTI_HOST)).thenReturn(List.of(entity));
        when(capabilityJpaRepository.findById(entity.getCapabilityId()))
                .thenReturn(Optional.of(new DeploymentCapability()));

        assertEquals(1, persistence.getServicesOfResource(memberA).size());
        assertEquals(0, persistence.getServicesOfResource(UUID.randomUUID()).size());
    }

    @Test
    public void deleteRemovesEntityAndPolicy() {
        UUID serviceId = UUID.randomUUID();
        persistence.delete(serviceId);
        verify(repository).deleteById(serviceId);
        verify(accessControlService).removeObjectFromAllPolicies(AccessControlObjectType.CAPABILITY_SERVICE, serviceId);
    }
}
```

- [ ] **Step 2: Run, verify FAIL**

Run: `mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test -Dtest=MultiHostCapabilityServicePersistenceTest -Dsurefire.failIfNoSpecifiedTests=false`

- [ ] **Step 3: Create the component**

```java
package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.common.access.AccessControlObjectType;
import org.eclipse.slm.resource_management.common.access.AccessControlService;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MultiHostCapabilityServicePersistence {

    public static final String CAPABILITY_SERVICE_POLICY_PREFIX = "capability-service_";

    private final CapabilityServiceJpaRepository repository;
    private final CapabilityServicePersistenceMapper mapper;
    private final AccessControlService accessControlService;

    public MultiHostCapabilityServicePersistence(
            CapabilityServiceJpaRepository repository,
            CapabilityServicePersistenceMapper mapper,
            AccessControlService accessControlService) {
        this.repository = repository;
        this.mapper = mapper;
        this.accessControlService = accessControlService;
    }

    /** Persists (insert or update) the multi-host service. Creates an owner access policy on first save. */
    public MultiHostCapabilityService save(MultiHostCapabilityService service, String fullPathOwnerGroupId) {
        boolean isNew = repository.findById(service.getServiceId()).isEmpty();
        repository.save(mapper.toEntity(service));
        if (isNew && fullPathOwnerGroupId != null) {
            accessControlService.createSingleObjectPolicy(
                    CAPABILITY_SERVICE_POLICY_PREFIX + service.getServiceId(),
                    fullPathOwnerGroupId,
                    AccessControlObjectType.CAPABILITY_SERVICE,
                    service.getServiceId());
        }
        return service;
    }

    /** Update without touching access policies. */
    public void update(MultiHostCapabilityService service) {
        repository.save(mapper.toEntity(service));
    }

    public Optional<MultiHostCapabilityService> getById(UUID serviceId) {
        return repository.findById(serviceId)
                .filter(e -> e.getServiceClass() == CapabilityServiceClass.MULTI_HOST)
                .map(mapper::toMultiHostDomain);
    }

    public List<MultiHostCapabilityService> getAll() {
        return repository.findByServiceClass(CapabilityServiceClass.MULTI_HOST).stream()
                .map(mapper::toMultiHostDomain)
                .toList();
    }

    /** Services where the resource is the owner OR a cluster member (memberMapping key). */
    public List<MultiHostCapabilityService> getServicesOfResource(UUID resourceId) {
        return repository.findByServiceClass(CapabilityServiceClass.MULTI_HOST).stream()
                .filter(e -> resourceId.equals(e.getResourceId())
                        || (e.getMemberMapping() != null && e.getMemberMapping().containsKey(resourceId)))
                .map(mapper::toMultiHostDomain)
                .toList();
    }

    public void delete(UUID serviceId) {
        repository.deleteById(serviceId);
        accessControlService.removeObjectFromAllPolicies(AccessControlObjectType.CAPABILITY_SERVICE, serviceId);
    }
}
```

- [ ] **Step 4: Run, verify PASS (4 tests)**

Run: `mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test -Dtest=MultiHostCapabilityServicePersistenceTest -Dsurefire.failIfNoSpecifiedTests=false`

- [ ] **Step 5: Commit**

```bash
git add resource_management/resource_management.features/resource_management.features.capabilities/src/main/java/org/eclipse/slm/resource_management/features/capabilities/persistence/MultiHostCapabilityServicePersistence.java \
        resource_management/resource_management.features/resource_management.features.capabilities/src/test/java/org/eclipse/slm/resource_management/features/capabilities/persistence/MultiHostCapabilityServicePersistenceTest.java
git commit -m "feat(capabilities): add DB-backed MultiHost capability service persistence"
```

---

## Task 3: Query service maps MULTI_HOST; CapabilitiesManager.isResourceClusterMember via DB (TDD)

**Files:**
- Modify: `…/persistence/CapabilityServiceQueryService.java`
- Modify: `…/CapabilitiesManager.java`
- Test: extend `…/persistence/CapabilityServiceQueryServiceTest.java`

- [ ] **Step 1: Add a failing test for MULTI_HOST mapping**

Add to `CapabilityServiceQueryServiceTest`:
```java
    @Test
    public void getCapabilityServicesMapsMultiHostByDiscriminator() {
        org.eclipse.slm.resource_management.features.capabilities.model.Capability capability =
                new org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability();
        capability.setId(java.util.UUID.randomUUID());
        CapabilityServiceEntity entity = new CapabilityServiceEntity(java.util.UUID.randomUUID());
        entity.setResourceId(java.util.UUID.randomUUID());
        entity.setCapabilityId(capability.getId());
        entity.setServiceClass(CapabilityServiceClass.MULTI_HOST);
        entity.setMemberMapping(new java.util.HashMap<>());
        when(repository.findAll()).thenReturn(java.util.List.of(entity));
        when(capabilityJpaRepository.findById(capability.getId())).thenReturn(java.util.Optional.of(capability));

        var result = queryService.getCapabilityServices();

        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService);
    }
```

- [ ] **Step 2: Run, verify FAIL** (currently everything maps as SingleHost)

Run: `mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test -Dtest=CapabilityServiceQueryServiceTest -Dsurefire.failIfNoSpecifiedTests=false`

- [ ] **Step 3: Dispatch the mapping by discriminator**

In `CapabilityServiceQueryService.toDomain`:
```java
    private CapabilityService toDomain(CapabilityServiceEntity entity) {
        if (entity.getServiceClass() == CapabilityServiceClass.MULTI_HOST) {
            return mapper.toMultiHostDomain(entity);
        }
        return mapper.toSingleHostDomain(entity);
    }
```

- [ ] **Step 4: Run, verify PASS**

Run: `mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test -Dtest=CapabilityServiceQueryServiceTest -Dsurefire.failIfNoSpecifiedTests=false`

- [ ] **Step 5: Repoint `CapabilitiesManager.isResourceClusterMember` to DB**

Replace the `multiHostCapabilitiesConsulClient` field in `CapabilitiesManager` with `MultiHostCapabilityServicePersistence multiHostCapabilityServicePersistence` (constructor param + assignment; remove the `MultiHostCapabilitiesConsulClient` import). Rewrite:
```java
    @Override
    public boolean isResourceClusterMember(UUID resourceId) throws ResourceNotFoundException {
        return !this.multiHostCapabilityServicePersistence.getServicesOfResource(resourceId).isEmpty();
    }
```

- [ ] **Step 6: Compile** (other MultiHost consumers — cluster handlers — still reference the old client; that's Task 5)

Run: `mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities compile`
Expected: FAIL only in cluster handlers/`MultiHostCapabilitiesConsulClient` (handled in Task 5). Proceed.

- [ ] **Step 7: Commit (after Task 5 compiles)** — do not commit a broken module; this task's changes are committed together with Task 5.

---

## Task 4: Decouple model classes from `NodeService`

**Files:**
- Modify: `…/model/CapabilityService.kt`
- Modify: `…/model/SingleHostCapabilityService.kt`
- Modify: `…/clusters/MultiHostCapabilityService.kt`

**Context:** Consumers rely on `getServiceId()`, `getResourceId()`, `getCapability()`, `getStatus()`, `getManaged()`, `getCustomMeta()`, `getPort()`/`setPort()`, `getServiceName()`, `getTags()`, `getMeta()`, `getServiceClass()`. MultiHost also: `getMemberMapping()`, `getTagsByNodeId(UUID)`, `getServiceMetaByNodeId(UUID)`, `applyScaleUp(ScaleUpOperation)`. The `createFromNodeService`/`createFromCatalogService` factories and `getMapOfNodeIdsAndCatalogServices` are Consul-only and are removed (their only callers — `CapabilitiesConsulClient`, `MultiHostCapabilitiesConsulClient` — are deleted in Task 5).

- [ ] **Step 1: Rewrite `CapabilityService.kt` without `NodeService`**

```kotlin
package org.eclipse.slm.resource_management.features.capabilities.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "capabilityServiceClass")
@JsonSubTypes(
    JsonSubTypes.Type(value = SingleHostCapabilityService::class, name = "SingleHostCapabilityService"),
    JsonSubTypes.Type(value = MultiHostCapabilityService::class, name = "MultiHostCapabilityService")
)
open class CapabilityService(

    @param:JsonProperty("resourceId")
    val resourceId: UUID,

    @JsonProperty("serviceId")
    var serviceId: UUID,

    @param:JsonProperty("capability")
    val capability: Capability,

    @param:JsonProperty("status")
    var status: CapabilityServiceStatus = CapabilityServiceStatus.UNKNOWN,

    @param:JsonProperty("managed")
    var managed: Boolean = false,

    @param:JsonProperty("customMeta")
    var customMeta: Map<String, String> = emptyMap()
) {

    var serviceClass: String = this.javaClass.simpleName

    var port: Int? = null

    val serviceName: String
        get() = capability.name.lowercase().replace(" ", "_") + "_" + serviceId

    val tags: List<String>
        get() = arrayListOf(
            TAG_CAPABILITY,
            capability.name,
            capability.capabilityClass,
            this.javaClass.simpleName
        )

    val meta: Map<String, String>
        get() {
            val defaultMap = hashMapOf(
                META_KEY_CAPABILITY_SERVICE_CLASS to serviceClass,
                META_KEY_CAPABILITY_CLASS to capability.capabilityClass,
                META_KEY_CAPABILITY_ID to capability.id.toString(),
                META_KEY_CONNECTION_TYPE to capability.connection.toString(),
                META_KEY_STATUS to status.name,
                META_KEY_MANAGED to managed.toString(),
            )
            return customMeta + defaultMap
        }

    companion object {
        const val META_KEY_CAPABILITY_SERVICE_CLASS = "capabilityServiceClass"
        const val META_KEY_CAPABILITY_CLASS = "capabilityClass"
        const val META_KEY_CAPABILITY_ID = "capabilityId"
        const val META_KEY_CONNECTION_TYPE = "connectionType"
        const val META_KEY_STATUS = "status"
        const val META_KEY_MANAGED = "managed"
        const val TAG_CAPABILITY = "Capability"
    }
}
```
Notes: `serviceId` is now a plain `var` (Java getter `getServiceId()`/`setServiceId()`); `port` is a plain `Int?` (`getPort()`/`setPort()`). The `Builder` and `createFrom*` factories are removed (only used by the deleted Consul clients). If a non-deleted caller still uses `CapabilityService.builder(...)`, STOP and report it — but per analysis only `createFrom*` used the Builder.

- [ ] **Step 2: Update `SingleHostCapabilityService.kt`**

Remove the `createFromNodeService` companion factory (it referenced `NodeService`). Keep the constructor. Result:
```kotlin
package org.eclipse.slm.resource_management.features.capabilities.model

import com.fasterxml.jackson.annotation.JsonTypeName
import java.util.*

@JsonTypeName("SingleHostCapabilityService")
class SingleHostCapabilityService(
    resourceId: UUID,
    serviceId: UUID,
    capability: Capability,
    status: CapabilityServiceStatus,
    isManaged: Boolean,
    configParameter: MutableMap<String, String>,
) : CapabilityService(resourceId, serviceId, capability, status, isManaged, configParameter)
```
(If anything still calls `SingleHostCapabilityService.createFromNodeService`, it is deleted-Consul code removed in Task 5; if a surviving caller uses it, STOP and report.)

- [ ] **Step 3: Update `MultiHostCapabilityService.kt`**

Remove `getMapOfNodeIdsAndCatalogServices()` (builds Consul `NodeService`) and the `NodeService` import. Keep `memberMapping`, `getTagsByNodeId`, `getServiceMetaByNodeId`, `applyScaleUp`. `getServiceMetaByNodeId` uses `meta` (now a plain getter on the base) — still valid. Result:
```kotlin
package org.eclipse.slm.resource_management.features.capabilities.clusters

import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.resource_management.features.capabilities.model.Capability
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus
import java.util.*

@JsonTypeName("MultiHostCapabilityService")
class MultiHostCapabilityService : CapabilityService {

    constructor(
        resourceId: UUID,
        serviceId: UUID,
        capability: Capability,
        memberMapping: MutableMap<UUID, String>,
        status: CapabilityServiceStatus,
        managed: Boolean,
        customMetadata: Map<String, String>
    ) : super(resourceId, serviceId, capability, status, managed, customMetadata) {
        this.memberMapping = memberMapping
    }

    //<NodeID, MemberTypeName>
    var memberMapping: MutableMap<UUID, String>? = null

    fun getTagsByNodeId(nodeId: UUID): ArrayList<String> {
        val serviceTags = ArrayList(tags)
        val clusterMemberTypeName = memberMapping!![nodeId]
        val clusterMemberType = capability.clusterMemberTypes.firstOrNull { it.name.equals(clusterMemberTypeName) }
        serviceTags.add(clusterMemberTypeName!!)
        serviceTags.add(clusterMemberType!!.prettyName!!)
        return serviceTags
    }

    fun getServiceMetaByNodeId(nodeId: UUID): HashMap<String, String> {
        val meta = meta.toMutableMap()
        val clusterMemberTypeName = memberMapping!![nodeId]
        if (clusterMemberTypeName != null) {
            meta["clusterMemberType"] = clusterMemberTypeName
        }
        return meta as HashMap<String, String>
    }

    fun applyScaleUp(scaleUpOperation: ScaleUpOperation) {
        this.memberMapping!![scaleUpOperation.resourceId] = scaleUpOperation.clusterMemberType.name
    }
}
```
(`ScaleUpOperation` is in package `…clusters`; confirm import/location — it was referenced unqualified before, so it is in the same package.)

- [ ] **Step 4: Compile check (expected to fail in Consul clients + handlers, fixed in Task 5)**

Run: `mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities compile`
Expected: errors in `CapabilitiesConsulClient`, `MultiHostCapabilitiesConsulClient`, cluster handlers, `Cluster.kt` (they use removed factories / `NodeService` semantics). These are all resolved in Task 5. Do not commit yet.

---

## Task 5: Repoint cluster handlers to DB, remove Consul clients, reactivate reads (coupled compile gate)

**Files:**
- Modify: `…/clusters/handler/AbstractClusterFunctions.java`, `ClusterGetFunctions.java`, `ClusterDeleteFunctions.java`, `ClusterScaleFunctions.java`, `ClusterCreateFunctions.java`
- Modify: `…/clusters/model/Cluster.kt`
- Delete: `…/clusters/MultiHostCapabilitiesConsulClient.java`, `…/persistence/CapabilitiesConsulClient.java` (+ tests `MultiHostCapabilitiesConsulClientTest.java`, `CapabilitiesConsulClientTest.java`, and `MultiHostCapabilitiesConsulClientTestData`/related if present)

This task only compiles as a whole. Read each file first.

- [ ] **Step 1: `AbstractClusterFunctions` — swap injected storage client**

Replace the field/ctor param `CapabilitiesConsulClient capabilitiesConsulClient` and `MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient` with:
- `protected final MultiHostCapabilityServicePersistence multiHostCapabilityServicePersistence;`
- `protected final org.eclipse.slm.resource_management.common.resources.ResourceJpaRepository resourceJpaRepository;`
- `protected final org.eclipse.slm.resource_management.common.access.AccessControlService accessControlService;`
Keep the Consul `consulAdminClient`/Vault/AWX/RemoteAccess fields (still used by orchestration). Update all 4 subclass constructors (`ClusterGetFunctions`, `ClusterDeleteFunctions`, `ClusterScaleFunctions`, `ClusterCreateFunctions`) to pass the new dependencies to `super(...)` instead of the two removed ones. Remove the deleted-class imports.

- [ ] **Step 2: Map the MultiHost storage calls in handlers to the DB component**

Apply these replacements across the handlers (read each call site; keep all AWX `executeJob`/observer and Vault ACL logic unchanged):
- `multiHostCapabilitiesConsulClient.getMultiHostCapabilityServiceOfUser(serviceId)` → `multiHostCapabilityServicePersistence.getById(serviceId)` (both return `Optional<MultiHostCapabilityService>`).
- `multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser()` → `multiHostCapabilityServicePersistence.getAll()`.
- `multiHostCapabilitiesConsulClient.updateMultiHostCapabilityService(svc)` → `multiHostCapabilityServicePersistence.update(svc)`.
- `multiHostCapabilitiesConsulClient.addMultiHostCapabilityService(svc)` → `multiHostCapabilityServicePersistence.save(svc, <ownerGroup>)` where `<ownerGroup>` is the `fullPathOwnerGroupId` already used in `ClusterCreateFunctions` (find it; it is the create request's owner group). If a create path has no owner group in scope, pass the cluster owner group already used for the Vault group naming.
- `multiHostCapabilitiesConsulClient.removeMultiHostCapabilityService(serviceId)` → `multiHostCapabilityServicePersistence.delete(serviceId)`.
- `multiHostCapabilitiesConsulClient.scaleMultiHostCapabilityService(scaleOp, serviceId)` → load via `getById`, call `svc.applyScaleUp(...)` (scale-up) or remove the member from `memberMapping` (scale-down), then `update(svc)`. Mirror the existing scale semantics in `ClusterScaleFunctions` (read it); keep the AWX scale job execution unchanged.
- `multiHostCapabilitiesConsulClient.addReadRuleForCapabilityServiceToResourcePolicy(svc)` → for each member group, `accessControlService` grant. Concretely: this previously added a Consul read rule per member; replace with `accessControlService.createSingleObjectPolicy("capability-service_" + svc.getServiceId() + "_" + memberResourceId, <memberGroupPath>, AccessControlObjectType.CAPABILITY_SERVICE, svc.getServiceId())` — OR, if member group paths are not readily available, this call becomes a no-op comment `// TODO(part2b): member-level grants handled by owner policy` because the owner policy created in `save(...)` already grants the owner. Prefer the no-op if the member→group mapping is not in scope (the cluster is visible to its owner group; per-member read grants were a Consul-specific refinement). Add a clear comment.
- `multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(serviceId)` → resolve member resource IDs from the service's `memberMapping().keySet()`; see Step 4 (`getClusterMembers`).
- `ResourcesConsulClient.getResourcePolicyName(...)` usage in `ClusterCreateFunctions` (line ~262) → remove; access is now via the `accessControlService` policy created in `save(...)`. Remove the `ResourcesConsulClient` import.

- [ ] **Step 3: `Cluster.kt` — replace Consul `Node` members with resource IDs**

```kotlin
package org.eclipse.slm.resource_management.features.capabilities.clusters.model

import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService
import java.util.*

class Cluster {
    var id: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    var name: String? = ""
    var clusterType: String = ""
    var clusterMemberTypes: List<ClusterMemberType> = emptyList()
    var memberResourceIds: List<UUID> = emptyList()
    var memberMapping: Map<UUID, String> = emptyMap()
    var metaData: Map<String, String>? = emptyMap()
    var capabilityService: MultiHostCapabilityService? = null
    var managed: Boolean = false

    constructor(multiHostCapabilityService: MultiHostCapabilityService, memberResourceIds: List<UUID>, metaData: Map<String, String>) {
        this.id = multiHostCapabilityService.serviceId
        this.name = multiHostCapabilityService.serviceName
        this.clusterType = multiHostCapabilityService.capability.name
        this.memberMapping = multiHostCapabilityService.memberMapping ?: emptyMap()
        this.clusterMemberTypes = multiHostCapabilityService.capability.clusterMemberTypes
        this.memberResourceIds = memberResourceIds
        this.managed = multiHostCapabilityService.managed
        this.capabilityService = multiHostCapabilityService
        this.metaData = multiHostCapabilityService.meta + metaData
    }

    constructor()
}
```
Update any reference to the old `nodes` field (e.g. in `ClusterGetFunctions`) to `memberResourceIds`.

- [ ] **Step 4: `ClusterGetFunctions` — reactivate `getClusters`/`getClusterMembers` from DB**

```java
    public List<Cluster> getClusters() {
        List<Cluster> clusterList = new ArrayList<>();
        for (MultiHostCapabilityService mhcs : this.multiHostCapabilityServicePersistence.getAll()) {
            List<UUID> memberResourceIds = mhcs.getMemberMapping() == null
                    ? List.of() : new ArrayList<>(mhcs.getMemberMapping().keySet());
            Map<String, String> secrets = new HashMap<>();
            try {
                secrets = this.vaultAdminClient.kv("resources")
                        .getSecretsOfPathOrThrow(mhcs.getServiceId().toString()).getData();
            } catch (VaultRuntimeException e) {
                LOG.info("Cluster has no secrets: " + e.getMessage());
            }
            clusterList.add(new Cluster(mhcs, memberResourceIds, secrets));
        }
        return clusterList;
    }

    public List<UUID> getClusterMembers(UUID clusterServiceId) {
        return this.multiHostCapabilityServicePersistence.getById(clusterServiceId)
                .map(mhcs -> mhcs.getMemberMapping() == null
                        ? new ArrayList<UUID>() : new ArrayList<>(mhcs.getMemberMapping().keySet()))
                .orElse(new ArrayList<>());
    }
```
Update `ClusterHandler.getClusterMembers` and `ClustersRestController` to the new `getClusterMembers(UUID)` signature/return type (was `List<Service> getClusterMembers(String)`). The REST endpoint `/{clusterName}/members` becomes `/{clusterServiceId}/members` returning `List<UUID>` — adjust the controller method param to `UUID` and return type accordingly. Read `ClustersRestController` and apply the minimal consistent change.

- [ ] **Step 5: Delete the Consul clients + their tests**

```bash
cd /home/operation/Development/slm2
git rm resource_management/resource_management.features/resource_management.features.capabilities/src/main/java/org/eclipse/slm/resource_management/features/capabilities/clusters/MultiHostCapabilitiesConsulClient.java \
       resource_management/resource_management.features/resource_management.features.capabilities/src/main/java/org/eclipse/slm/resource_management/features/capabilities/persistence/CapabilitiesConsulClient.java
git rm resource_management/resource_management.features/resource_management.features.capabilities/src/test/java/org/eclipse/slm/resource_management/features/capabilities/MultiHostCapabilitiesConsulClientTest.java \
       resource_management/resource_management.features/resource_management.features.capabilities/src/test/java/org/eclipse/slm/resource_management/features/capabilities/CapabilitiesConsulClientTest.java 2>/dev/null || true
```

- [ ] **Step 6: Resolve all remaining references and compile**

Run:
```bash
grep -rn "CapabilitiesConsulClient\|MultiHostCapabilitiesConsulClient\|getMapOfNodeIdsAndCatalogServices\|createFromNodeService\|createFromCatalogService" \
  resource_management/resource_management.features/resource_management.features.capabilities/src/main --include="*.java" --include="*.kt"
```
Expected: empty. Then:
`mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities compile`
Iterate until BUILD SUCCESS. If a cluster orchestration method genuinely depended on a Consul-only behavior that has no DB equivalent and isn't storage/access (e.g. live Consul service registration consumed elsewhere), STOP and report NEEDS_CONTEXT rather than inventing behavior.

- [ ] **Step 7: Commit (Tasks 3, 4, 5 together)**

```bash
git add -A
git commit -m "refactor(capabilities): migrate MultiHost/cluster storage to DB, decouple model from NodeService, remove capability Consul clients"
```

---

## Task 6: Build + test verification

- [ ] **Step 1: Module test suite**

Run: `mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test`
Expected: green. Fix or delete tests that referenced deleted Consul clients or the old `Cluster.nodes`/`createFrom*` APIs; migrate behavior to the new components, do not weaken assertions.

- [ ] **Step 2: Service-app build (wiring/entity scan)**

Run: `mvn -q -am -pl resource_management/resource_management.service/resource_management.service.app -Dmaven.test.skip=true install`
Expected: BUILD SUCCESS. (If it fails only in the `docker-maven-plugin:build` goal, that is an environment issue — re-run with `compile` instead to confirm code compiles: `mvn -q -am -pl resource_management/resource_management.service/resource_management.service.app compile`.)

- [ ] **Step 3: Downstream compile**

Run: `mvn -q -am -Dmaven.test.skip=true -pl service_management/service_management.service/service_management.service.app,notification_service/notification_service.service/notification_service.service.app compile`
Expected: BUILD SUCCESS. (`service_management` consumes capability services via REST; the providers/serviceHoster payload shape is unchanged.)

- [ ] **Step 4: Commit (if test fixes were needed)**

```bash
git add -A
git commit -m "test(capabilities): migrate cluster/MultiHost tests to DB persistence"
```

---

## Self-Review Checklist (completed during planning)

- **Spec coverage (Part 2b):** MultiHost storage → DB (Tasks 1,2); query maps MULTI_HOST (Task 3); `isResourceClusterMember` via DB (Task 3); `NodeService` decoupling (Task 4); remove `MultiHostCapabilitiesConsulClient` + `CapabilitiesConsulClient` (Task 5); reactivate `getClusters`/`getClusterMembers` (Task 5 Step 4); replace `ResourcesConsulClient.getResourcePolicyName` in `ClusterCreateFunctions` (Task 5 Step 2); AWX orchestration left intact (only storage/access migrated); build/test (Task 6).
- **Type consistency:** mapper `toEntity`(SingleHost/MultiHost overloads) / `toSingleHostDomain` / `toMultiHostDomain`; `MultiHostCapabilityServicePersistence` (`save`, `update`, `getById`, `getAll`, `getServicesOfResource`, `delete`); `CapabilityServiceQueryService.toDomain` discriminator dispatch; model getters `getServiceId/getServiceName/getTags/getMeta/getPort/getMemberMapping`; `Cluster(MultiHostCapabilityService, List<UUID>, Map)`; `getClusterMembers(UUID): List<UUID>`.
- **Documented design decision (not a placeholder):** cluster members are `memberMapping` resource IDs (Consul `Node` list replaced by `memberResourceIds`); per-member Consul read-rules collapse into the owner access policy (commented), because resource-level/owner-group visibility already governs access.
- **Coupling note:** Tasks 3–5 only compile together and are committed as one unit (Task 5 Step 7); Tasks 1–2 are independent, committed earlier.
