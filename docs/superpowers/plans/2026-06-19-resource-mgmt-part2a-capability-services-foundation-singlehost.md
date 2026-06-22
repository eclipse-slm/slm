# Resource Management Part 2a — CapabilityServices Foundation + SingleHost (Consul → DB)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persist **SingleHost** CapabilityServices in the database instead of Consul, reusing the Part 1 `AccessControlService`, with a flat `CapabilityServiceEntity` that also accommodates MultiHost for a later plan.

**Architecture:** A flat `CapabilityServiceEntity` (discriminator `serviceClass`, JSON `customMeta`/`memberMapping`) persisted via `CapabilityServiceJpaRepository`. A hand-written persistence mapper converts entity ↔ the existing domain classes (which keep extending Consul `NodeService` for now — full decoupling is deferred to Part 2b). The Consul-backed read/write paths for single-host services move to the repository; per-service access policies move to `AccessControlService` (object type `CAPABILITY_SERVICE`). Visibility parity is preserved: reads that used the Consul **admin** client stay unfiltered.

**Tech Stack:** Java 17 + Kotlin, Spring Boot, Spring Data JPA, MariaDB (Testcontainers in tests), JUnit 5.

**Reference spec:** `docs/superpowers/specs/2026-06-19-resource-mgmt-capability-services-db-design.md`

**Scope of this plan (2a):** Spec phases 1, 2, partial 3 (mapper only — no `NodeService` removal yet), and 4 (SingleHost). **Deferred to Part 2b:** MultiHost write/storage, removal of `NodeService` inheritance, `MultiHostCapabilitiesConsulClient` removal, cluster-handler storage migration, replacing `ResourcesConsulClient.getResourcePolicyName` usage in `ClusterCreateFunctions`, and final cleanup.

**Module:** `resource_management/resource_management.features/resource_management.features.capabilities` (paths below are relative to repo root). Build with `-am`; if isolated build fails on unrelated missing artifacts, first `mvn -q -am -pl <module> install -Dmaven.test.skip=true`.

---

## File Structure

**New (package `…features.capabilities.persistence`):**
- `CapabilityServiceClass.kt` — enum `SINGLE_HOST | MULTI_HOST`
- `CapabilityServiceEntity.kt` — flat JPA entity
- `CapabilityServiceJpaRepository.java` — Spring Data repo
- `CapabilityServicePersistenceMapper.kt` — entity ↔ domain (`SingleHostCapabilityService`)
- Tests under `…/src/test/java/.../capabilities/persistence/`

**Modified:**
- `…persistence/SingleHostCapabilitiesConsulClient.java` → renamed responsibility to DB; becomes `SingleHostCapabilityServicePersistence` (new file) and the Consul class is deleted (see Task 8).
- `…persistence/CapabilitiesConsulClient.java` → read methods move to DB; class deleted, replaced by `CapabilityServiceQueryService` (Task 7).
- `…CapabilitiesManager.java`, `…providers/ProviderHandler.java`, `…providers/ServiceHosterHandler.java` — switch to the new query service.
- `…jobs/CapabilityJobServiceImpl.java`, `…jobs/CapabilityJobExecutor.java` — switch to the new persistence component.

**Deleted (after callers migrated):**
- `…persistence/CapabilitiesConsulClient.java`, `…persistence/SingleHostCapabilitiesConsulClient.java` and their tests.

**Untouched in 2a (still Consul):** `MultiHostCapabilitiesConsulClient`, all `clusters/handler/*`, `MultiHostCapabilityService`. These still reference `CapabilitiesConsulClient`/`MultiHostCapabilitiesConsulClient`; keep them compiling (see Task 7/8 bridge notes).

---

## Task 1: Verify no external module reads CapabilityServices directly from Consul

**Files:** none (investigation)

- [ ] **Step 1: Search outside resource_management for direct Consul service reads of capability services**

Run:
```bash
grep -rn "getServicesByTag\|getNodeServices\|TAG_CAPABILITY\|capabilityId\|CapabilityService" \
  /home/operation/Development/slm2/service_management \
  /home/operation/Development/slm2/platform_management \
  --include="*.java" --include="*.kt" | grep -i consul
```
Expected: no external module reads capability services from Consul directly. Confirm `service_management` obtains them via REST (`resourceManagementClient.providers().getServiceHosters(...)`).

- [ ] **Step 2: Record finding**

If external direct Consul reads exist, STOP and report to the user before proceeding. Otherwise note "verified: external consumers use REST" and continue. (No commit.)

---

## Task 2: CapabilityServiceClass enum

**Files:**
- Create: `resource_management/resource_management.features/resource_management.features.capabilities/src/main/java/org/eclipse/slm/resource_management/features/capabilities/persistence/CapabilityServiceClass.kt`

- [ ] **Step 1: Create the enum**

```kotlin
package org.eclipse.slm.resource_management.features.capabilities.persistence

enum class CapabilityServiceClass {
    SINGLE_HOST,
    MULTI_HOST
}
```

- [ ] **Step 2: Commit**

```bash
git add resource_management/resource_management.features/resource_management.features.capabilities/src/main/java/org/eclipse/slm/resource_management/features/capabilities/persistence/CapabilityServiceClass.kt
git commit -m "feat(capabilities): add CapabilityServiceClass enum"
```

---

## Task 3: CapabilityServiceEntity + repository (TDD)

