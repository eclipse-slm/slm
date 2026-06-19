# Resource Management DB Persistence — Part 1: Access Control + Resources

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace Consul-based storage and ACL access control for **Resources** with database persistence and a new DB-based policy permission system that all later phases (CapabilityServices, RemoteAccess) will reuse.

**Architecture:** A reusable access-control component (`AccessControlPolicy` entity + `AccessControlService`) models the Consul ACL concept in the DB: a policy references the groups it applies to (subjects) and the objects it grants access to (`objectType` + `objectId`). Protected entities hold no policy reference. `BasicResource` becomes a JPA entity persisted via `ResourceJpaRepository`; `ResourcesManagerImpl` uses the repository plus `AccessControlService` for per-group visibility filtering instead of the Consul client.

**Tech Stack:** Java 17 + Kotlin, Spring Boot, Spring Data JPA, MariaDB (Testcontainers in tests), MapStruct, JUnit 5.

**Reference spec:** `docs/superpowers/specs/2026-06-18-resource-mgmt-db-persistence-design.md`

**Scope of this plan:** Spec phases 1, 2, and 3. CapabilityServices (SingleHost/MultiHost) and RemoteAccess are separate follow-up plans on this same foundation.

---

## File Structure

**New files (access-control component, package `org.eclipse.slm.resource_management.common.access`):**
- `…/common/access/AccessControlObjectType.kt` — enum RESOURCE / CAPABILITY_SERVICE / REMOTE_ACCESS
- `…/common/access/AccessControlObjectRef.kt` — `@Embeddable` (objectType, objectId)
- `…/common/access/AccessControlPolicy.kt` — `@Entity` (id, name, description, subjects, objects)
- `…/common/access/AccessControlPolicyRepository.java` — Spring Data repo + query methods
- `…/common/access/UserContext.kt` — (groups, isAdmin) value object
- `…/common/access/AccessControlService.java` — `@Component`, all permission logic
- Tests under `src/test/java/org/eclipse/slm/resource_management/common/access/`

**Modified files (resources):**
- `…/common/resources/BasicResource.kt` — becomes `@Entity`
- `…/common/resources/ResourceJpaRepository.java` — **new**
- `…/common/resources/ResourcesManager.java` — interface signatures: token → `UserContext`
- `…/common/resources/ResourcesManagerImpl.java` — use repository + AccessControlService
- `…/common/resources/ResourcesRestController.java` — build `UserContext`, pass down
- `…/common/utils/keycloak/KeycloakTokenUtil.java` (module `common.utils.keycloak`) — add `getGroups`, `isAdmin`

**Deleted files (resources Consul):**
- `…/common/adapters/ResourcesConsulClient.java`
- `…/common/adapters/ResourcesConsulClientFactory.java`
- `…/common/adapters/ResourcesConsulMapper.java`
- `…/common/adapters/ResourceConsulNode.kt`
- `…/common/adapters/ResourcesConsulClientTest.java` (test)

**Out of scope (untouched here, still Consul-backed):** `RemoteAccessConsulClient`, `CapabilitiesConsulClient`, `SingleHost/MultiHostCapabilitiesConsulClient`. `ResourcesManagerImpl` keeps calling `remoteAccessManager` and `capabilitiesService` as today — those are migrated in later plans.

---

## Task 1: Verify no external module reads Resources directly from Consul

**Files:** none (investigation only)

- [ ] **Step 1: Search the whole repo for direct Consul reads of resource nodes outside resource_management**

Run:
```bash
grep -rn "nodes()\|getNodes\|getNodeById\|ResourceConsulNode\|resourceId" \
  /home/operation/Development/slm2/service_management \
  /home/operation/Development/slm2/platform_management \
  /home/operation/Development/slm2/catalog \
  --include="*.java" --include="*.kt" | grep -i consul
```
Expected: no hits that read resource nodes from Consul. Also confirm cross-module access goes through the REST client `resource_management.service.client`.

- [ ] **Step 2: Record the finding**

If hits are found, STOP and report them to the user before continuing — removing Consul resource storage would break those consumers. If none, note "verified: no external direct Consul resource reads" and proceed. (No commit; investigation only.)

---

## Task 2: AccessControlObjectType enum

**Files:**
- Create: `resource_management/resource_management.common/src/main/java/org/eclipse/slm/resource_management/common/access/AccessControlObjectType.kt`

- [ ] **Step 1: Create the enum**

```kotlin
package org.eclipse.slm.resource_management.common.access

enum class AccessControlObjectType {
    RESOURCE,
    CAPABILITY_SERVICE,
    REMOTE_ACCESS
}
```

- [ ] **Step 2: Commit**

```bash
git add resource_management/resource_management.common/src/main/java/org/eclipse/slm/resource_management/common/access/AccessControlObjectType.kt
git commit -m "feat(access): add AccessControlObjectType enum"
```

---

## Task 3: AccessControlObjectRef embeddable

**Files:**
- Create: `…/common/access/AccessControlObjectRef.kt`

- [ ] **Step 1: Create the embeddable**

```kotlin
package org.eclipse.slm.resource_management.common.access

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.util.UUID

@Embeddable
data class AccessControlObjectRef(

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false)
    var objectType: AccessControlObjectType = AccessControlObjectType.RESOURCE,

    @Column(name = "object_id", nullable = false)
    var objectId: UUID = UUID(0L, 0L)
)
```

(Default values are required so JPA/Kotlin can instantiate the embeddable; they are overwritten on every real use.)

- [ ] **Step 2: Commit**

```bash
git add resource_management/resource_management.common/src/main/java/org/eclipse/slm/resource_management/common/access/AccessControlObjectRef.kt
git commit -m "feat(access): add AccessControlObjectRef embeddable"
```

---

## Task 4: AccessControlPolicy entity

**Files:**
- Create: `…/common/access/AccessControlPolicy.kt`

- [ ] **Step 1: Create the entity**

