package org.eclipse.slm.common.keycloak.config;

import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakGroupNotFoundException;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakUserNotFoundException;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.ClientErrorException;
import java.util.*;

@Component
public class KeycloakUtil {

    private final static Logger LOG = LoggerFactory.getLogger(KeycloakUtil.class);

    private final MultiTenantKeycloakRegistration multiTenantKeycloakRegistration;

    @Autowired
    public KeycloakUtil(MultiTenantKeycloakRegistration multiTenantKeycloakRegistration) {
        this.multiTenantKeycloakRegistration = multiTenantKeycloakRegistration;
    }

    public List<UserRepresentation> getUsersOfRealm(String realm) {
        var realmResource = this.getKeycloakRealm(realm);

        return realmResource.users().list();
    }

    public RoleRepresentation createRealmRole(String realm, String roleName)
    {
        var realmResource = this.getKeycloakRealm(realm);
        var newRole = new RoleRepresentation();
        newRole.setName(roleName);
        try {
            realmResource.roles().create(newRole);
        } catch(ClientErrorException e) {
            LOG.info("Realm role '" + newRole + "' already exists");
        }

        return newRole;
    }

    public void createRealmRoleAndAssignToUser(KeycloakPrincipal keycloakPrincipal, String roleName)
    {
        // Create role
        var realmResource = this.getKeycloakRealm(keycloakPrincipal);
        var createdRole = this.createRealmRole(keycloakPrincipal.getKeycloakSecurityContext().getRealm(), roleName);
        // Add user to newly created role
        UserResource userResource = realmResource.users().get(keycloakPrincipal.getName());
        createdRole = realmResource.roles().get(createdRole.getName()).toRepresentation();
        userResource.roles().realmLevel().add(Arrays.asList(createdRole));
    }

    public void deleteRealmRoles(KeycloakPrincipal keycloakPrincipal, List<String> realmRoleNames) {
        realmRoleNames
                .stream()
                .forEach(realmRole -> deleteRealmRole(keycloakPrincipal, realmRole));
    }

    public void deleteRealmRole(KeycloakPrincipal keycloakPrincipal, String roleName)
    {
        var realmResource = getKeycloakRealm(keycloakPrincipal);
        try {
            realmResource.roles().deleteRole(roleName);
        } catch(ClientErrorException e) {
            LOG.info("Error deleting role '" + roleName + "': " + e);
        }
    }

    public void deleteRealmRoleAsAdmin(KeycloakPrincipal keycloakPrincipal, String roleName)
    {
        var realmResource = getKeycloakRealm(keycloakPrincipal.getKeycloakSecurityContext().getRealm());
        try {
            realmResource.roles().deleteRole(roleName);
        } catch(ClientErrorException e) {
            LOG.info("Error deleting role '" + roleName + "': " + e);
        }
    }

    private RealmResource getKeycloakRealm(KeycloakPrincipal keycloakPrincipal) {
        return multiTenantKeycloakRegistration.getRealmResource(KeycloakTokenUtil.getRealm(keycloakPrincipal));
    }

    private RealmResource getKeycloakRealm(String realm) {
        return multiTenantKeycloakRegistration.getRealmResource(realm);
    }

    public List<UserRepresentation> getUsersOfGroup(String realm, String keycloakGroupName) throws KeycloakGroupNotFoundException {
        var realmResource = this.getKeycloakRealm(realm);
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
        var realmResource = this.getKeycloakRealm(realm);

        var user = realmResource.users().get(userId.toString());


        if (user != null) {
            var realmGroups = realmResource.groups().groups();
            var groupRepresentation = realmGroups.stream().filter(g -> g.getName().equals(keycloakGroupName)).findFirst();
            if (groupRepresentation.isPresent()) {
                user.joinGroup(groupRepresentation.get().getId());
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
        var realmResource = this.getKeycloakRealm(realm);

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
        var realmResource = this.getKeycloakRealm(realm);

        var user = realmResource.users().get(userId.toString());
        if (user != null) {
            return user.groups();
        }
        else {
            throw new KeycloakUserNotFoundException(userId);
        }
    }

    public GroupRepresentation createGroup(String realm, String groupName, Map<String, List<String>> attributes) {
        var realmResource = this.getKeycloakRealm(realm);
        var newGroup = new GroupRepresentation();
        newGroup.setName(groupName);
        newGroup.setAttributes(attributes);
        try {
            realmResource.groups().add(newGroup);
        } catch(ClientErrorException e) {
            LOG.info("Group '" + groupName + "' already exists");
        }

        return newGroup;
    }

    public void deleteGroup(String realm, String keycloakGroupName) throws KeycloakGroupNotFoundException {
        var realmResource = this.getKeycloakRealm(realm);

        var realmGroups = realmResource.groups().groups();
        var groupRepresentation = realmGroups.stream().filter(g -> g.getName().equals(keycloakGroupName)).findFirst();

        if (groupRepresentation.isPresent()) {
            realmResource.groups().group(groupRepresentation.get().getId()).remove();
        }
        else {
            throw new KeycloakGroupNotFoundException(keycloakGroupName);
        }
    }
}
