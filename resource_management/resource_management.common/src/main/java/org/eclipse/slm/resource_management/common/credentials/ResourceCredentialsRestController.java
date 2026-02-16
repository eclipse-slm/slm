package org.eclipse.slm.resource_management.common.credentials;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resources")
@Tag(name = "Credentials")
public class ResourceCredentialsRestController {

    private final static Logger LOG = LoggerFactory.getLogger(ResourceCredentialsRestController.class);

    private final ResourceCredentialsManager resourceCredentialsManager;

    public ResourceCredentialsRestController(ResourceCredentialsManager resourceCredentialsManager) {
        this.resourceCredentialsManager = resourceCredentialsManager;
    }

    @RequestMapping(value = "/credentials/{credentialId}", method = RequestMethod.GET)
    @Operation(summary = "Get resource credential by id")
    public @ResponseBody ResponseEntity<ResourceCredentialReadDTO> getResourceCredentialById(
            @PathVariable(name = "credentialId") UUID credentialId) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);

        var resourceCredentials = this.resourceCredentialsManager.getCredentialByIdForCurrentUser(credentialId, userAccessToken);
        return ResponseEntity.ok(resourceCredentials);
    }

    @RequestMapping(value = "/{resourceId}/credentials", method = RequestMethod.GET)
    @Operation(summary = "Get credentials of resource")
    public @ResponseBody ResponseEntity<List<ResourceCredentialReadDTO>> getCredentialsOfResource(
            @PathVariable(name = "resourceId") UUID resourceId) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        var resourceCredentials = this.resourceCredentialsManager.getCredentialsOfResourceForCurrentUser(resourceId, userAccessToken);
        return ResponseEntity.ok(resourceCredentials);
    }
}
