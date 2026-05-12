package org.eclipse.slm.platform_management.features.user_management.impl;

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
import org.eclipse.slm.platform_management.features.user_management.api.UserCreateRequest;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class UserManagerImpl implements UserManager {

    private static final Logger LOG = LoggerFactory.getLogger(UserManagerImpl.class);

    public static final String KEYCLOAK_PARENT_GROUP_USERS = "users";
    public static final String KEYCLOAK_REALM_ROLE_NAME_SLM_USER = "slm-user";
    public static final String KEYCLOAK_REALM_ROLE_NAME_SLM_ADMIN = "slm-admin";
    public static final String CONSUL_KEYCLOAK_AUTH_METHOD_NAME = "keycloak";

    private final MultiTenantKeycloakRegistration multiTenantKeycloakRegistration;
    private final ConsulClient consulAdminClient;
    private final VaultClient vaultAdminClient;
    private final KeycloakAdminClient keycloakAdminClient;

    public UserManagerImpl(MultiTenantKeycloakRegistration multiTenantKeycloakRegistration,
                           ConsulClientFactory consulClientFactory,
                           VaultClientFactory vaultClientFactory,
                           KeycloakAdminClient keycloakAdminClient) {
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
            var realm = this.multiTenantKeycloakRegistration.getDefaultRealm();
            var keycloakUserId = this.keycloakAdminClient.getUserId(username);
            this.validateAdminDeletion(keycloakUserId, username);
            var fullPathUserGroupId = "/" + KEYCLOAK_PARENT_GROUP_USERS + "/" + keycloakUserId;

            try {
                var userGroupRole = this.consulAdminClient.acl().getRoleByName(fullPathUserGroupId);
                this.consulAdminClient.acl().deleteRoleById(userGroupRole.getId());
            } catch (ConsulRoleNotFoundException ignored) {
                // Ignore missing Consul role during cleanup.
            }

            var bindingRules = this.consulAdminClient.acl().getBindingRules();
            var userGroupBindingRules = bindingRules.stream()
                    .filter(br -> br.getBindName().equals(this.consulAdminClient.acl().cleanUserGroupBindingRuleName(fullPathUserGroupId)))
                    .toList();
            for (var bindingRule : userGroupBindingRules) {
                this.consulAdminClient.acl().deleteBindingRuleById(bindingRule.getId());
            }

            try {
                var vaultUserGroup = this.vaultAdminClient.acl().getGroupByName(fullPathUserGroupId);
                if (vaultUserGroup.getAlias() == null) {
                    throw new UserManagementRuntimeException("Vault group alias for user group '" + fullPathUserGroupId + "' not found");
                }
                this.vaultAdminClient.auth().removeJwtGroupAlias(vaultUserGroup.getAlias().getId());
                this.vaultAdminClient.acl().deleteGroupByName(fullPathUserGroupId);
            } catch (VaultGroupNotFoundException ignored) {
                // Ignore missing Vault group during cleanup.
            }

            this.keycloakAdminClient.deleteChildGroup(realm, keycloakUserId, KEYCLOAK_PARENT_GROUP_USERS);
            this.keycloakAdminClient.deleteUser(realm, keycloakUserId);
            LOG.info("Successfully deleted user '{}'", username);
        } catch (Exception e) {
            if (e instanceof KeycloakUserNotFoundException) {
                throw new UserNotFoundException("User '" + username + "' not found", e);
            }
            if (e instanceof LastAdminDeletionNotAllowedException) {
                throw (LastAdminDeletionNotAllowedException) e;
            }
            throw new UserManagementRuntimeException("Error deleting user '" + username + "'", e);
        }
    }

    private void validateAdminDeletion(String keycloakUserId, String username) {
        var adminUserIds = this.keycloakAdminClient.getUserIdsAssignedToRole(KEYCLOAK_REALM_ROLE_NAME_SLM_ADMIN);
        var isAdminUser = adminUserIds.contains(keycloakUserId);

        if (!isAdminUser) {
            return;
        }

        if (adminUserIds.size() <= 1) {
            throw new LastAdminDeletionNotAllowedException(
                    "Cannot delete user '" + username + "' because at least one admin user must remain");
        }
    }

    private String configureKeycloakForUser(UserCreateRequest userCreateRequest)
            throws KeycloakGroupRuntimeException, KeycloakGroupNotFoundException, KeycloakUserNotFoundException, KeycloakUserRuntimeException {
        var realm = this.multiTenantKeycloakRegistration.getDefaultRealm();
        var keycloakUserRepresentation = new UserRepresentation();
        keycloakUserRepresentation.setUsername(userCreateRequest.getUsername());
        keycloakUserRepresentation.setEnabled(true);
        keycloakUserRepresentation.setEmail(userCreateRequest.getEmail());
        keycloakUserRepresentation.setFirstName(userCreateRequest.getFirstName());
        keycloakUserRepresentation.setLastName(userCreateRequest.getLastName());
        var createdKeycloakUser = this.keycloakAdminClient.createUser(realm, keycloakUserRepresentation);
        var keycloakUserId = UUID.fromString(createdKeycloakUser.getId());
        var fullPathUserGroupId = "/" + KEYCLOAK_PARENT_GROUP_USERS + "/" + keycloakUserId;

        this.keycloakAdminClient.setUserPassword(keycloakUserId.toString(), userCreateRequest.getPassword(), userCreateRequest.isPasswordTemporary());
        this.keycloakAdminClient.assignUserToRealmRole(KEYCLOAK_REALM_ROLE_NAME_SLM_USER, keycloakUserId.toString());
        if (userCreateRequest.isAdmin()) {
            this.keycloakAdminClient.assignUserToRealmRole(KEYCLOAK_REALM_ROLE_NAME_SLM_ADMIN, keycloakUserId.toString());
        }

        this.keycloakAdminClient.createChildGroup(
                realm,
                keycloakUserId.toString(),
                "Group of user '" + userCreateRequest.getUsername() + "'",
                KEYCLOAK_PARENT_GROUP_USERS,
                Map.of());
        this.keycloakAdminClient.assignUserToGroup(realm, keycloakUserId.toString(), keycloakUserId);

        return fullPathUserGroupId;
    }

    private void configureConsulForUserGroup(String fullPathUserGroupId) {
        try {
            this.consulAdminClient.acl().createRole(fullPathUserGroupId, "Role for Keycloak user group '" + fullPathUserGroupId + "'", List.of());
        } catch (ConsulRuntimeException e) {
            if (!e.getCause().getMessage().contains("already exists")) {
                throw e;
            }
        }

        try {
            var bindingRuleUserGroup = new BindingRule(
                    null,
                    "Binding rule of group '" + fullPathUserGroupId + "' to auth method 'keycloak'",
                    CONSUL_KEYCLOAK_AUTH_METHOD_NAME,
                    "\"" + fullPathUserGroupId + "\" in list.groups",
                    "role",
                    fullPathUserGroupId);
            this.consulAdminClient.acl().createBindingRule(bindingRuleUserGroup);
        } catch (ConsulRuntimeException e) {
            if (!e.getCause().getMessage().contains("already exists")) {
                throw e;
            }
        }
    }

    private void configureVaultForUserGroup(String fullPathUserGroupId) {
        this.vaultAdminClient.acl().createOrUpdateGroup(fullPathUserGroupId, GroupType.EXTERNAL, List.of());
        var vaultUserGroup = this.vaultAdminClient.acl().getGroupByName(fullPathUserGroupId);
        var mountAccessor = this.vaultAdminClient.auth().getJwtAuthMethod().getAccessor();
        try {
            this.vaultAdminClient.auth().addJwtGroupAlias(fullPathUserGroupId, mountAccessor, vaultUserGroup.getId());
        } catch (VaultRuntimeException e) {
            if (!e.getCause().getMessage().contains("combination of mount and group alias name is already in use")) {
                throw e;
            }
        }
    }

    @Override
    public List<Map<String, Object>> getUsers() {
        var realm = this.multiTenantKeycloakRegistration.getDefaultRealm();
        var adminUserIds = new HashSet<>(this.keycloakAdminClient.getUserIdsAssignedToRole(KEYCLOAK_REALM_ROLE_NAME_SLM_ADMIN));

        return this.keycloakAdminClient.getUsersOfRealm(realm)
                .stream()
                .filter(user -> user.getUsername() != null && !user.getUsername().startsWith("service-account-"))
                .map(user -> Map.<String, Object>of(
                        "username", user.getUsername(),
                        "firstName", Objects.requireNonNullElse(user.getFirstName(), ""),
                        "lastName", Objects.requireNonNullElse(user.getLastName(), ""),
                        "email", Objects.requireNonNullElse(user.getEmail(), ""),
                        "admin", adminUserIds.contains(user.getId())))
                .toList();
    }

    @Override
    public void makeUserAdmin(String username) {
        try {
            var userId = this.keycloakAdminClient.getUserId(username);
            this.keycloakAdminClient.assignUserToRealmRole(KEYCLOAK_REALM_ROLE_NAME_SLM_ADMIN, userId);
            LOG.info("Successfully granted '{}' role to user '{}'", KEYCLOAK_REALM_ROLE_NAME_SLM_ADMIN, username);
        } catch (Exception e) {
            if (e instanceof KeycloakUserNotFoundException) {
                throw new UserNotFoundException("User '" + username + "' not found", e);
            }
            throw new UserManagementRuntimeException(
                    "Error assigning role '" + KEYCLOAK_REALM_ROLE_NAME_SLM_ADMIN + "' to user '" + username + "'",
                    e);
        }
    }
}

