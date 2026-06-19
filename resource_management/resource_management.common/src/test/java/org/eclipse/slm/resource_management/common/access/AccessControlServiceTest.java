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
}
