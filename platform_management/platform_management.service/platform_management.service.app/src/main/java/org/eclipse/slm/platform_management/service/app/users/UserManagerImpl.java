package org.eclipse.slm.platform_management.service.app.users;

import org.eclipse.slm.common.consul.client.ConsulClient;
import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.consul.model.acl.bindingrules.BindingRule;
import org.eclipse.slm.common.consul.model.exceptions.ConsulRoleNotFoundException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulRuntimeException;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakGroupNotFoundException;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakGroupRuntimeException;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakUserNotFoundException;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakUserRuntimeException;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultClientFactory;
import org.eclipse.slm.common.vault.client.exceptions.VaultGroupNotFoundException;
import org.eclipse.slm.common.vault.client.exceptions.VaultRuntimeException;
import org.eclipse.slm.common.vault.model.acl.GroupType;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class UserManagerImpl implements UserManager {

    private final static Logger LOG = LoggerFactory.getLogger(UserManagerImpl.class);

    public final static String KEYCLOAK_PARENT_GROUP_USERS = "users";
    public final static String KEYCLOAK_REALM_ROLE_NAME_SLM_USER = "slm-user";
    public final static String KEYCLOAK_REALM_ROLE_NAME_SLM_ADMIN = "slm-admin";
    public final static String CONSUL_KEYCLOAK_AUTH_METHOD_NAME = "keycloak";

    private final MultiTenantKeycloakRegistration multiTenantKeycloakRegistration;

    private final ConsulClient consulAdminClient;

    private final VaultClient vaultAdminClient;

    private final KeycloakAdminClient keycloakAdminClient;

    public UserManagerImpl(MultiTenantKeycloakRegistration multiTenantKeycloakRegistration, ConsulClientFactory consulClientFactory, VaultClientFactory vaultClientFactory, KeycloakAdminClient keycloakAdminClient) {
        this.multiTenantKeycloakRegistration = multiTenantKeycloakRegistration;
        this.consulAdminClient = consulClientFactory.createAdminClient();
        this.vaultAdminClient = vaultClientFactory.createAdminClient();
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @Override
    public void createUser(UserCreateRequest userCreateRequest) {
        try {
            var fullPathUserGroupId = this.configureKeycloakForUser(userCreateRequest);
            this.configureConsulForUserGroup(fullPathUserGroupId);
            this.configureVaultForUserGroup(fullPathUserGroupId);

            LOG.info("Successfully created user '{}'", userCreateRequest.getUsername());
        } catch (Exception e) {
            LOG.error("Error creating user '{}', trying to cleanup and delete user", userCreateRequest.getUsername(), e);
            this.deleteUser(userCreateRequest.getUsername());
            throw new UserManagementRuntimeException("Error creating user '" + userCreateRequest.getUsername() + "', cleaned up and delete user", e);
        }
    }

    @Override
    public void deleteUser(String username) {
        try {
            // Get Keycloak user details
            var realm = this.multiTenantKeycloakRegistration.getDefaultRealm();
            var keycloakUserId = this.keycloakAdminClient.getUserId(username);
            var fullPathUserGroupId = "/" + UserManagerImpl.KEYCLOAK_PARENT_GROUP_USERS + "/" + keycloakUserId;
            // Consul delete role and binding rule
            try {
                var userGroupRole = this.consulAdminClient.acl().getRoleByName(fullPathUserGroupId);
                this.consulAdminClient.acl().deleteRoleById(userGroupRole.getId());
            } catch (ConsulRoleNotFoundException e) {
                // Role not found, possibly already deleted -->  Ignore and continue
            }
            var bindingRules = this.consulAdminClient.acl().getBindingRules();
            var userGroupBindingRules = bindingRules.stream().filter(br -> br.getBindName()
                    .equals(this.consulAdminClient.acl().cleanUserGroupBindingRuleName(fullPathUserGroupId))).toList();
            for (var bindingRule : userGroupBindingRules) {
                this.consulAdminClient.acl().deleteBindingRuleById(bindingRule.getId());
            }

            // Vault delete group and group alias
            try {
                var vaultUserGroup = this.vaultAdminClient.acl().getGroupByName(fullPathUserGroupId);
                if (vaultUserGroup.getAlias() == null) {
                    throw new UserManagementRuntimeException("Vault group alias for user group '" + fullPathUserGroupId + "' not found");
                }
                this.vaultAdminClient.auth().removeJwtGroupAlias(vaultUserGroup.getAlias().getId());
                this.vaultAdminClient.acl().deleteGroupByName(fullPathUserGroupId);
            } catch (VaultGroupNotFoundException e) {
                // Group not found, possibly already deleted -->  Ignore and continue
            }
            // Keycloak delete user group and user
            this.keycloakAdminClient.deleteChildGroup(realm, keycloakUserId, UserManagerImpl.KEYCLOAK_PARENT_GROUP_USERS);
            this.keycloakAdminClient.deleteUser(realm, keycloakUserId);

            LOG.info("Successfully deleted user '{}'", username);
        } catch (Exception e) {
            if (e instanceof KeycloakUserNotFoundException) {
                throw new UserNotFoundException("User '" + username + "' not found", e);
            }
            throw new UserManagementRuntimeException("Error deleting user '" + username + "'", e);
        }
    }

    /** Configure Keycloak for the new user
     *
     * @param userCreateRequest The user create request
     * @return The full path of the created Keycloak user group
     */
    private String configureKeycloakForUser(UserCreateRequest userCreateRequest)
            throws KeycloakGroupRuntimeException, KeycloakGroupNotFoundException, KeycloakUserNotFoundException, KeycloakUserRuntimeException {
        // Create Keycloak user including users group
        var realm = this.multiTenantKeycloakRegistration.getDefaultRealm();
        var keycloakUserRepresentation = new UserRepresentation();
        keycloakUserRepresentation.setUsername(userCreateRequest.getUsername());
        keycloakUserRepresentation.setEnabled(true);
        keycloakUserRepresentation.setEmail(userCreateRequest.getEmail());
        keycloakUserRepresentation.setFirstName(userCreateRequest.getFirstName());
        keycloakUserRepresentation.setLastName(userCreateRequest.getLastName());
        var createdKeycloakUser = this.keycloakAdminClient.createUser(realm, keycloakUserRepresentation);
        var keycloakUserId = UUID.fromString(createdKeycloakUser.getId());
        var fullPathUserGroupId = "/" + UserManagerImpl.KEYCLOAK_PARENT_GROUP_USERS + "/" + keycloakUserId;

        this.keycloakAdminClient.setUserPassword(keycloakUserId.toString(), userCreateRequest.getPassword(), userCreateRequest.isPasswordTemporary());

        this.keycloakAdminClient.assignUserToRealmRole(UserManagerImpl.KEYCLOAK_REALM_ROLE_NAME_SLM_USER, keycloakUserId.toString());
        if (userCreateRequest.isAdmin()) {
            this.keycloakAdminClient.assignUserToRealmRole(UserManagerImpl.KEYCLOAK_REALM_ROLE_NAME_SLM_ADMIN, keycloakUserId.toString());
        }

        this.keycloakAdminClient.createChildGroup(realm, keycloakUserId.toString(),
                "Group of user '" + userCreateRequest.getUsername() + "'",
                UserManagerImpl.KEYCLOAK_PARENT_GROUP_USERS,
                Map.of());
        this.keycloakAdminClient.assignUserToGroup(realm, keycloakUserId.toString(), keycloakUserId);

        return fullPathUserGroupId;
    }

    /** Configure Consul for the Keycloak user group
     *
     * @param fullPathUserGroupId The full path of the Keycloak user group
     */
    private void configureConsulForUserGroup(String fullPathUserGroupId) {
        // Create Consul role user group
        try {
            this.consulAdminClient.acl().createRole(fullPathUserGroupId, "Role for Keycloak user group '" + fullPathUserGroupId + "'", List.of());
        } catch (ConsulRuntimeException e) {
            if (e.getCause().getMessage().contains("already exists")) {
                // Role already exists, ignore
            } else {
                throw e;
            }
        }
        // Create Consul binding rule matching the Keycloak user group
        try {
            var bindingRuleUserGroup = new BindingRule(
                    null,
                    "Binding rule of group '" + fullPathUserGroupId + "' to auth method 'keycloak'",
                    UserManagerImpl.CONSUL_KEYCLOAK_AUTH_METHOD_NAME,
                    "\"" + fullPathUserGroupId + "\" in list.groups",
                    "role",
                    fullPathUserGroupId
            );
            this.consulAdminClient.acl().createBindingRule(bindingRuleUserGroup);
        } catch (ConsulRuntimeException e) {
            if (e.getCause().getMessage().contains("already exists")) {
                // Binding rule already exists, ignore
            } else {
                throw e;
            }
        }
    }

    /** Configure Vault for the Keycloak user group
     *
     * @param fullPathUserGroupId The full path of the Keycloak user group
     */
    private void configureVaultForUserGroup(String fullPathUserGroupId) {
        // Create Vault group for matching Keycloak users group
        vaultAdminClient.acl().createOrUpdateGroup(fullPathUserGroupId, GroupType.EXTERNAL, List.of());
        var vaultUserGroup = vaultAdminClient.acl().getGroupByName(fullPathUserGroupId);
        var mountAccessor = vaultAdminClient.auth().getJwtAuthMethod().getAccessor();
        try {
            vaultAdminClient.auth().addJwtGroupAlias(
                    fullPathUserGroupId,
                    mountAccessor,
                    vaultUserGroup.getId());
        } catch (VaultRuntimeException e) {
            if (e.getCause().getMessage().contains("combination of mount and group alias name is already in use")) {
                // Group alias already exists, ignore
            } else {
                throw e;
            }
        }
    }
}
