package org.eclipse.slm.common.keycloak.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakGroupNotFoundException;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakGroupRuntimeException;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakUserNotFoundException;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakUserRuntimeException;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class KeycloakAdminClient {

    private final static Logger LOG = LoggerFactory.getLogger(KeycloakAdminClient.class);

    private final MultiTenantKeycloakRegistration multiTenantKeycloakRegistration;

    private static final String defaultRealm = "fabos";

    @Autowired
    public KeycloakAdminClient(MultiTenantKeycloakRegistration multiTenantKeycloakRegistration) {
        this.multiTenantKeycloakRegistration = multiTenantKeycloakRegistration;
    }

    public List<UserRepresentation> getUsersOfRealm(String realm) {
        var realmResource = this.getKeycloakRealmResource(realm);

        return realmResource.users().list();
    }

    public UserRepresentation createUser(String realm, UserRepresentation userRepresentation) throws KeycloakUserRuntimeException {
        var realmResource = this.getKeycloakRealmResource(realm);
        try {
            var userCreateResponse = realmResource.users().create(userRepresentation);
            return realmResource.users().search(userRepresentation.getUsername()).get(0);
        } catch(Exception e) {
            throw new KeycloakUserRuntimeException("Error creating user '" + userRepresentation.getUsername() + "'", e);
        }
    }

    public List<RoleRepresentation> getAllRolesOfRealm() {
        var realmResource = this.getKeycloakRealmResource(KeycloakAdminClient.defaultRealm);

        return realmResource.roles().list();
    }

    public RoleRepresentation createRealmRole(String roleName)
    {
        var realmResource = this.getKeycloakRealmResource(this.defaultRealm);
        var newRole = new RoleRepresentation();
        newRole.setName(roleName);
        try {
            realmResource.roles().create(newRole);
        } catch(Exception e) {
            LOG.info("Realm role '" + newRole + "' already exists");
        }

        return newRole;
    }

    public void createRealmRoleAndAssignToUser(String userId, String roleName)
    {
        // Create role
        var realmResource = this.getKeycloakRealmResource(KeycloakAdminClient.defaultRealm);
        var createdRole = this.createRealmRole(roleName);
        // Add user to newly created role
        var userResource = realmResource.users().get(userId);
        createdRole = realmResource.roles().get(createdRole.getName()).toRepresentation();
        userResource.roles().realmLevel().add(Arrays.asList(createdRole));
    }

    public void assignUserToRealmRole(String roleName, String userId) {
        var realmResource = this.getKeycloakRealmResource(KeycloakAdminClient.defaultRealm);
        var userResource = realmResource.users().get(userId);
        var realmRole = realmResource.roles().get(roleName).toRepresentation();
        userResource.roles().realmLevel().add(Arrays.asList(realmRole));
    }

    public void deleteRealmRoles(List<String> realmRoleNames) {
        realmRoleNames
                .stream()
                .forEach(realmRole -> deleteRealmRole(realmRole));
    }

    public void deleteRealmRole(String roleName)
    {
        var realmResource = getKeycloakRealmResource(KeycloakAdminClient.defaultRealm);
        try {
            realmResource.roles().deleteRole(roleName);
        } catch(Exception e) {
            LOG.info("Error deleting role '" + roleName + "': " + e);
        }
    }

    private RealmResource getKeycloakRealmResource(JwtAuthenticationToken jwtAuthenticationToken) {
        return multiTenantKeycloakRegistration.getRealmResource(KeycloakTokenUtil.getRealm(jwtAuthenticationToken));
    }

    private RealmResource getKeycloakRealmResource(String realm) {
        return multiTenantKeycloakRegistration.getRealmResource(realm);
    }

    public List<UserRepresentation> getUsersOfGroup(String realm, String keycloakGroupName) throws KeycloakGroupNotFoundException {
        var realmResource = this.getKeycloakRealmResource(realm);
        var realmGroups = realmResource.groups().groups();

        var groupRepresentation = realmGroups.stream().filter(g -> g.getName().equals(keycloakGroupName)).findFirst();

        if (groupRepresentation.isPresent()) {
            var groupResource = realmResource.groups().group(groupRepresentation.get().getId());
            var members = groupResource.members();
            return members;
        }
        else {
            throw new KeycloakGroupNotFoundException(keycloakGroupName);
        }
    }

    public void assignUserToGroup(String realm, String keycloakGroupName, UUID userId) throws KeycloakGroupNotFoundException, KeycloakUserNotFoundException {
        var realmResource = this.getKeycloakRealmResource(realm);
        var user = realmResource.users().get(userId.toString());

        if (user != null) {
            var filteredGroups = realmResource.groups().groups(keycloakGroupName, 0, 1);
            if (!filteredGroups.isEmpty()) {
                GroupRepresentation userGroup;
                if (!filteredGroups.get(0).getName().equals(keycloakGroupName)) {
                    var parentGroup = filteredGroups.get(0);
                    var childGroupOptional = parentGroup.getSubGroups().stream()
                            .filter(g -> g.getName().equals(keycloakGroupName))
                            .findFirst();
                    if (childGroupOptional.isEmpty()) {
                        throw new KeycloakGroupNotFoundException(keycloakGroupName);
                    } else {
                        userGroup = childGroupOptional.get();
                    }
                }
                else {
                    userGroup = filteredGroups.get(0);
                }
                user.joinGroup(userGroup.getId());
            }
            else {
                throw new KeycloakGroupNotFoundException(keycloakGroupName);
            }
        }
        else {
            throw new KeycloakUserNotFoundException(userId);
        }
    }

    public void removeUserFromGroup(String realm, String keycloakGroupName, UUID userId) throws KeycloakGroupNotFoundException, KeycloakUserNotFoundException {
        var realmResource = this.getKeycloakRealmResource(realm);

        var user = realmResource.users().get(userId.toString());
        if (user != null) {
            var realmGroups = realmResource.groups().groups();
            var groupRepresentation = realmGroups.stream().filter(g -> g.getName().equals(keycloakGroupName)).findFirst();
            if (groupRepresentation.isPresent()) {
                user.leaveGroup(groupRepresentation.get().getId());
            }
            else {
                throw new KeycloakGroupNotFoundException(keycloakGroupName);
            }
        }
        else {
            throw new KeycloakUserNotFoundException(userId);
        }
    }

    public List<GroupRepresentation> getGroupsOfUser(String realm, UUID userId) throws KeycloakUserNotFoundException {
        var realmResource = this.getKeycloakRealmResource(realm);

        var user = realmResource.users().get(userId.toString());
        if (user != null) {
            return user.groups();
        }
        else {
            throw new KeycloakUserNotFoundException(userId);
        }
    }

    public GroupRepresentation createGroup(String realm, String groupName, Map<String, List<String>> attributes) {
        var realmResource = this.getKeycloakRealmResource(realm);
        var newGroup = new GroupRepresentation();
        newGroup.setName(groupName);
        newGroup.setAttributes(attributes);
        try {
            Response response = realmResource.groups().add(newGroup);
            if (response.getStatus() != 201) {
                var objectMapper = new ObjectMapper();
                LOG.error("Error creating Keycloak group '" + groupName + "': " + objectMapper.writeValueAsString(response));
            }
        } catch(Exception e) {
            LOG.info("Group '" + groupName + "' already exists");
        }

        return newGroup;
    }

    public void createChildGroup(String realm, String groupName, String groupDescription, String parentGroupname, Map<String, List<String>> attributes)
            throws KeycloakGroupNotFoundException, KeycloakGroupRuntimeException {
        var realmResource = this.getKeycloakRealmResource(realm);

        var matchingGroups = realmResource.groups().groups(parentGroupname, 0, 1);
        if (matchingGroups.isEmpty()) {
            throw new KeycloakGroupNotFoundException(parentGroupname);
        }

        try {
            var parentGroup = matchingGroups.get(0);
            var childGroup = new GroupRepresentation();
            childGroup.setName(groupName);
            childGroup.setDescription(groupDescription);
            childGroup.setAttributes(attributes);
            var subGroupCreateResponse = realmResource.groups().group(parentGroup.getId()).subGroup(childGroup);
        } catch(Exception e) {
            throw new KeycloakGroupRuntimeException("Error creating child group '" + groupName + "' under parent group '" + parentGroupname + "'", e);
        }
    }

    public void deleteGroup(String groupname)
            throws KeycloakGroupNotFoundException, KeycloakGroupRuntimeException {
        var realmResource = this.getKeycloakRealmResource(defaultRealm);

        var matchingGroups = realmResource.groups().groups(groupname, 0, 1);
        if (matchingGroups.isEmpty()) {
            throw new KeycloakGroupNotFoundException(groupname);
        }

        try {
            var foundGroup = matchingGroups.get(0);
            realmResource.groups().group(foundGroup.getId()).remove();
        } catch(Exception e) {
            throw new KeycloakGroupRuntimeException("Error deleting group '" + groupname + "'", e);
        }
    }

    public void deleteChildGroup(String realm, String childGroupName, String parentGroupname)
            throws KeycloakGroupNotFoundException, KeycloakGroupRuntimeException {
        var realmResource = this.getKeycloakRealmResource(realm);

        var matchingGroups = realmResource.groups().groups(parentGroupname, 0, 1);
        if (matchingGroups.isEmpty()) {
            throw new KeycloakGroupNotFoundException(parentGroupname);
        }
        var parentGroup = matchingGroups.get(0);

        var childGroups = realmResource.groups()
                .group(parentGroup.getId())
                .getSubGroups(0, Integer.MAX_VALUE, false);
        var optionalChildGroup = childGroups
                .stream()
                .filter(g -> g.getName().equals(childGroupName))
                .findFirst();
        if (optionalChildGroup.isEmpty()) {
            throw new KeycloakGroupNotFoundException(childGroupName);
        }

        try {
            realmResource.groups().group(optionalChildGroup.get().getId()).remove();
        } catch(Exception e) {
            throw new KeycloakGroupRuntimeException("Error deleting child group '" + childGroupName + "' under parent group '" + parentGroupname + "'", e);
        }
    }

    public List<String> getUserIdsAssignedToRole(String roleName) {
        var userIds = new ArrayList<String>();

        var realmResource = this.getKeycloakRealmResource(defaultRealm);
        var roleRepresentation = realmResource.roles().get(roleName).toRepresentation();

        if (roleRepresentation != null) {
            var users = realmResource.roles().get(roleName).getRoleUserMembers();
            users.forEach(user -> userIds.add(user.getId()));

            LOG.debug("Users with role '" + roleName + "': " + users);
        } else {
            LOG.warn("Role '" + roleName + "' not found in realm '" + defaultRealm + "'");
        }

        return userIds;
    }

    public String getUserId(String username) throws KeycloakUserNotFoundException {
        var realmResource = this.getKeycloakRealmResource(defaultRealm);
        var users = realmResource.users().search(username);

        if (users.isEmpty()) {
            throw new KeycloakUserNotFoundException(username);
        }

        return users.get(0).getId();
    }

    public List<String> getAllUserIds() {
        var userIds = new ArrayList<String>();
        var realmResource = this.getKeycloakRealmResource(defaultRealm);
        var users = realmResource.users().list();

        for (UserRepresentation user : users) {
            userIds.add(user.getId());
        }

        LOG.debug("All user IDs in realm '" + defaultRealm + "': " + userIds);
        return userIds;
    }

    public void setUserPassword(String userId, String password, boolean temporaryPassword) {
        var realmResource = this.getKeycloakRealmResource(defaultRealm);

        var credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(password);
        credentialRepresentation.setTemporary(temporaryPassword);

        realmResource.users().get(userId).resetPassword(credentialRepresentation);
    }

    public void deleteUser(String realm, String keycloakUserId) {
        var realmResource = this.getKeycloakRealmResource(realm);

        realmResource.users().get(keycloakUserId).remove();
    }
}