**Files:**
- Create: `…/capabilities/persistence/CapabilityServiceEntity.kt`
- Create: `…/capabilities/persistence/CapabilityServiceJpaRepository.java`
- Test: `…/src/test/java/org/eclipse/slm/resource_management/features/capabilities/persistence/CapabilityServiceJpaRepositoryTest.java`

- [ ] **Step 1: Write the failing test**

```java
package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CapabilityServiceJpaRepositoryTest {

    @Autowired
    private CapabilityServiceJpaRepository repository;

    private CapabilityServiceEntity singleHost(UUID id, UUID resourceId, UUID capabilityId) {
        CapabilityServiceEntity e = new CapabilityServiceEntity();
        e.setId(id);
        e.setResourceId(resourceId);
        e.setCapabilityId(capabilityId);
        e.setStatus(CapabilityServiceStatus.READY);
        e.setManaged(true);
        e.setPort(8080);
        e.setCustomMeta(new java.util.HashMap<>(Map.of("k", "v")));
        e.setServiceClass(CapabilityServiceClass.SINGLE_HOST);
        return e;
    }

    @Test
    public void saveAndLoadPreservesFieldsIncludingJsonMeta() {
        UUID id = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID capabilityId = UUID.randomUUID();
        repository.save(singleHost(id, resourceId, capabilityId));

        Optional<CapabilityServiceEntity> loaded = repository.findById(id);
        assertTrue(loaded.isPresent());
        assertEquals(resourceId, loaded.get().getResourceId());
        assertEquals(capabilityId, loaded.get().getCapabilityId());
        assertEquals(CapabilityServiceStatus.READY, loaded.get().getStatus());
        assertTrue(loaded.get().getManaged());
        assertEquals(8080, loaded.get().getPort());
        assertEquals("v", loaded.get().getCustomMeta().get("k"));
        assertEquals(CapabilityServiceClass.SINGLE_HOST, loaded.get().getServiceClass());
    }

    @Test
    public void findByResourceIdAndCapabilityId() {
        UUID resourceId = UUID.randomUUID();
        UUID capabilityId = UUID.randomUUID();
        repository.save(singleHost(UUID.randomUUID(), resourceId, capabilityId));
        repository.save(singleHost(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));

        List<CapabilityServiceEntity> byResource = repository.findByResourceId(resourceId);
        Optional<CapabilityServiceEntity> byBoth =
                repository.findByResourceIdAndCapabilityId(resourceId, capabilityId);

        assertEquals(1, byResource.size());
        assertTrue(byBoth.isPresent());
        assertEquals(capabilityId, byBoth.get().getCapabilityId());
    }

    @Test
    public void findByServiceClassFiltersDiscriminator() {
        repository.save(singleHost(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        assertEquals(1, repository.findByServiceClass(CapabilityServiceClass.SINGLE_HOST).size());
        assertEquals(0, repository.findByServiceClass(CapabilityServiceClass.MULTI_HOST).size());
    }
}
```

- [ ] **Step 2: Run, verify FAIL**

Run: `cd /home/operation/Development/slm2 && mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test -Dtest=CapabilityServiceJpaRepositoryTest`
Expected: FAIL (entity/repository missing).

- [ ] **Step 3: Create the entity**

```kotlin
package org.eclipse.slm.resource_management.features.capabilities.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
class CapabilityServiceEntity(id: UUID? = null) {

    constructor() : this(null)

    @field:Id
    @field:Column(name = "uuid", length = 36, unique = true, nullable = false)
    var id: UUID = id ?: UUID.randomUUID()

    @field:Column(name = "resource_id")
    var resourceId: UUID? = null

    @field:Column(name = "capability_id")
    var capabilityId: UUID? = null

    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "status")
    var status: CapabilityServiceStatus = CapabilityServiceStatus.UNKNOWN

    @field:Column(name = "managed")
    var managed: Boolean = false

    @field:Column(name = "port")
    var port: Int? = null

    @field:Column(name = "custom_meta", columnDefinition = "LONGTEXT")
    @field:JdbcTypeCode(SqlTypes.JSON)
    var customMeta: MutableMap<String, String> = mutableMapOf()

    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "service_class")
    var serviceClass: CapabilityServiceClass = CapabilityServiceClass.SINGLE_HOST

    @field:Column(name = "member_mapping", columnDefinition = "LONGTEXT")
    @field:JdbcTypeCode(SqlTypes.JSON)
    var memberMapping: MutableMap<UUID, String>? = null
}
```

- [ ] **Step 4: Create the repository**

```java
package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface CapabilityServiceJpaRepository extends JpaRepository<CapabilityServiceEntity, UUID> {

    List<CapabilityServiceEntity> findByResourceId(UUID resourceId);

    List<CapabilityServiceEntity> findByServiceClass(CapabilityServiceClass serviceClass);

    List<CapabilityServiceEntity> findByIdIn(Set<UUID> ids);

    Optional<CapabilityServiceEntity> findByResourceIdAndCapabilityId(UUID resourceId, UUID capabilityId);

    @Query("SELECT cs FROM CapabilityServiceEntity cs, " +
           "org.eclipse.slm.resource_management.features.capabilities.model.Capability c " +
           "WHERE cs.capabilityId = c.id AND c.capabilityClass = :capabilityClass")
    List<CapabilityServiceEntity> findByCapabilityClass(@Param("capabilityClass") String capabilityClass);
}
```

