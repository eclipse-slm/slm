package org.eclipse.slm.service_management.service.rest.endpoints;

import org.eclipse.slm.common.keycloak.config.KeycloakUtil;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.service_management.service.rest.utils.MultiTenancyUtil;
import org.eclipse.slm.service_management.model.users.User;
import org.eclipse.slm.service_management.persistence.keycloak.ServiceVendorRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/services/users")
public class UsersRestController {

    private final ServiceVendorRepository serviceVendorRepository;

    private final KeycloakUtil keycloakUtil;

    @Autowired
    public UsersRestController(
            ServiceVendorRepository serviceVendorRepository,
            KeycloakUtil keycloakUtil)
    {
        this.serviceVendorRepository = serviceVendorRepository;
        this.keycloakUtil = keycloakUtil;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get users")
    public @ResponseBody ResponseEntity<List<User>> getUsers()
    {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var keycloakUserRepresentations = this.keycloakUtil.getUsersOfRealm(KeycloakTokenUtil.getRealm(keycloakPrincipal));
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

    @RequestMapping(value = "/id", method = RequestMethod.GET)
    @Operation(summary = "Get user id of authenticated user")
    public @ResponseBody
    UUID getUserIdOfAuthenticatedUser(
            KeycloakAuthenticationToken authentication)
    {
        var userId = MultiTenancyUtil.getKeycloakUserIdFromKeycloakAuthenticationToken(authentication);
        return userId;
    }

    @RequestMapping(value = "/organisation/id", method = RequestMethod.GET)
    @Operation(summary = "Get organisation id of authenticated user")
    public @ResponseBody
    UUID getOrganisationIdOfAuthenticatedUser(
            KeycloakAuthenticationToken authentication)
    {
        var organisationId = MultiTenancyUtil.generateOrganisationIdFromKeycloakAuthenticationToken(authentication);
        return organisationId;
    }

    @RequestMapping(value = "/vendors", method = RequestMethod.GET)
    @Operation(summary = "Get service vendors of authenticated user")
    public @ResponseBody ResponseEntity<List<UUID>> getServiceVendorsOfUser() {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var serviceVendorIds = this.serviceVendorRepository.getServiceVendorsOfDeveloper(keycloakPrincipal);

        return ResponseEntity.ok(serviceVendorIds);
    }
}