```kotlin
package org.eclipse.slm.resource_management.common.access

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import java.util.UUID

@Entity
class AccessControlPolicy(id: UUID? = null) {

    constructor() : this(null)

    @Id
    @Column(name = "uuid", length = 36, unique = true, nullable = false)
    var id: UUID = id ?: UUID.randomUUID()

    @Column(name = "name")
    var name: String = ""

    @Column(name = "description")
    var description: String = ""

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "policy_subjects",
        joinColumns = [JoinColumn(name = "policy_id")]
    )
    @Column(name = "subject")
    var subjects: MutableSet<String> = mutableSetOf()

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "policy_objects",
        joinColumns = [JoinColumn(name = "policy_id")]
    )
    var objects: MutableSet<AccessControlObjectRef> = mutableSetOf()
}
```

- [ ] **Step 2: Commit**

```bash
git add resource_management/resource_management.common/src/main/java/org/eclipse/slm/resource_management/common/access/AccessControlPolicy.kt
git commit -m "feat(access): add AccessControlPolicy entity"
```

---

## Task 5: AccessControlPolicyRepository

**Files:**
- Create: `…/common/access/AccessControlPolicyRepository.java`
- Test: `…/test/java/org/eclipse/slm/resource_management/common/access/AccessControlPolicyRepositoryTest.java`

- [ ] **Step 1: Write the failing test**

```java
package org.eclipse.slm.resource_management.common.access;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccessControlPolicyRepositoryTest {

    @Autowired
    private AccessControlPolicyRepository repository;

    @Test
    public void findAccessibleObjectIdsReturnsOnlyMatchingGroupAndType() {
        UUID resourceId = UUID.randomUUID();

        AccessControlPolicy policy = new AccessControlPolicy();
        policy.setName("resource_" + resourceId);
        policy.getSubjects().add("/Org/CustomerA");
        policy.getObjects().add(
            new AccessControlObjectRef(AccessControlObjectType.RESOURCE, resourceId));
        repository.save(policy);

        Set<UUID> accessibleForA = repository.findAccessibleObjectIds(
            Set.of("/Org/CustomerA"), AccessControlObjectType.RESOURCE);
        Set<UUID> accessibleForB = repository.findAccessibleObjectIds(
            Set.of("/Org/CustomerB"), AccessControlObjectType.RESOURCE);
        Set<UUID> accessibleWrongType = repository.findAccessibleObjectIds(
            Set.of("/Org/CustomerA"), AccessControlObjectType.REMOTE_ACCESS);

        assertEquals(Set.of(resourceId), accessibleForA);
        assertTrue(accessibleForB.isEmpty());
        assertTrue(accessibleWrongType.isEmpty());
    }

    @Test
    public void findByObjectReturnsPoliciesReferencingObject() {
        UUID resourceId = UUID.randomUUID();
        AccessControlPolicy policy = new AccessControlPolicy();
        policy.getObjects().add(
            new AccessControlObjectRef(AccessControlObjectType.RESOURCE, resourceId));
        repository.save(policy);

        var found = repository.findByObject(AccessControlObjectType.RESOURCE, resourceId);

        assertEquals(1, found.size());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.common test -Dtest=AccessControlPolicyRepositoryTest`
Expected: FAIL — `AccessControlPolicyRepository` does not exist (compilation error).

- [ ] **Step 3: Create the repository**

```java
package org.eclipse.slm.resource_management.common.access;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface AccessControlPolicyRepository extends JpaRepository<AccessControlPolicy, UUID> {

    @Query("SELECT o.objectId FROM AccessControlPolicy p " +
           "JOIN p.objects o JOIN p.subjects s " +
           "WHERE s IN :groups AND o.objectType = :objectType")
    Set<UUID> findAccessibleObjectIds(
            @Param("groups") Set<String> groups,
            @Param("objectType") AccessControlObjectType objectType);

    @Query("SELECT p FROM AccessControlPolicy p JOIN p.objects o " +
           "WHERE o.objectType = :objectType AND o.objectId = :objectId")
    List<AccessControlPolicy> findByObject(
            @Param("objectType") AccessControlObjectType objectType,
            @Param("objectId") UUID objectId);

    @Query("SELECT COUNT(p) FROM AccessControlPolicy p " +
           "JOIN p.objects o JOIN p.subjects s " +
           "WHERE s IN :groups AND o.objectType = :objectType AND o.objectId = :objectId")
    long countAccessGranting(
            @Param("groups") Set<String> groups,
            @Param("objectType") AccessControlObjectType objectType,
            @Param("objectId") UUID objectId);
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.common test -Dtest=AccessControlPolicyRepositoryTest`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add resource_management/resource_management.common/src/main/java/org/eclipse/slm/resource_management/common/access/AccessControlPolicyRepository.java \
        resource_management/resource_management.common/src/test/java/org/eclipse/slm/resource_management/common/access/AccessControlPolicyRepositoryTest.java
git commit -m "feat(access): add AccessControlPolicyRepository with access queries"
```

---

## Task 6: UserContext value object

**Files:**
- Create: `…/common/access/UserContext.kt`

- [ ] **Step 1: Create the value object**

```kotlin
package org.eclipse.slm.resource_management.common.access