- [ ] **Step 5: Run, verify PASS (3 tests)**

Run: `cd /home/operation/Development/slm2 && mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test -Dtest=CapabilityServiceJpaRepositoryTest`
Expected: PASS. If `@DataJpaTest` cannot find a `@SpringBootConfiguration`, add a minimal `TestApplication` (`@SpringBootApplication`, `@ActiveProfiles("test")`) in the test `persistence` package, mirroring the pattern used in `resource_management.common` tests.

- [ ] **Step 6: Commit**

```bash
git add resource_management/resource_management.features/resource_management.features.capabilities/src/main/java/org/eclipse/slm/resource_management/features/capabilities/persistence/CapabilityServiceEntity.kt \
        resource_management/resource_management.features/resource_management.features.capabilities/src/main/java/org/eclipse/slm/resource_management/features/capabilities/persistence/CapabilityServiceJpaRepository.java \
        resource_management/resource_management.features/resource_management.features.capabilities/src/test/java/org/eclipse/slm/resource_management/features/capabilities/persistence/CapabilityServiceJpaRepositoryTest.java
git commit -m "feat(capabilities): add CapabilityServiceEntity + repository"
```

---

## Task 4: CapabilityServicePersistenceMapper (entity ↔ SingleHost domain) (TDD)

**Context:** `SingleHostCapabilityService` (Kotlin) has constructor `(resourceId: UUID, serviceId: UUID, capability: Capability, status: CapabilityServiceStatus, isManaged: Boolean, configParameter: MutableMap<String,String>)` and `port` is a settable property inherited from the Consul `NodeService` base (`setPort(Integer)`). `getServiceId()`, `getResourceId()`, `getCapability()`, `getStatus()`, `getManaged()`, `getPort()`, `getCustomMeta()` are available. The mapper loads the `Capability` via `CapabilityJpaRepository`.

**Files:**
- Create: `…/capabilities/persistence/CapabilityServicePersistenceMapper.kt`
- Test: `…/src/test/java/.../capabilities/persistence/CapabilityServicePersistenceMapperTest.java`

- [ ] **Step 1: Write the failing test**

```java
package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.SingleHostCapabilityService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CapabilityServicePersistenceMapperTest {

    private final CapabilityJpaRepository capabilityJpaRepository = Mockito.mock(CapabilityJpaRepository.class);
    private final CapabilityServicePersistenceMapper mapper =
            new CapabilityServicePersistenceMapper(capabilityJpaRepository);

    @Test
    public void toEntityThenToDomainRoundTripsSingleHost() {
        UUID serviceId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        Capability capability = new DeploymentCapability();
        capability.setId(UUID.randomUUID());
        capability.setName("Docker");

        HashMap<String, String> config = new HashMap<>();
        config.put("param", "x");
        SingleHostCapabilityService domain = new SingleHostCapabilityService(
                resourceId, serviceId, capability, CapabilityServiceStatus.READY, true, config);
        domain.setPort(8080);

        CapabilityServiceEntity entity = mapper.toEntity(domain);
        assertEquals(serviceId, entity.getId());
        assertEquals(resourceId, entity.getResourceId());
        assertEquals(capability.getId(), entity.getCapabilityId());
        assertEquals(CapabilityServiceStatus.READY, entity.getStatus());
        assertTrue(entity.getManaged());
        assertEquals(8080, entity.getPort());
        assertEquals(CapabilityServiceClass.SINGLE_HOST, entity.getServiceClass());

        Mockito.when(capabilityJpaRepository.findById(capability.getId()))
                .thenReturn(Optional.of(capability));
        SingleHostCapabilityService back = mapper.toSingleHostDomain(entity);
        assertEquals(serviceId, back.getServiceId());
        assertEquals(resourceId, back.getResourceId());
        assertEquals(capability.getId(), back.getCapability().getId());
        assertEquals(CapabilityServiceStatus.READY, back.getStatus());
        assertTrue(back.getManaged());
        assertEquals(8080, back.getPort());
    }
}
```

- [ ] **Step 2: Run, verify FAIL**

Run: `cd /home/operation/Development/slm2 && mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test -Dtest=CapabilityServicePersistenceMapperTest`
Expected: FAIL (mapper missing).

- [ ] **Step 3: Create the mapper**

```kotlin
package org.eclipse.slm.resource_management.features.capabilities.persistence

import org.eclipse.slm.resource_management.features.capabilities.exceptions.CapabilityNotFoundException
import org.eclipse.slm.resource_management.features.capabilities.model.SingleHostCapabilityService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CapabilityServicePersistenceMapper(
    private val capabilityJpaRepository: CapabilityJpaRepository
) {

    fun toEntity(service: SingleHostCapabilityService): CapabilityServiceEntity {
        val entity = CapabilityServiceEntity(service.serviceId)
        entity.resourceId = service.resourceId
        entity.capabilityId = service.capability.id
        entity.status = service.status
        entity.managed = service.managed
        entity.port = service.port
        entity.customMeta = HashMap(service.customMeta)
        entity.serviceClass = CapabilityServiceClass.SINGLE_HOST
        return entity
    }

    fun toSingleHostDomain(entity: CapabilityServiceEntity): SingleHostCapabilityService {
        val capabilityId = entity.capabilityId
            ?: throw CapabilityNotFoundException(entity.id)
        val capability = capabilityJpaRepository.findById(capabilityId)
            .orElseThrow { CapabilityNotFoundException(capabilityId) }
        val service = SingleHostCapabilityService(
            entity.resourceId!!,
            entity.id,
            capability,
            entity.status,
            entity.managed,
            HashMap(entity.customMeta)
        )
        service.port = entity.port
        return service
    }
}
```
If `CapabilityNotFoundException` has no `UUID` constructor, check its definition and use the available constructor (e.g. message string). Verify with `cat .../capabilities/exceptions/CapabilityNotFoundException.*`.

