package org.eclipse.slm.service_management.common.impl.users;

import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.service_management.MultiTenancyUtil;
import org.eclipse.slm.service_management.common.api.users.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.service_management.common.api.users.UsersRestApi;
import org.eclipse.slm.service_management.common.api.users.UsersRestApiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(UsersRestApiConfig.BASE_PATH)
@Tag(name = UsersRestApiConfig.TAG)
public class UsersRestController implements UsersRestApi {

    private final KeycloakAdminClient keycloakAdminClient;

    @Autowired
    public UsersRestController(
            KeycloakAdminClient keycloakAdminClient)
    {
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @Override
    public @ResponseBody ResponseEntity<List<User>> getUsers()
    {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var keycloakUserRepresentations = this.keycloakAdminClient.getUsersOfRealm(KeycloakTokenUtil.getRealm(jwtAuthenticationToken));
        var users = new ArrayList<User>();
        for (var keycloakUserRepresentation : keycloakUserRepresentations) {
            var user = new User();
            user.setId(keycloakUserRepresentation.getId());
            user.setUsername(keycloakUserRepresentation.getUsername());
            user.setFirstName(keycloakUserRepresentation.getFirstName());
            user.setLastName(keycloakUserRepresentation.getLastName());
            user.setEmail(keycloakUserRepresentation.getEmail());
            users.add(user);
        }

        return ResponseEntity.ok(users);
    }

    @Override
    public @ResponseBody
    UUID getUserIdOfAuthenticatedUser()
    {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var userId = MultiTenancyUtil.getKeycloakUserIdFromAuthenticationToken(jwtAuthenticationToken);
        return userId;
    }

    @Override
    public @ResponseBody ResponseEntity<List<UUID>> getServiceVendorsOfUser() {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var token = jwtAuthenticationToken.getToken();
        var claims = token.getClaims();
        var vendorPrefix = "/vendor_";
        var serviceVendorIds = new ArrayList<UUID>();
        if (claims.containsKey("groups")) {
            var userGroups = claims.get("groups");
            List<String> normalizedGroups;
            if (userGroups instanceof String[]) {
                normalizedGroups = List.of((String[]) userGroups);
            } else {
                normalizedGroups = (ArrayList<String>) userGroups;
            }
            for (var userGroup : normalizedGroups) {
                if (userGroup.startsWith(vendorPrefix)) {
                    var serviceVendorId = UUID.fromString(userGroup.replace(vendorPrefix, ""));
                    serviceVendorIds.add(serviceVendorId);
                }
            }
        }

        return ResponseEntity.ok(serviceVendorIds);
    }
}