data class UserContext(
    val groups: Set<String>,
    val isAdmin: Boolean
)
```

- [ ] **Step 2: Commit**

```bash
git add resource_management/resource_management.common/src/main/java/org/eclipse/slm/resource_management/common/access/UserContext.kt
git commit -m "feat(access): add UserContext value object"
```

---

## Task 7: KeycloakTokenUtil.getGroups + isAdmin

**Files:**
- Modify: `common/common.utils/common.utils.keycloak/src/main/java/org/eclipse/slm/common/utils/keycloak/KeycloakTokenUtil.java`
- Test: `common/common.utils/common.utils.keycloak/src/test/java/org/eclipse/slm/common/utils/keycloak/KeycloakTokenUtilTest.java`

- [ ] **Step 1: Write the failing test**

```java
package org.eclipse.slm.common.utils.keycloak;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class KeycloakTokenUtilTest {

    private JwtAuthenticationToken tokenWith(Map<String, Object> claims, String... roles) {
        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                claims.isEmpty() ? Map.of("sub", "user") : claims);
        var authorities = java.util.Arrays.stream(roles)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .map(a -> (org.springframework.security.core.GrantedAuthority) a)
                .toList();
        return new JwtAuthenticationToken(jwt, authorities);
    }

    @Test
    public void getGroupsReturnsGroupsClaim() {
        var token = tokenWith(Map.of("sub", "user", "groups", List.of("/Org/CustomerA", "/Org/CustomerB")));

        Set<String> groups = KeycloakTokenUtil.getGroups(token);

        assertEquals(Set.of("/Org/CustomerA", "/Org/CustomerB"), groups);
    }

    @Test
    public void getGroupsReturnsEmptySetWhenClaimMissing() {
        var token = tokenWith(Map.of("sub", "user"));

        assertTrue(KeycloakTokenUtil.getGroups(token).isEmpty());
    }

    @Test
    public void isAdminTrueWhenSlmAdminRolePresent() {
        var token = tokenWith(Map.of("sub", "user"), "slm-admin");

        assertTrue(KeycloakTokenUtil.isAdmin(token));
    }

    @Test
    public void isAdminFalseWhenOnlyUserRole() {
        var token = tokenWith(Map.of("sub", "user"), "slm-user");

        assertFalse(KeycloakTokenUtil.isAdmin(token));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl common/common.utils/common.utils.keycloak test -Dtest=KeycloakTokenUtilTest`
Expected: FAIL — `getGroups` / `isAdmin` not defined.

- [ ] **Step 3: Add the methods to KeycloakTokenUtil**

Add these imports near the top of `KeycloakTokenUtil.java`:
```java
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
```

Add these methods inside the class:
```java
    public static final String ROLE_SLM_ADMIN = "ROLE_slm-admin";

    @SuppressWarnings("unchecked")
    public static Set<String> getGroups(JwtAuthenticationToken jwtAuthenticationToken) {
        var claim = jwtAuthenticationToken.getToken().getClaim("groups");
        if (claim instanceof Collection<?> collection) {
            return collection.stream().map(Object::toString).collect(Collectors.toSet());
        }
        return Set.of();
    }

    public static boolean isAdmin(JwtAuthenticationToken jwtAuthenticationToken) {
        return jwtAuthenticationToken.getAuthorities().stream()
                .anyMatch(a -> ROLE_SLM_ADMIN.equals(a.getAuthority()));
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl common/common.utils/common.utils.keycloak test -Dtest=KeycloakTokenUtilTest`
Expected: PASS (4 tests).

- [ ] **Step 5: Commit**

```bash
git add common/common.utils/common.utils.keycloak/src/main/java/org/eclipse/slm/common/utils/keycloak/KeycloakTokenUtil.java \
        common/common.utils/common.utils.keycloak/src/test/java/org/eclipse/slm/common/utils/keycloak/KeycloakTokenUtilTest.java
git commit -m "feat(keycloak): add getGroups and isAdmin token helpers"
```

---

## Task 8: AccessControlService — createSingleObjectPolicy + hasAccess

**Files:**
- Create: `…/common/access/AccessControlService.java`
- Test: `…/test/java/org/eclipse/slm/resource_management/common/access/AccessControlServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
package org.eclipse.slm.resource_management.common.access;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AccessControlService.class)
public class AccessControlServiceTest {

    @Autowired
    private AccessControlService accessControlService;

    private final UserContext customerA = new UserContext(Set.of("/Org/CustomerA"), false);
    private final UserContext customerB = new UserContext(Set.of("/Org/CustomerB"), false);
    private final UserContext admin = new UserContext(Set.of(), true);

    @Test
    public void ownerHasAccessAfterCreateSingleObjectPolicy() {
        UUID resourceId = UUID.randomUUID();
        accessControlService.createSingleObjectPolicy(
            "resource_" + resourceId, "/Org/CustomerA",
            AccessControlObjectType.RESOURCE, resourceId);

        assertTrue(accessControlService.hasAccess(
            AccessControlObjectType.RESOURCE, resourceId, customerA));
        assertFalse(accessControlService.hasAccess(
            AccessControlObjectType.RESOURCE, resourceId, customerB));
    }

    @Test
    public void adminAlwaysHasAccess() {
        UUID resourceId = UUID.randomUUID();

        assertTrue(accessControlService.hasAccess(
            AccessControlObjectType.RESOURCE, resourceId, admin));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.common test -Dtest=AccessControlServiceTest`
Expected: FAIL — `AccessControlService` not defined.

- [ ] **Step 3: Create AccessControlService (initial methods)**

```java
package org.eclipse.slm.resource_management.common.access;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class AccessControlService {

    private final AccessControlPolicyRepository policyRepository;

    public AccessControlService(AccessControlPolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    public AccessControlPolicy createSingleObjectPolicy(
            String name, String subjectGroup,
            AccessControlObjectType objectType, UUID objectId) {
        AccessControlPolicy policy = new AccessControlPolicy();
        policy.setName(name);
        policy.getSubjects().add(subjectGroup);
        policy.getObjects().add(new AccessControlObjectRef(objectType, objectId));
        return policyRepository.save(policy);
    }

    public boolean hasAccess(
            AccessControlObjectType objectType, UUID objectId, UserContext userContext) {
        if (userContext.isAdmin()) {
            return true;
        }
        if (userContext.getGroups().isEmpty()) {
            return false;
        }
        return policyRepository.countAccessGranting(
                userContext.getGroups(), objectType, objectId) > 0;
    }

    /**
     * @return Optional.empty() for admins (= no filtering / all objects accessible),
     *         otherwise the set of object ids the user's groups may access.
     */
    public Optional<Set<UUID>> getAccessibleObjectIds(
            UserContext userContext, AccessControlObjectType objectType) {
        if (userContext.isAdmin()) {
            return Optional.empty();
        }
        if (userContext.getGroups().isEmpty()) {
            return Optional.of(Set.of());
        }
        return Optional.of(policyRepository.findAccessibleObjectIds(
                userContext.getGroups(), objectType));
    }

    public void removeObjectFromAllPolicies(
            AccessControlObjectType objectType, UUID objectId) {
        var policies = policyRepository.findByObject(objectType, objectId);
        for (AccessControlPolicy policy : policies) {
            policy.getObjects().removeIf(
                ref -> ref.getObjectType() == objectType && ref.getObjectId().equals(objectId));
            if (policy.getObjects().isEmpty()) {
                policyRepository.delete(policy);
            } else {
                policyRepository.save(policy);
            }
        }
    }
}
```

Note for the Kotlin getters used from Java: `UserContext` exposes `isAdmin()` and `getGroups()`; `AccessControlObjectRef` exposes `getObjectType()` / `getObjectId()`.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.common test -Dtest=AccessControlServiceTest`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add resource_management/resource_management.common/src/main/java/org/eclipse/slm/resource_management/common/access/AccessControlService.java \
        resource_management/resource_management.common/src/test/java/org/eclipse/slm/resource_management/common/access/AccessControlServiceTest.java
git commit -m "feat(access): add AccessControlService (create/hasAccess/filter)"
```

---

## Task 9: AccessControlService — filtering + cleanup tests

**Files:**
- Test: `…/test/java/org/eclipse/slm/resource_management/common/access/AccessControlServiceTest.java` (extend)

- [ ] **Step 1: Add tests for getAccessibleObjectIds and removeObjectFromAllPolicies**

Add these methods to `AccessControlServiceTest`:
```java
    @Test
    public void getAccessibleObjectIdsFiltersByGroup() {
        UUID resourceA = UUID.randomUUID();
        UUID resourceB = UUID.randomUUID();
        accessControlService.createSingleObjectPolicy(
            "resource_" + resourceA, "/Org/CustomerA", AccessControlObjectType.RESOURCE, resourceA);
        accessControlService.createSingleObjectPolicy(
            "resource_" + resourceB, "/Org/CustomerB", AccessControlObjectType.RESOURCE, resourceB);

        var accessibleForA = accessControlService.getAccessibleObjectIds(
            customerA, AccessControlObjectType.RESOURCE);

        assertTrue(accessibleForA.isPresent());
        assertEquals(Set.of(resourceA), accessibleForA.get());
    }

    @Test
    public void getAccessibleObjectIdsEmptyForAdmin() {
        var accessibleForAdmin = accessControlService.getAccessibleObjectIds(
            admin, AccessControlObjectType.RESOURCE);

        assertTrue(accessibleForAdmin.isEmpty());
    }

    @Test
    public void removeObjectDeletesEmptyPolicy() {
        UUID resourceId = UUID.randomUUID();
        accessControlService.createSingleObjectPolicy(
            "resource_" + resourceId, "/Org/CustomerA", AccessControlObjectType.RESOURCE, resourceId);

        accessControlService.removeObjectFromAllPolicies(
            AccessControlObjectType.RESOURCE, resourceId);

        assertFalse(accessControlService.hasAccess(
            AccessControlObjectType.RESOURCE, resourceId, customerA));
        assertTrue(accessControlService.getAccessibleObjectIds(
            customerA, AccessControlObjectType.RESOURCE).get().isEmpty());
    }
```

- [ ] **Step 2: Run tests**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.common test -Dtest=AccessControlServiceTest`
Expected: PASS (5 tests). No production change needed (methods already implemented in Task 8).

- [ ] **Step 3: Commit**

```bash
git add resource_management/resource_management.common/src/test/java/org/eclipse/slm/resource_management/common/access/AccessControlServiceTest.java
git commit -m "test(access): cover group filtering and policy cleanup"
```

---

## Task 10: Convert BasicResource to a JPA entity

**Files:**
- Modify: `…/common/resources/BasicResource.kt`
- Test: `…/test/java/org/eclipse/slm/resource_management/common/resources/ResourceJpaRepositoryTest.java`
- Create: `…/common/resources/ResourceJpaRepository.java`

- [ ] **Step 1: Write the failing repository test**

```java
package org.eclipse.slm.resource_management.common.resources;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ResourceJpaRepositoryTest {

    @Autowired
    private ResourceJpaRepository resourceJpaRepository;

    @Test
    public void saveAndLoadResourcePreservesFields() {
        UUID id = UUID.randomUUID();
        BasicResource resource = new BasicResource(id, "host-1", "10.0.0.1");
        resource.setAssetId("asset-1");
        resource.setFirmwareVersion("1.2.3");
        resource.setDriverId("driver-1");

        resourceJpaRepository.save(resource);

        Optional<BasicResource> loaded = resourceJpaRepository.findById(id);
        assertTrue(loaded.isPresent());
        assertEquals("host-1", loaded.get().getHostname());
        assertEquals("10.0.0.1", loaded.get().getIp());
        assertEquals("asset-1", loaded.get().getAssetId());
        assertEquals("1.2.3", loaded.get().getFirmwareVersion());
        assertEquals("driver-1", loaded.get().getDriverId());
    }

    @Test
    public void findByIdInReturnsOnlyRequested() {
        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();
        resourceJpaRepository.save(new BasicResource(idA, "a", "10.0.0.1"));
        resourceJpaRepository.save(new BasicResource(idB, "b", "10.0.0.2"));

        List<BasicResource> result = resourceJpaRepository.findByIdIn(Set.of(idA));

        assertEquals(1, result.size());
        assertEquals(idA, result.get(0).getId());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.common test -Dtest=ResourceJpaRepositoryTest`
Expected: FAIL — `ResourceJpaRepository` missing and `BasicResource` not an entity.

- [ ] **Step 3: Add JPA annotations to BasicResource**

Replace the contents of `BasicResource.kt` with:
```kotlin
package org.eclipse.slm.resource_management.common.resources

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Transient
import java.util.*

@Entity
class BasicResource
(
    @field:JsonProperty("id")
    @Id
    @Column(name = "uuid", length = 36, unique = true, nullable = false)
    var id: UUID
)
{
    constructor() : this(UUID.randomUUID())

    @Column(name = "asset_id")
    var assetId: String? = null

    @Column(name = "location_id")
    var locationId: UUID? = null

    @Column(name = "ip")
    var ip: String? = null

    @Column(name = "hostname")
    var hostname: String? = null

    @Column(name = "firmware_version")
    var firmwareVersion: String? = null

    @Transient
    var clusterMember = false

    @Transient
    var capabilityServiceIds: List<UUID> = emptyList()

    @Transient
    var remoteAccessIds: List<UUID> = emptyList()

    @Column(name = "driver_id")
    var driverId: String? = null

    constructor(id: UUID, hostname: String, ip: String) : this(id) {
        this.hostname = hostname
        this.ip = ip
    }
}
```

- [ ] **Step 4: Create the repository**

```java
package org.eclipse.slm.resource_management.common.resources;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ResourceJpaRepository extends JpaRepository<BasicResource, UUID> {

    List<BasicResource> findByIdIn(Set<UUID> ids);
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.common test -Dtest=ResourceJpaRepositoryTest`
Expected: PASS (2 tests).

- [ ] **Step 6: Commit**

```bash
git add resource_management/resource_management.common/src/main/java/org/eclipse/slm/resource_management/common/resources/BasicResource.kt \
        resource_management/resource_management.common/src/main/java/org/eclipse/slm/resource_management/common/resources/ResourceJpaRepository.java \
        resource_management/resource_management.common/src/test/java/org/eclipse/slm/resource_management/common/resources/ResourceJpaRepositoryTest.java
git commit -m "feat(resources): persist BasicResource via JPA repository"
```

---

## Task 11: Change ResourcesManager interface to use UserContext

**Files:**
- Modify: `…/common/resources/ResourcesManager.java`

- [ ] **Step 1: Inspect the current interface**

Run: `sed -n '1,80p' resource_management/resource_management.common/src/main/java/org/eclipse/slm/resource_management/common/resources/ResourcesManager.java`
Expected: methods currently take `String jwtAccessToken`.

- [ ] **Step 2: Replace `String jwtAccessToken` parameters with `UserContext userContext`**

For every method that currently accepts `String jwtAccessToken`, change the parameter to `UserContext userContext` (keep all other parameters). Add the import:
```java
import org.eclipse.slm.resource_management.common.access.UserContext;
```
Example resulting signatures:
```java
List<BasicResource> getResources(UserContext userContext) throws ResourceNotFoundException, ResourceRuntimeException;

Optional<BasicResource> getResourceById(UUID resourceId, UserContext userContext) throws ResourceRuntimeException;

BasicResource getResourceByIdOrThrow(UUID resourceId, UserContext userContext) throws ResourceRuntimeException, ResourceNotFoundException;

void deleteResource(UUID resourceId, UserContext userContext) throws ResourceNotFoundException, ResourceRuntimeException;

void setLocationOfResource(UUID resourceId, UUID locationId, UserContext userContext);

void updateResource(UUID resourceId, ResourceUpdateRequest updateResourceRequest, UserContext userContext) throws ResourceNotFoundException, ResourceRuntimeException;
```
Leave `createResource(...)`, `setConnectionParametersOfResource(...)`, `getConnectionParametersOfResource(...)`, `setFirmwareVersionOfResource(...)` signatures unchanged (they do not currently take a token).

- [ ] **Step 3: Compile (will fail in ResourcesManagerImpl — expected, fixed next task)**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.common compile`
Expected: FAIL — `ResourcesManagerImpl` and `ResourcesRestController` no longer match the interface. This is expected; do **not** commit yet. Proceed to Task 12.

---

## Task 12: Rewrite ResourcesManagerImpl to use DB + AccessControlService

**Files:**
- Modify: `…/common/resources/ResourcesManagerImpl.java`

- [ ] **Step 1: Replace Consul fields with repository + access service**

In the field/constructor section, remove `resourcesConsulClientFactory`, `resourcesConsulAdminClient` and inject the new collaborators. New field block:
```java
    private final ResourceJpaRepository resourceJpaRepository;
    private final AccessControlService accessControlService;
    private final ResourcesVaultClient resourcesVaultClient;
    private final Optional<ICapabilitiesManager> capabilitiesService;
    private final LocationJpaRepository locationJpaRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ResourcesAasHandler resourcesAasHandler;
    private final ResourceEventMessageSender resourceEventMessageSender;
    private final RemoteAccessManager remoteAccessManager;

    @Autowired
    public ResourcesManagerImpl(
            ResourceJpaRepository resourceJpaRepository,
            AccessControlService accessControlService,
            ResourcesVaultClient resourcesVaultClient,
            Optional<ICapabilitiesManager> capabilitiesService,
            LocationJpaRepository locationJpaRepository,
            ApplicationEventPublisher applicationEventPublisher,
            ResourcesAasHandler resourcesAasHandler,
            ResourceEventMessageSender resourceEventMessageSender,
            RemoteAccessManager remoteAccessManager
    ) {
        this.resourceJpaRepository = resourceJpaRepository;
        this.accessControlService = accessControlService;
        this.resourcesVaultClient = resourcesVaultClient;
        this.capabilitiesService = capabilitiesService;
        this.locationJpaRepository = locationJpaRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.resourcesAasHandler = resourcesAasHandler;
        this.resourceEventMessageSender = resourceEventMessageSender;
        this.remoteAccessManager = remoteAccessManager;
        this.remoteAccessManager.registerResourceUpdatedListener(this);
    }
```
Add imports:
```java
import org.eclipse.slm.resource_management.common.access.AccessControlObjectType;
import org.eclipse.slm.resource_management.common.access.AccessControlService;
import org.eclipse.slm.resource_management.common.access.UserContext;
```
Remove imports of `ResourcesConsulClient`, `ResourcesConsulClientFactory`.

- [ ] **Step 2: Rewrite the read methods**

```java
    @Override
    public List<BasicResource> getResources(UserContext userContext)
            throws ResourceNotFoundException, ResourceRuntimeException {
        try {
            var accessibleIds = accessControlService.getAccessibleObjectIds(
                    userContext, AccessControlObjectType.RESOURCE);
            List<BasicResource> resources = accessibleIds
                    .map(resourceJpaRepository::findByIdIn)
                    .orElseGet(resourceJpaRepository::findAll);

            for (var resource : resources) {
                this.addDetailsToResource(resource, userContext);
            }
            resources.removeIf(r -> r.getIp() != null && r.getIp().contains("-cluster"));
            return resources;
        } catch (Exception e) {
            LOG.error("Failed to get resources: {}", e.getMessage(), e);
            throw new ResourceRuntimeException("Failed to get resources:" + e.getMessage(), e);
        }
    }

    @Override
    public Optional<BasicResource> getResourceById(UUID resourceId, UserContext userContext)
            throws ResourceRuntimeException {
        try {
            return Optional.of(this.getResourceByIdOrThrow(resourceId, userContext));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public BasicResource getResourceByIdOrThrow(UUID resourceId, UserContext userContext)
            throws ResourceRuntimeException, ResourceNotFoundException {
        if (!accessControlService.hasAccess(
                AccessControlObjectType.RESOURCE, resourceId, userContext)) {
            throw new ResourceNotFoundException(resourceId);
        }
        var resource = resourceJpaRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(resourceId));
        return this.addDetailsToResource(resource, userContext);
    }
```

- [ ] **Step 3: Update `addDetailsToResource` signature (token → userContext)**

The method currently takes `String jwtAccessToken` and passes it to `remoteAccessManager.getRemoteAccessIdsOfResource(resource.getId(), jwtAccessToken)`. RemoteAccess is still Consul-backed in this phase and still needs a JWT string; since we no longer thread the raw token, pass an empty string for now (admin Consul client path is used internally by RemoteAccess). Replace with:
```java
    private BasicResource addDetailsToResource(BasicResource resource, UserContext userContext) {
        if (this.capabilitiesService.isPresent()) {
            var capabilityServicesIds =
                    this.capabilitiesService.get().getCapabilityServiceIdsOfResource(resource.getId());
            resource.setCapabilityServiceIds(capabilityServicesIds);

            var isClusterMember =
                    this.capabilitiesService.get().isResourceClusterMember(resource.getId());
            resource.setClusterMember(isClusterMember);
        }

        var remoteAccessServicesIds =
                this.remoteAccessManager.getRemoteAccessIdsOfResource(resource.getId(), "");
        resource.setRemoteAccessIds(remoteAccessServicesIds);
        return resource;
    }
```
NOTE: When RemoteAccess is migrated to DB (later plan), the `""` argument and the JWT parameter on `getRemoteAccessIdsOfResource` are removed. This is a documented temporary seam, not a placeholder.

- [ ] **Step 4: Rewrite createResource persistence + policy**

Replace the Consul block (`resource = this.resourcesConsulAdminClient.addResource(resource, fullPathOwnerGroupId);`) with:
```java
            resource = this.resourceJpaRepository.save(resource);
            this.accessControlService.createSingleObjectPolicy(
                    "resource_" + resource.getId(),
                    fullPathOwnerGroupId,
                    AccessControlObjectType.RESOURCE,
                    resource.getId());
```
Leave the surrounding vault/AAS/event-sender calls unchanged.

- [ ] **Step 5: Rewrite deleteResource**

```java
    @Override
    public void deleteResource(UUID resourceId, UserContext userContext)
            throws ResourceNotFoundException, ResourceRuntimeException {
        try {
            var resource = this.getResourceByIdOrThrow(resourceId, userContext);

            for (var remoteAccessServiceId : resource.getRemoteAccessIds()) {
                this.remoteAccessManager.deleteRemoteAccessById(
                        resourceId, remoteAccessServiceId, "", false);
            }

            this.resourceJpaRepository.deleteById(resourceId);
            this.accessControlService.removeObjectFromAllPolicies(
                    AccessControlObjectType.RESOURCE, resourceId);
            this.resourcesVaultClient.removeSecretsForResource(resource.getId());
            this.resourcesVaultClient.removeIntermediateCertificateAuthority(resource.getId());

            this.resourceEventMessageSender.sendMessage(resource, ResourceEventType.DELETED);
            this.applicationEventPublisher.publishEvent(
                    new ResourceEvent(this, resourceId, ResourceEvent.Operation.DELETE));
        } catch (ShellNotFoundException ignored) {
        } catch (Exception e) {
            throw new ResourceRuntimeException("Failed to delete resource: " + e.getMessage(), e);
        }
    }
```

- [ ] **Step 6: Rewrite setLocationOfResource, setFirmwareVersionOfResource, updateResource**

```java
    @Override
    public void setLocationOfResource(UUID resourceId, UUID locationId, UserContext userContext) {
        this.getResourceByIdOrThrow(resourceId, userContext);
        var location = locationJpaRepository.findById(locationId).orElseThrow();
        var resource = resourceJpaRepository.findById(resourceId).orElseThrow();
        resource.setLocationId(location.getId());
        resourceJpaRepository.save(resource);
    }

    @Override
    public void setFirmwareVersionOfResource(UUID resourceId, String firmwareVersion) {
        resourceJpaRepository.findById(resourceId).ifPresentOrElse(
            (resource) -> {
                resource.setFirmwareVersion(firmwareVersion);
                resourceJpaRepository.save(resource);
            },
            () -> LOG.error("Resource with id: " + resourceId
                    + " not found. Cannot set firmware version."));
    }
```
In `updateResource`, replace the final Consul block:
```java
        var updateRequired = false;
        if (updateResourceRequest.getHostname() != null) {
            resource.setHostname(updateResourceRequest.getHostname());
            updateRequired = true;
        }
        if (updateResourceRequest.getIp() != null) {
            resource.setIp(updateResourceRequest.getIp());
            updateRequired = true;
        }
        if (updateRequired) {
            this.resourceJpaRepository.save(resource);
        }

        this.onResourceUpdated(resourceId, userContext);
```
Change the method signature to `updateResource(UUID resourceId, ResourceUpdateRequest updateResourceRequest, UserContext userContext)` and the first line to `var resource = this.getResourceByIdOrThrow(resourceId, userContext);`.

- [ ] **Step 7: Update onResourceUpdated**

`onResourceUpdated` is part of `ResourceUpdatedListener`. It currently takes `(UUID resourceId, String jwtAccessToken)`. Change the listener and this method to use `UserContext`:
```java
    @Override
    public void onResourceUpdated(UUID resourceId, UserContext userContext) {
        var resource = this.getResourceByIdOrThrow(resourceId, userContext);
        this.resourceEventMessageSender.sendMessage(resource, ResourceEventType.UPDATED);
    }
```
Update the `ResourceUpdatedListener` interface signature accordingly (`…/common/resources/ResourceUpdatedListener.java`): `void onResourceUpdated(UUID resourceId, UserContext userContext);` and add the `UserContext` import.

- [ ] **Step 8: Check RemoteAccessManager listener call site compatibility**

Run: `grep -rn "onResourceUpdated\|ResourceUpdatedListener" resource_management/resource_management.common/src/main/java`
If `RemoteAccessManagerImpl` invokes `onResourceUpdated(resourceId, jwtToken)`, change that call to build a `UserContext` — for the internal listener path use an admin context: `new UserContext(java.util.Set.of(), true)`. Apply the minimal change needed to compile.

- [ ] **Step 9: Compile**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.common compile`
Expected: FAIL only in `ResourcesRestController` (interface changed). Proceed to Task 13.

---

## Task 13: Update ResourcesRestController to build UserContext

**Files:**
- Modify: `…/common/resources/ResourcesRestController.java`

- [ ] **Step 1: Add a private helper to build UserContext from the security context**

Add imports:
```java
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.resource_management.common.access.UserContext;
```
Add helper method:
```java
    private UserContext currentUserContext() {
        var jwtAuthenticationToken =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return new UserContext(
                KeycloakTokenUtil.getGroups(jwtAuthenticationToken),
                KeycloakTokenUtil.isAdmin(jwtAuthenticationToken));
    }
```

- [ ] **Step 2: Replace token plumbing in each endpoint**

In `getResources`, `getResource`, `deleteResource`, `setLocationOfResource`, `updateResource`: remove the `accessToken` extraction and pass `currentUserContext()` where the manager method now expects a `UserContext`. Example:
```java
    @Override
    public ResponseEntity<List<ResourceDTO>> getResources() throws ResourceNotFoundException {
        var resources = this.resourcesManager.getResources(currentUserContext());
        var resourceDTOs = ResourceMapper.INSTANCE.toDto(resources);
        return ResponseEntity.ok(resourceDTOs);
    }

    @Override
    public ResponseEntity<ResourceDTO> getResource(UUID resourceId) throws ResourceNotFoundException {
        var resource = this.resourcesManager.getResourceByIdOrThrow(resourceId, currentUserContext());
        return ResponseEntity.ok(ResourceMapper.INSTANCE.toDto(resource));
    }

    @Override
    public ResponseEntity<Void> deleteResource(UUID resourceId) {
        this.resourcesManager.deleteResource(resourceId, currentUserContext());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> setLocationOfResource(UUID resourceId, UUID locationId) {
        this.resourcesManager.setLocationOfResource(resourceId, locationId, currentUserContext());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> updateResource(UUID resourceId, ResourceUpdateRequest updateResourceRequest) {
        this.resourcesManager.updateResource(resourceId, updateResourceRequest, currentUserContext());
        return ResponseEntity.ok().build();
    }
```
Leave `addExistingResource`, `addExistingResourceWithId`, `setConnectionParametersOfResource` as they are (no token parameter changes).

- [ ] **Step 3: Compile**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.common compile`
Expected: FAIL — remaining references to deleted Consul classes (`ResourcesConsulClient*`, `ResourceConsulNode`) from their own files and any other callers. Proceed to Task 14.

---

## Task 14: Delete Consul resource classes and fix remaining references

**Files:**
- Delete: `…/common/adapters/ResourcesConsulClient.java`, `ResourcesConsulClientFactory.java`, `ResourcesConsulMapper.java`, `ResourceConsulNode.kt`
- Delete: `…/test/java/org/eclipse/slm/resource_management/common/adapters/ResourcesConsulClientTest.java`

- [ ] **Step 1: Delete the files**

```bash
cd /home/operation/Development/slm2
git rm resource_management/resource_management.common/src/main/java/org/eclipse/slm/resource_management/common/adapters/ResourcesConsulClient.java \
       resource_management/resource_management.common/src/main/java/org/eclipse/slm/resource_management/common/adapters/ResourcesConsulClientFactory.java \
       resource_management/resource_management.common/src/main/java/org/eclipse/slm/resource_management/common/adapters/ResourcesConsulMapper.java \
       resource_management/resource_management.common/src/main/java/org/eclipse/slm/resource_management/common/adapters/ResourceConsulNode.kt \
       resource_management/resource_management.common/src/test/java/org/eclipse/slm/resource_management/common/adapters/ResourcesConsulClientTest.java
```

- [ ] **Step 2: Find remaining references**

Run:
```bash
grep -rn "ResourcesConsulClient\|ResourceConsulNode\|ResourcesConsulMapper" \
  resource_management --include="*.java" --include="*.kt"
```
Expected hits: `SingleHostCapabilitiesConsulClient` (uses `ResourcesConsulClientFactory`/`ResourcesConsulClient`) and possibly capability tests. These belong to the **CapabilityServices phase**, which is not migrated yet — they must keep compiling now.

- [ ] **Step 3: Keep capability code compiling (temporary bridge)**

`SingleHostCapabilitiesConsulClient` calls `resourcesConsulAdminClient.getResources()` in `removeCapabilityServiceFromAllConsulNodes`. To keep this phase self-contained and compiling, replace that single dependency: inject `ResourceJpaRepository` into `SingleHostCapabilitiesConsulClient` instead of `ResourcesConsulClientFactory`, and change the loop source from `this.resourcesConsulAdminClient.getResources()` to `resourceJpaRepository.findAll()`. Concretely:
  - Remove fields `resourcesConsulClientFactory` and `resourcesConsulAdminClient` and their constructor params/imports.
  - Add constructor param `ResourceJpaRepository resourceJpaRepository` and field.
  - In `removeCapabilityServiceFromAllConsulNodes`, change `var existingResources = this.resourcesConsulAdminClient.getResources();` to `var existingResources = this.resourceJpaRepository.findAll();`.

(The rest of `SingleHostCapabilitiesConsulClient` stays Consul-backed until its own plan.)

- [ ] **Step 4: Compile the whole resource_management module**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.common -am compile`
Expected: PASS (BUILD SUCCESS).

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor(resources): replace Consul storage with DB persistence and policy access control"
```

---

## Task 15: Fix and run the resources module test suite

**Files:**
- Modify/Delete: any remaining resources tests that referenced Consul resource APIs (e.g. `ResourcesRestControllerIT`, `ResourcesManagerImplITDev`).

- [ ] **Step 1: Identify broken tests**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.common test-compile`
Expected: compilation errors in tests still calling `getResources(token)` / Consul helpers.

- [ ] **Step 2: Update or delete those tests**

For each failing test: if it tested Consul resource behavior directly, delete it (Consul resource storage no longer exists). If it tested manager behavior, update calls to pass a `UserContext` (e.g. admin context `new UserContext(Set.of(), true)` or a group context). Make the minimal change to restore compilation; do not weaken assertions.

- [ ] **Step 3: Run the full module test suite**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.common test`
Expected: PASS (all tests green).

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "test(resources): migrate resource tests to DB persistence + UserContext"
```

---

## Task 16: Build the runnable service app

**Files:** none (verification)

- [ ] **Step 1: Compile the service app module to catch wiring/bean issues**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.service/resource_management.service.app -am test-compile`
Expected: PASS. (Confirms `AccessControlService`, `ResourceJpaRepository`, and the new `BasicResource` entity are picked up by the existing `@EntityScan`/`@EnableJpaRepositories`/`scanBasePackages` for `org.eclipse.slm.resource_management.common`.)

- [ ] **Step 2: Run app-module tests**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl resource_management/resource_management.service/resource_management.service.app test`
Expected: PASS, or only failures unrelated to this change (note any such pre-existing failures in the task notes; do not fix unrelated failures here).

- [ ] **Step 3: Commit (if any test fixes were needed)**

```bash
git add -A
git commit -m "test(resources): fix service-app wiring for DB-backed resources"
```

---

## Task 17: Fix service_management resource-IP lookup (Consul → REST)

**Context:** Task 1 verification found that `service_management`'s
`ServiceOfferingOrderHandler.getResourceIpOfServiceHoster` reads the resource IP
directly from Consul (`consulAdminClient.nodes().getNodeById(resourceId)`).
After Task 14 removes resource node storage from Consul this returns empty and
the IP becomes `"N/A"`, breaking service ordering. User decision: fix now via the
already-injected `ResourceManagementClientFactory` (REST), threading the access
token. `ResourceDTO` exposes `ip`.

**Files:**
- Modify: `service_management/service_management.service/service_management.service.app/src/main/java/org/eclipse/slm/service_management/service/app/service_offerings/ServiceOfferingOrderHandler.java`

- [ ] **Step 1: Trace the access token through the call chain**

Run: `grep -n "getResourceIpOfServiceHoster\|getResourceIdOfServiceHoster\|accessToken\|createWithBearerTokenAuth" service_management/service_management.service/service_management.service.app/src/main/java/org/eclipse/slm/service_management/service/app/service_offerings/ServiceOfferingOrderHandler.java`
Identify the caller of `getResourceIpOfServiceHoster` and confirm an `accessToken` (or `ResourceManagementClient`) is available there. The class already builds clients via `resourceManagementClientFactory.createWithBearerTokenAuth(accessToken)` at lines ~118/129/159.

- [ ] **Step 2: Replace the Consul read with a REST lookup**

Change `getResourceIpOfServiceHoster` to take the access token (or an already-built `ResourceManagementClient`) and fetch the resource via REST instead of Consul. Resulting shape:
```java
    private String getResourceIpOfServiceHoster(CapabilityService capabilityService, String accessToken) {
        var resourceId = this.getResourceIdOfServiceHoster(capabilityService);
        var resourceManagementClient = resourceManagementClientFactory.createWithBearerTokenAuth(accessToken);
        var resource = resourceManagementClient.getResourcesApiClient().getResource(resourceId);

        String resourceIp = "N/A";
        if (resource != null && resource.getBody() != null && resource.getBody().getIp() != null) {
            resourceIp = resource.getBody().getIp();
        }
        return resourceIp;
    }
```
Adapt the exact client accessor/return type to the actual `ResourceManagementClient` API (verify with `grep -n "getResource\|ResourcesApiClient\|public" resource_management/resource_management.service/resource_management.service.client/src/main/java/org/eclipse/slm/resource_management/service/client/ResourceManagementClient.java`). Update the call site to pass the token. Remove the now-unused Consul `Node` import and, if `consulAdminClient`/`consulClientFactory` become entirely unused in this class, remove those fields and constructor params too.

- [ ] **Step 3: Compile service_management**

Run: `cd /home/operation/Development/slm2 && mvn -q -pl service_management/service_management.service/service_management.service.app -am test-compile`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor(service-mgmt): resolve resource IP via REST instead of Consul"
```

---

## Self-Review Checklist (completed during planning)

- **Spec coverage:** Phase 1 → Task 1. Phase 2 (access-control component, UserContext, KeycloakTokenUtil.getGroups, AccessControlService) → Tasks 2–9. Phase 3 (Resources → DB, manager/controller rewrite, Consul removal, tests) → Tasks 10–16. CapabilityServices/MultiHost/RemoteAccess (phases 4–6) and final Consul-dependency cleanup (phase 7) are intentionally deferred to follow-up plans.
- **Type consistency:** `UserContext.getGroups()/isAdmin()`, `AccessControlObjectType.RESOURCE`, `AccessControlObjectRef.getObjectType()/getObjectId()`, `AccessControlService.createSingleObjectPolicy/hasAccess/getAccessibleObjectIds/removeObjectFromAllPolicies`, `ResourceJpaRepository.findByIdIn`, `AccessControlPolicyRepository.findAccessibleObjectIds/findByObject/countAccessGranting` are used consistently across tasks.
- **Documented temporary seams (not placeholders):** the `""` JWT argument passed to the still-Consul-backed `RemoteAccessManager` (removed in the RemoteAccess plan), and the `ResourceJpaRepository` bridge in `SingleHostCapabilitiesConsulClient` (replaced in the CapabilityServices plan).
```