- [ ] **Step 4: Run, verify PASS**

Run: `cd /home/operation/Development/slm2 && mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test -Dtest=CapabilityServicePersistenceMapperTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add resource_management/resource_management.features/resource_management.features.capabilities/src/main/java/org/eclipse/slm/resource_management/features/capabilities/persistence/CapabilityServicePersistenceMapper.kt \
        resource_management/resource_management.features/resource_management.features.capabilities/src/test/java/org/eclipse/slm/resource_management/features/capabilities/persistence/CapabilityServicePersistenceMapperTest.java
git commit -m "feat(capabilities): add CapabilityService persistence mapper"
```

---

## Task 5: SingleHostCapabilityServicePersistence (DB-backed) (TDD)

**Context:** This replaces the storage responsibilities of `SingleHostCapabilitiesConsulClient` for single-host services. It uses `CapabilityServiceJpaRepository`, the mapper, and `AccessControlService` (from `resource_management.common`, already on the classpath of this feature module).

`AccessControlService` API (Part 1): `createSingleObjectPolicy(String name, String subjectGroup, AccessControlObjectType type, UUID objectId)`, `removeObjectFromAllPolicies(AccessControlObjectType type, UUID objectId)`. Object type `CAPABILITY_SERVICE` already exists in the enum.

**Files:**
- Create: `…/capabilities/persistence/SingleHostCapabilityServicePersistence.java`
- Test: `…/src/test/java/.../capabilities/persistence/SingleHostCapabilityServicePersistenceTest.java`

- [ ] **Step 1: Write the failing test**

```java
package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.common.access.AccessControlObjectType;
import org.eclipse.slm.resource_management.common.access.AccessControlService;
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

public class SingleHostCapabilityServicePersistenceTest {

    private final CapabilityServiceJpaRepository repository = Mockito.mock(CapabilityServiceJpaRepository.class);
    private final CapabilityJpaRepository capabilityJpaRepository = Mockito.mock(CapabilityJpaRepository.class);
    private final AccessControlService accessControlService = Mockito.mock(AccessControlService.class);
    private final CapabilityServicePersistenceMapper mapper = new CapabilityServicePersistenceMapper(capabilityJpaRepository);

    private final SingleHostCapabilityServicePersistence persistence =
            new SingleHostCapabilityServicePersistence(repository, capabilityJpaRepository, mapper, accessControlService);

    private Capability capability() {
        Capability c = new DeploymentCapability();
        c.setId(UUID.randomUUID());
        c.setName("Docker");
        return c;
    }

    @Test
    public void addPersistsEntityAndCreatesPolicy() {
        Capability capability = capability();
        UUID resourceId = UUID.randomUUID();
        when(repository.save(any(CapabilityServiceEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(capabilityJpaRepository.findById(capability.getId())).thenReturn(Optional.of(capability));

        var result = persistence.addSingleHostCapability(
                capability, resourceId, CapabilityServiceStatus.INSTALL, true,
                new HashMap<>(), "/Org/CustomerA");

        assertEquals(resourceId, result.getResourceId());
        assertEquals(capability.getId(), result.getCapability().getId());
        verify(repository).save(any(CapabilityServiceEntity.class));
        verify(accessControlService).createSingleObjectPolicy(
                anyString(), eq("/Org/CustomerA"), eq(AccessControlObjectType.CAPABILITY_SERVICE), any(UUID.class));
    }

    @Test
    public void removeDeletesEntityAndCleansPolicy() {
        Capability capability = capability();
        UUID resourceId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        CapabilityServiceEntity entity = new CapabilityServiceEntity(serviceId);
        entity.setResourceId(resourceId);
        entity.setCapabilityId(capability.getId());
        entity.setServiceClass(CapabilityServiceClass.SINGLE_HOST);
        when(repository.findByResourceIdAndCapabilityId(resourceId, capability.getId()))
                .thenReturn(Optional.of(entity));

        persistence.removeSingleHostCapability(capability, resourceId);

        verify(repository).deleteById(serviceId);
        verify(accessControlService).removeObjectFromAllPolicies(AccessControlObjectType.CAPABILITY_SERVICE, serviceId);
    }

    @Test
    public void removeIsNoOpWhenAbsent() {
        Capability capability = capability();
        UUID resourceId = UUID.randomUUID();
        when(repository.findByResourceIdAndCapabilityId(resourceId, capability.getId()))
                .thenReturn(Optional.empty());

        persistence.removeSingleHostCapability(capability, resourceId);

        verify(repository, never()).deleteById(any());
    }

    @Test
    public void getServicesOfResourceReturnsOnlySingleHost() {
        Capability capability = capability();
        UUID resourceId = UUID.randomUUID();
        CapabilityServiceEntity entity = new CapabilityServiceEntity(UUID.randomUUID());
        entity.setResourceId(resourceId);
        entity.setCapabilityId(capability.getId());
        entity.setServiceClass(CapabilityServiceClass.SINGLE_HOST);
        when(repository.findByResourceId(resourceId)).thenReturn(List.of(entity));
        when(capabilityJpaRepository.findById(capability.getId())).thenReturn(Optional.of(capability));

        var services = persistence.getSingleHostCapabilityServicesOfResource(resourceId);

        assertEquals(1, services.size());
        assertEquals(resourceId, services.get(0).getResourceId());
    }
}
```

