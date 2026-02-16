package org.eclipse.slm.platform_management.service.app.credentials;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.credentials.CredentialsManager;
import org.eclipse.slm.common.credentials.model.CredentialReadDTO;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.platform_management.service.api.CredentialCreateRequest;
import org.eclipse.slm.platform_management.service.api.credentials.CredentialManagementRestApi;
import org.eclipse.slm.platform_management.service.api.credentials.CredentialManagementRestApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(CredentialManagementRestApiConfig.BASE_PATH)
@Tag(name = CredentialManagementRestApiConfig.TAG)
public class CredentialManagementRestController implements CredentialManagementRestApi {

    private final static Logger LOG = LoggerFactory.getLogger(CredentialManagementRestController.class);

    private final CredentialsManager credentialsManager;

    public CredentialManagementRestController(CredentialsManager credentialsManager) {
        this.credentialsManager = credentialsManager;
    }

    @Override
    public ResponseEntity<CredentialReadDTO> getCredentialById(UUID credentialId) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        var credential = this.credentialsManager.getCredentialByIdForCurrentUser(credentialId, userAccessToken);
        return ResponseEntity.ok(credential);
    }

    @Override
    public ResponseEntity<Void> createCredential(CredentialCreateRequest credentialCreateRequest) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        credentialCreateRequest.getCredential().setId(UUID.randomUUID());
        this.credentialsManager.createCredential(
                credentialCreateRequest.getCredential(),
                credentialCreateRequest.getEntityLinks(),
                credentialCreateRequest.getFullPathOwnerGroupId());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> createOrUpdateCredential(UUID credentialId, CredentialCreateRequest credentialCreateRequest) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        credentialCreateRequest.getCredential().setId(credentialId);
        this.credentialsManager.createCredential(credentialCreateRequest.getCredential(),
                credentialCreateRequest.getEntityLinks(),
                credentialCreateRequest.getFullPathOwnerGroupId());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteCredential(UUID credentialId) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);

        this.credentialsManager.deleteCredentialForUser(credentialId, userAccessToken);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<CredentialReadDTO>> getCredentialsOfEntity(String entityId, String entityType) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        var credentials = this.credentialsManager.getCredentialsOfEntityForCurrentUser(entityId, entityType, userAccessToken);
        return ResponseEntity.ok(credentials);
    }

    @Override
    public ResponseEntity<List<CredentialReadDTO>> getCredentialsOfUser() {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        if (jwtAuthenticationToken.getToken().getClaims().get("groups") instanceof List<?> groupsClaimList) {
            @SuppressWarnings("unchecked")
            var userGroups = (List<String>) groupsClaimList;
            var credentials = this.credentialsManager.getAllCredentialsForCurrentUser(userGroups, userAccessToken);
            return ResponseEntity.ok(credentials);
        } else {
            throw new IllegalStateException("User groups claim is missing or invalid in the JWT token");
        }

    }

    @Override
    public ResponseEntity<Void> linkCredentialToEntity(UUID credentialId, org.eclipse.slm.common.credentials.model.CredentialEntityLinkCreateDTO entityLink) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        this.credentialsManager.getCredentialByIdForCurrentUser(credentialId, userAccessToken);
        this.credentialsManager.linkCredentialToEntity(credentialId, entityLink);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteCredentialEntityLink(UUID credentialId, String entityType, String entityId, boolean deleteIfOrphaned) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        this.credentialsManager.getCredentialByIdForCurrentUser(credentialId, userAccessToken);
        this.credentialsManager.deleteCredentialEntityLink(credentialId, entityType, entityId, deleteIfOrphaned);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> addCredentialScopes(UUID credentialId, List<String> scopes) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        this.credentialsManager.getCredentialByIdForCurrentUser(credentialId, userAccessToken);
        this.credentialsManager.addCredentialScopes(credentialId, scopes);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> removeCredentialScopes(UUID credentialId, List<String> scopes) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        this.credentialsManager.getCredentialByIdForCurrentUser(credentialId, userAccessToken);
        this.credentialsManager.removeCredentialScopes(credentialId, scopes);
        return ResponseEntity.ok().build();
    }
}
