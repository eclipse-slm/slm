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