- [ ] **Step 2: Run, verify FAIL**

Run: `cd /home/operation/Development/slm2 && mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test -Dtest=SingleHostCapabilityServicePersistenceTest`
Expected: FAIL (class missing).

- [ ] **Step 3: Create the persistence component**

```java
package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.common.access.AccessControlObjectType;
import org.eclipse.slm.resource_management.common.access.AccessControlService;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.SingleHostCapabilityService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class SingleHostCapabilityServicePersistence {

    public static final String CAPABILITY_SERVICE_POLICY_PREFIX = "capability-service_";

    private final CapabilityServiceJpaRepository repository;
    private final CapabilityJpaRepository capabilityJpaRepository;
    private final CapabilityServicePersistenceMapper mapper;
    private final AccessControlService accessControlService;

    public SingleHostCapabilityServicePersistence(
            CapabilityServiceJpaRepository repository,
            CapabilityJpaRepository capabilityJpaRepository,
            CapabilityServicePersistenceMapper mapper,
            AccessControlService accessControlService) {
        this.repository = repository;
        this.capabilityJpaRepository = capabilityJpaRepository;
        this.mapper = mapper;
        this.accessControlService = accessControlService;
    }

    public SingleHostCapabilityService addSingleHostCapability(
            Capability capability, UUID resourceId, CapabilityServiceStatus status,
            Boolean isManaged, Map<String, String> configParameter, String fullPathOwnerGroupId) {
        UUID serviceId = UUID.randomUUID();
        SingleHostCapabilityService service = new SingleHostCapabilityService(
                resourceId, serviceId, capability, status, isManaged,
                new java.util.HashMap<>(configParameter));
        CapabilityServiceEntity entity = mapper.toEntity(service);
        repository.save(entity);
        accessControlService.createSingleObjectPolicy(
                CAPABILITY_SERVICE_POLICY_PREFIX + serviceId,
                fullPathOwnerGroupId,
                AccessControlObjectType.CAPABILITY_SERVICE,
                serviceId);
        return mapper.toSingleHostDomain(entity);
    }

    public void updateCapabilityService(SingleHostCapabilityService service) {
        repository.save(mapper.toEntity(service));
    }

    public void removeSingleHostCapability(Capability capability, UUID resourceId) {
        repository.findByResourceIdAndCapabilityId(resourceId, capability.getId())
                .ifPresent(entity -> {
                    repository.deleteById(entity.getId());
                    accessControlService.removeObjectFromAllPolicies(
                            AccessControlObjectType.CAPABILITY_SERVICE, entity.getId());
                });
    }

    public List<SingleHostCapabilityService> getSingleHostCapabilityServicesOfResource(UUID resourceId) {
        return repository.findByResourceId(resourceId).stream()
                .filter(e -> e.getServiceClass() == CapabilityServiceClass.SINGLE_HOST)
                .map(mapper::toSingleHostDomain)
                .toList();
    }

    public SingleHostCapabilityService getCapabilityServiceOfResourceByCapabilityId(UUID capabilityId, UUID resourceId) {
        var entity = repository.findByResourceIdAndCapabilityId(resourceId, capabilityId)
                .orElseThrow(() -> new org.eclipse.slm.resource_management.features.capabilities.exceptions.CapabilityServiceNotFoundException(
                        "Resource[id='" + resourceId + "'] has no capability service for capability[id='" + capabilityId + "']"));
        return mapper.toSingleHostDomain(entity);
    }
}
```
Verify `CapabilityServiceNotFoundException` has a `String` constructor (it did pre-refactor); adjust if not.

- [ ] **Step 4: Run, verify PASS (4 tests)**

Run: `cd /home/operation/Development/slm2 && mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test -Dtest=SingleHostCapabilityServicePersistenceTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add resource_management/resource_management.features/resource_management.features.capabilities/src/main/java/org/eclipse/slm/resource_management/features/capabilities/persistence/SingleHostCapabilityServicePersistence.java \
        resource_management/resource_management.features/resource_management.features.capabilities/src/test/java/org/eclipse/slm/resource_management/features/capabilities/persistence/SingleHostCapabilityServicePersistenceTest.java
git commit -m "feat(capabilities): add DB-backed SingleHost capability service persistence"
```

---

## Task 6: CapabilityServiceQueryService (DB read paths) (TDD)

**Context:** Replaces the read methods of `CapabilitiesConsulClient` (`getCapabilityServices`, `getCapabilityServicesOfResource`, `getCapabilityServicesByCapabilityClass`). Returns domain `CapabilityService` objects. In 2a only SingleHost entities exist in the DB, so all results map via `toSingleHostDomain`; MultiHost mapping is added in Part 2b. Parity note: the old code used the Consul **admin** client (unfiltered), so this service is also unfiltered.

