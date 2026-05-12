package org.eclipse.slm.platform_management.features.user_management.impl;

import org.eclipse.slm.common.consul.client.ConsulAclClient;
import org.eclipse.slm.common.consul.client.ConsulClient;
import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.consul.model.acl.bindingrules.BindingRule;
import org.eclipse.slm.common.consul.model.acl.roles.Role;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakUserNotFoundException;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultClientAcl;
import org.eclipse.slm.common.vault.client.VaultClientAuth;
import org.eclipse.slm.common.vault.client.VaultClientFactory;
import org.eclipse.slm.common.vault.model.acl.Group;
import org.eclipse.slm.common.vault.model.auth.JwtGroupAlias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagerImplTest {

    private static final String DEFAULT_REALM = "fabos";

    @Mock
    private MultiTenantKeycloakRegistration multiTenantKeycloakRegistration;

    @Mock
    private ConsulClientFactory consulClientFactory;

    @Mock
    private VaultClientFactory vaultClientFactory;

    @Mock
    private KeycloakAdminClient keycloakAdminClient;

    @Mock
    private ConsulClient consulClient;

    @Mock
    private ConsulAclClient consulAclClient;

    @Mock
    private VaultClient vaultClient;

    @Mock
    private VaultClientAcl vaultClientAcl;

    @Mock
    private VaultClientAuth vaultClientAuth;

    private UserManagerImpl userManager;

    @BeforeEach
    void setUp() {
        when(consulClientFactory.createAdminClient()).thenReturn(consulClient);
        when(vaultClientFactory.createAdminClient()).thenReturn(vaultClient);

        userManager = new UserManagerImpl(
                multiTenantKeycloakRegistration,
                consulClientFactory,
                vaultClientFactory,
                keycloakAdminClient);
    }

    @Nested
    class DeleteUserTests {

        @BeforeEach
        void setUpDeleteUserTests() {
            when(multiTenantKeycloakRegistration.getDefaultRealm()).thenReturn(DEFAULT_REALM);
        }

        @Test
        void deleteUser_deletesResources_whenUserCanBeDeleted() throws Exception {
            var username = "alice";
            var keycloakUserId = "user-1";
            var fullPathUserGroupId = "/users/user-1";
            var cleanedBindingRuleName = "users_user-1";

            when(consulClient.acl()).thenReturn(consulAclClient);
            when(vaultClient.acl()).thenReturn(vaultClientAcl);
            when(vaultClient.auth()).thenReturn(vaultClientAuth);

            when(keycloakAdminClient.getUserId(username)).thenReturn(keycloakUserId);
            when(keycloakAdminClient.getUserIdsAssignedToRole(UserManagerImpl.KEYCLOAK_REALM_ROLE_NAME_SLM_ADMIN))
                    .thenReturn(List.of("another-admin"));

            var role = new Role("role-id", "roleName", null, Collections.emptyList());
            when(consulAclClient.getRoleByName(fullPathUserGroupId)).thenReturn(role);
            when(consulAclClient.cleanUserGroupBindingRuleName(fullPathUserGroupId)).thenReturn(cleanedBindingRuleName);
            when(consulAclClient.getBindingRules()).thenReturn(List.of(
                    new BindingRule("binding-rule-id", "desc", "keycloak", "selector", "role", cleanedBindingRuleName)
            ));

            var vaultGroup = new Group();
            vaultGroup.setAlias(new JwtGroupAlias(null, null, "alias-id", null, null, null, null, null, null, null));
            when(vaultClientAcl.getGroupByName(fullPathUserGroupId)).thenReturn(vaultGroup);

            userManager.deleteUser(username);

            verify(consulAclClient).deleteRoleById("role-id");
            verify(consulAclClient).deleteBindingRuleById("binding-rule-id");
            verify(vaultClientAuth).removeJwtGroupAlias("alias-id");
            verify(vaultClientAcl).deleteGroupByName(fullPathUserGroupId);
            verify(keycloakAdminClient).deleteChildGroup(DEFAULT_REALM, keycloakUserId, UserManagerImpl.KEYCLOAK_PARENT_GROUP_USERS);
            verify(keycloakAdminClient).deleteUser(DEFAULT_REALM, keycloakUserId);
        }

        @Test
        void deleteUser_throwsLastAdminDeletionNotAllowedException_whenDeletingOnlyAdmin() throws Exception {
            var username = "admin";
            var keycloakUserId = "admin-user-id";

            when(keycloakAdminClient.getUserId(username)).thenReturn(keycloakUserId);
            when(keycloakAdminClient.getUserIdsAssignedToRole(UserManagerImpl.KEYCLOAK_REALM_ROLE_NAME_SLM_ADMIN))
                    .thenReturn(List.of(keycloakUserId));

            var exception = assertThrows(LastAdminDeletionNotAllowedException.class, () -> userManager.deleteUser(username));

            assertTrue(exception.getMessage().contains("at least one admin user must remain"));
            verify(keycloakAdminClient, never()).deleteUser(DEFAULT_REALM, keycloakUserId);
        }

        @Test
        void deleteUser_throwsUserNotFoundException_whenKeycloakUserDoesNotExist() throws Exception {
            var username = "ghost";
            when(keycloakAdminClient.getUserId(username)).thenThrow(new KeycloakUserNotFoundException(username));

            var exception = assertThrows(UserNotFoundException.class, () -> userManager.deleteUser(username));

            assertTrue(exception.getMessage().contains("User 'ghost' not found"));
        }
    }

    @Nested
    class GetUsersTests {

        @BeforeEach
        void setUpGetUsersTests() {
            when(multiTenantKeycloakRegistration.getDefaultRealm()).thenReturn(DEFAULT_REALM);
        }

        @Test
        void getUsers_filtersServiceAccountsAndMapsAdminFlagAndFallbackValues() {
            var regularUser = new UserRepresentation();
            regularUser.setId("user-1");
            regularUser.setUsername("alice");
            regularUser.setFirstName(null);
            regularUser.setLastName("Doe");
            regularUser.setEmail(null);

            var serviceAccount = new UserRepresentation();
            serviceAccount.setId("user-2");
            serviceAccount.setUsername("service-account-operator");

            when(keycloakAdminClient.getUserIdsAssignedToRole(UserManagerImpl.KEYCLOAK_REALM_ROLE_NAME_SLM_ADMIN))
                    .thenReturn(List.of("user-1"));
            when(keycloakAdminClient.getUsersOfRealm(DEFAULT_REALM)).thenReturn(List.of(regularUser, serviceAccount));

            List<Map<String, Object>> users = userManager.getUsers();

            assertEquals(1, users.size());
            var userEntry = users.getFirst();
            assertEquals("alice", userEntry.get("username"));
            assertEquals("", userEntry.get("firstName"));
            assertEquals("Doe", userEntry.get("lastName"));
            assertEquals("", userEntry.get("email"));
            assertTrue((Boolean) userEntry.get("admin"));
        }
    }

    @Nested
    class MakeUserAdminTests {

        @Test
        void makeUserAdmin_assignsAdminRole_whenUserExists() throws Exception {
            var username = "alice";
            var userId = "user-1";
            when(keycloakAdminClient.getUserId(username)).thenReturn(userId);

            userManager.makeUserAdmin(username);

            verify(keycloakAdminClient).assignUserToRealmRole(UserManagerImpl.KEYCLOAK_REALM_ROLE_NAME_SLM_ADMIN, userId);
        }

        @Test
        void makeUserAdmin_throwsUserNotFoundException_whenUserDoesNotExist() throws Exception {
            var username = "ghost";
            when(keycloakAdminClient.getUserId(username)).thenThrow(new KeycloakUserNotFoundException(username));

            RuntimeException exception = assertThrows(RuntimeException.class, () -> userManager.makeUserAdmin(username));

            assertInstanceOf(UserNotFoundException.class, exception);
            assertFalse(exception.getMessage().isBlank());
        }
    }
}