**Files:**
- Create: `…/capabilities/persistence/CapabilityServiceQueryService.java`
- Test: `…/src/test/java/.../capabilities/persistence/CapabilityServiceQueryServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CapabilityServiceQueryServiceTest {

    private final CapabilityServiceJpaRepository repository = Mockito.mock(CapabilityServiceJpaRepository.class);
    private final CapabilityJpaRepository capabilityJpaRepository = Mockito.mock(CapabilityJpaRepository.class);
    private final CapabilityServicePersistenceMapper mapper = new CapabilityServicePersistenceMapper(capabilityJpaRepository);
    private final CapabilityServiceQueryService queryService =
            new CapabilityServiceQueryService(repository, mapper);

    private CapabilityServiceEntity entity(UUID resourceId, Capability capability) {
        CapabilityServiceEntity e = new CapabilityServiceEntity(UUID.randomUUID());
        e.setResourceId(resourceId);
        e.setCapabilityId(capability.getId());
        e.setServiceClass(CapabilityServiceClass.SINGLE_HOST);
        return e;
    }

    @Test
    public void getCapabilityServicesByCapabilityClassMapsResults() {
        Capability capability = new DeploymentCapability();
        capability.setId(UUID.randomUUID());
        UUID resourceId = UUID.randomUUID();
        when(repository.findByCapabilityClass("DeploymentCapability"))
                .thenReturn(List.of(entity(resourceId, capability)));
        when(capabilityJpaRepository.findById(capability.getId())).thenReturn(Optional.of(capability));

        var result = queryService.getCapabilityServicesByCapabilityClass(DeploymentCapability.class);

        assertEquals(1, result.size());
        assertEquals(resourceId, result.get(0).getResourceId());
    }
}
```

- [ ] **Step 2: Run, verify FAIL**

Run: `cd /home/operation/Development/slm2 && mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test -Dtest=CapabilityServiceQueryServiceTest`
Expected: FAIL.

- [ ] **Step 3: Create the query service**

```java
package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CapabilityServiceQueryService {

    private final CapabilityServiceJpaRepository repository;
    private final CapabilityServicePersistenceMapper mapper;

    public CapabilityServiceQueryService(
            CapabilityServiceJpaRepository repository,
            CapabilityServicePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CapabilityService> getCapabilityServices() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    public List<CapabilityService> getCapabilityServicesOfResource(UUID resourceId) {
        return repository.findByResourceId(resourceId).stream()
                .map(this::toDomain)
                .toList();
    }

    public List<CapabilityService> getCapabilityServicesByCapabilityClass(Class capabilityClass) {
        return repository.findByCapabilityClass(capabilityClass.getSimpleName()).stream()
                .map(this::toDomain)
                .toList();
    }

    private CapabilityService toDomain(CapabilityServiceEntity entity) {
        // 2a: only SINGLE_HOST entities are persisted. MULTI_HOST mapping is added in Part 2b.
        return mapper.toSingleHostDomain(entity);
    }
}
```

- [ ] **Step 4: Run, verify PASS**

Run: `cd /home/operation/Development/slm2 && mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test -Dtest=CapabilityServiceQueryServiceTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add resource_management/resource_management.features/resource_management.features.capabilities/src/main/java/org/eclipse/slm/resource_management/features/capabilities/persistence/CapabilityServiceQueryService.java \
        resource_management/resource_management.features/resource_management.features.capabilities/src/test/java/org/eclipse/slm/resource_management/features/capabilities/persistence/CapabilityServiceQueryServiceTest.java
git commit -m "feat(capabilities): add DB-backed capability service query service"
```

---

## Task 7: Switch read consumers to CapabilityServiceQueryService

**Files:**
- Modify: `…/providers/ProviderHandler.java`
- Modify: `…/providers/ServiceHosterHandler.java`
- Modify: `…/CapabilitiesManager.java` (read methods only)

- [ ] **Step 1: ProviderHandler uses the query service**

Replace the `CapabilitiesConsulClient` field/constructor param with `CapabilityServiceQueryService` and the call:
```java
package org.eclipse.slm.resource_management.features.capabilities.providers;

import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityServiceQueryService;

import java.util.ArrayList;
import java.util.List;

public class ProviderHandler {

    protected final CapabilityServiceQueryService capabilityServiceQueryService;
    Class capabilityClass;

    public ProviderHandler(Class capabilityClass, CapabilityServiceQueryService capabilityServiceQueryService) {
        this.capabilityClass = capabilityClass;
        this.capabilityServiceQueryService = capabilityServiceQueryService;
    }

    protected List<Provider> getProvider() {
        List<Provider> provider = new ArrayList<>();
        List<CapabilityService> capabilityServices =
                capabilityServiceQueryService.getCapabilityServicesByCapabilityClass(capabilityClass);
        capabilityServices.forEach(cs -> provider.add(new Provider(cs, capabilityClass)));
        return provider;
    }
}
```

- [ ] **Step 2: ServiceHosterHandler uses the query service**

Change its constructor to inject `CapabilityServiceQueryService` and pass it to `super(...)`, and replace `capabilitiesConsulClient.getCapabilityServicesByCapabilityClass(DeploymentCapability.class)` with `capabilityServiceQueryService.getCapabilityServicesByCapabilityClass(DeploymentCapability.class)`. Remove the `ConsulLoginFailedException` from the `getServiceHosters` signature if it's no longer thrown (and remove the now-unused import). Update the method body's references accordingly (the filtering logic stays identical).

- [ ] **Step 3: CapabilitiesManager read methods use the query service**

In `CapabilitiesManager`, replace the `capabilitiesConsulClient` field with `CapabilityServiceQueryService capabilityServiceQueryService` (constructor param + assignment; remove the `CapabilitiesConsulClient` import). Update:
```java
    public List<CapabilityServiceDTO> getAllCapabilityServices(JwtAuthenticationToken jwtAuthenticationToken) throws ResourceNotFoundException {
        try {
            var capabilityServices = this.capabilityServiceQueryService.getCapabilityServices();
            return CapabilityServiceMapper.INSTANCE.toDtoList(capabilityServices);
        } catch (Exception e) {
            LOG.error("Failed to get all capability services: " + e);
            return List.of();
        }
    }

    public List<CapabilityServiceDTO> getCapabilityServicesOfResource(UUID resourceId) throws ResourceNotFoundException {
        try {
            var capabilityServices = this.capabilityServiceQueryService.getCapabilityServicesOfResource(resourceId);
            return CapabilityServiceMapper.INSTANCE.toDtoList(capabilityServices);
        } catch (Exception e) {
            LOG.error("Failed to get capability services of resource: " + resourceId, e);
            return List.of();
        }
    }
```
Leave `isResourceClusterMember` calling `multiHostCapabilitiesConsulClient` for now (MultiHost still Consul in 2a).

- [ ] **Step 4: Compile**

Run: `cd /home/operation/Development/slm2 && mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities compile`
Expected: FAIL only where `CapabilitiesConsulClient` is still referenced by not-yet-migrated code (its own file, and possibly `MultiHostCapabilitiesConsulClient`/cluster handlers). Proceed to Task 8.

---

## Task 8: Switch write consumers to SingleHostCapabilityServicePersistence; delete single-host Consul code

**Files:**
- Modify: `…/jobs/CapabilityJobServiceImpl.java`, `…/jobs/CapabilityJobExecutor.java`, `…/CapabilitiesManager.java` (`deleteCapability`)
- Delete: `…/persistence/SingleHostCapabilitiesConsulClient.java`, `…/persistence/CapabilitiesConsulClient.java` and their tests.

- [ ] **Step 1: Repoint the job flow**

In `CapabilityJobServiceImpl` and `CapabilityJobExecutor`, replace the injected `SingleHostCapabilitiesConsulClient` with `SingleHostCapabilityServicePersistence` and map the calls:
- `addSingleHostCapabilityToNode(capability, resourceId, status, isManaged, configParameter, fullPathOwnerGroupId)` → `addSingleHostCapability(capability, resourceId, status, isManaged, configParameter, fullPathOwnerGroupId)`.
- `updateCapabilityService(nodeId, capabilityService)` → `updateCapabilityService((SingleHostCapabilityService) capabilityService)` (the new component persists by service id; the `nodeId`/`resourceId` arg is dropped — the service already carries `resourceId`). Confirm the call sites cast/hold a `SingleHostCapabilityService`; if they hold a `CapabilityService`, cast it.
- `getCapabilityServiceOfResourceByCapabilityId(capabilityId, resourceId)` → same name on the new component.
- `removeSingleHostCapabilityFromNode(capability, resourceId)` → `removeSingleHostCapability(capability, resourceId)`.

Read each call site and adjust types minimally. Keep all surrounding job/state-machine logic unchanged.

- [ ] **Step 2: Repoint `CapabilitiesManager.deleteCapability`**

`deleteCapability(Capability)` calls `singleHostCapabilitiesConsulClient.removeCapabilityServiceFromAllConsulNodes(capability)`. Replace with a DB-wide removal: add to `SingleHostCapabilityServicePersistence`:
```java
    public void removeCapabilityServiceFromAllResources(org.eclipse.slm.resource_management.features.capabilities.model.Capability capability) {
        repository.findAll().stream()
                .filter(e -> capability.getId().equals(e.getCapabilityId()))
                .forEach(e -> {
                    repository.deleteById(e.getId());
                    accessControlService.removeObjectFromAllPolicies(
                            org.eclipse.slm.resource_management.common.access.AccessControlObjectType.CAPABILITY_SERVICE, e.getId());
                });
    }
```
and call `this.singleHostCapabilityServicePersistence.removeCapabilityServiceFromAllResources(capability)` in `deleteCapability`. Replace the `singleHostCapabilitiesConsulClient` field in `CapabilitiesManager` accordingly. (Add a one-line test for `removeCapabilityServiceFromAllResources` to `SingleHostCapabilityServicePersistenceTest` mirroring the remove test: stub `repository.findAll()` to return one matching entity, verify `deleteById` + `removeObjectFromAllPolicies`.)

- [ ] **Step 3: Delete the single-host Consul classes + tests**

```bash
cd /home/operation/Development/slm2
git rm resource_management/resource_management.features/resource_management.features.capabilities/src/main/java/org/eclipse/slm/resource_management/features/capabilities/persistence/SingleHostCapabilitiesConsulClient.java \
       resource_management/resource_management.features/resource_management.features.capabilities/src/main/java/org/eclipse/slm/resource_management/features/capabilities/persistence/CapabilitiesConsulClient.java
git rm resource_management/resource_management.features/resource_management.features.capabilities/src/test/java/org/eclipse/slm/resource_management/features/capabilities/SingleHostCapabilitiesConsulClientTest.java \
       resource_management/resource_management.features/resource_management.features.capabilities/src/test/java/org/eclipse/slm/resource_management/features/capabilities/CapabilitiesConsulClientTest.java 2>/dev/null || true
```

- [ ] **Step 4: Fix remaining references (MultiHost bridge)**

`CapabilitiesConsulClient` provided the static `getCapabilityServicePolicyName`/`CAPABILITY_SERVICE_POLICY_PREFIX` and was injected into `MultiHostCapabilitiesConsulClient` and cluster handlers. Find references:
```bash
grep -rn "CapabilitiesConsulClient\|SingleHostCapabilitiesConsulClient" resource_management --include="*.java" | grep -v /target/
```
For MultiHost code that still needs the policy-name helper, move that static helper to the new `SingleHostCapabilityServicePersistence.CAPABILITY_SERVICE_POLICY_PREFIX` is NOT appropriate (different concern). Instead, if `MultiHostCapabilitiesConsulClient` only used `CapabilitiesConsulClient` for `getCapabilityServicesByTag`/the JPA repo, repoint it to `CapabilityServiceQueryService` for reads or leave its Consul logic intact using the consul admin client it already holds. Make the **minimal** change so the module's MAIN compiles; do NOT migrate MultiHost storage here (that's Part 2b). If a MultiHost file needs the old static `getCapabilityServicePolicyName`, add that static method to `MultiHostCapabilitiesConsulClient` itself (it is the only remaining user) and note it for Part 2b.

- [ ] **Step 5: Compile MAIN**

Run: `cd /home/operation/Development/slm2 && mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities compile`
Expected: BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "refactor(capabilities): persist SingleHost capability services in DB, remove single-host Consul code"
```

---

## Task 9: Wiring + module + service-app build verification

**Files:** possibly `…service.app` test/config; otherwise none.

- [ ] **Step 1: Confirm entity/repo are scanned**

The capabilities feature package is already in the app's `@EntityScan`/`@EnableJpaRepositories`/`scanBasePackages` (`org.eclipse.slm.resource_management.features.capabilities`). Confirm by building the service app:
Run: `cd /home/operation/Development/slm2 && mvn -q -am -pl resource_management/resource_management.service/resource_management.service.app -Dmaven.test.skip=true install`
Expected: BUILD SUCCESS.

- [ ] **Step 2: Run the capabilities module test suite**

Run: `cd /home/operation/Development/slm2 && mvn -q -am -pl resource_management/resource_management.features/resource_management.features.capabilities test`
Expected: all green. Fix or delete any test still referencing the deleted Consul classes (migrate behavior to the new components; delete tests that asserted Consul-storage behavior). Note any pre-existing unrelated failures.

- [ ] **Step 3: Verify downstream modules still compile**

Run: `cd /home/operation/Development/slm2 && mvn -q -am -Dmaven.test.skip=true -pl service_management/service_management.service/service_management.service.app,notification_service/notification_service.service/notification_service.service.app install`
Expected: BUILD SUCCESS (service_management consumes capability services via REST; notification_service unaffected).

- [ ] **Step 4: Commit (if test fixes were needed)**

```bash
git add -A
git commit -m "test(capabilities): migrate capability service tests to DB persistence"
```

---

## Self-Review Checklist (completed during planning)

- **Spec coverage (2a portion):** Phase 1 → Task 1. Phase 2 (entity+repo) → Task 3. Phase 3 mapper (without `NodeService` removal) → Task 4. Phase 4 (SingleHost persistence + read paths + access control + remove single-host Consul) → Tasks 5–9. **Deferred to Part 2b:** MultiHost storage, `NodeService` decoupling, `MultiHostCapabilitiesConsulClient` removal, cluster-handler storage migration, `ClusterCreateFunctions` policy-name replacement, final cleanup.
- **Type consistency:** `CapabilityServiceEntity` getters/setters, `CapabilityServiceClass.SINGLE_HOST/MULTI_HOST`, repository methods (`findByResourceId`, `findByServiceClass`, `findByIdIn`, `findByResourceIdAndCapabilityId`, `findByCapabilityClass`), mapper (`toEntity`, `toSingleHostDomain`), `SingleHostCapabilityServicePersistence` (`addSingleHostCapability`, `updateCapabilityService`, `removeSingleHostCapability`, `removeCapabilityServiceFromAllResources`, `getSingleHostCapabilityServicesOfResource`, `getCapabilityServiceOfResourceByCapabilityId`), `CapabilityServiceQueryService` (`getCapabilityServices`, `getCapabilityServicesOfResource`, `getCapabilityServicesByCapabilityClass`), `AccessControlService` (`createSingleObjectPolicy`, `removeObjectFromAllPolicies`), object type `CAPABILITY_SERVICE` — used consistently.
- **Documented deferrals (not placeholders):** `CapabilityServiceQueryService.toDomain` maps everything as SingleHost in 2a because only SingleHost entities are persisted until Part 2b; MultiHost read/write and `NodeService` decoupling are explicitly Part 2b.
- **Parity:** read paths stay unfiltered (Consul admin-client parity); per-service access policy still created on add and cleaned on remove, as the Consul code did.
