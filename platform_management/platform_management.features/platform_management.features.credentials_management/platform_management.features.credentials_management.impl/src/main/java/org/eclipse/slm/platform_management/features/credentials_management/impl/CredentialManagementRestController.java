package org.eclipse.slm.platform_management.features.credentials_management.impl;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.credentials.CredentialsManager;
import org.eclipse.slm.common.credentials.model.CredentialData;
import org.eclipse.slm.common.credentials.model.CredentialEntityLinkCreateDTO;
import org.eclipse.slm.common.credentials.model.CredentialReadDTO;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.platform_management.features.credentials_management.api.CredentialCreateRequest;
import org.eclipse.slm.platform_management.features.credentials_management.api.CredentialManagementRestApi;
import org.eclipse.slm.platform_management.features.credentials_management.api.CredentialManagementRestApiConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(CredentialManagementRestApiConfig.BASE_PATH)
@Tag(name = CredentialManagementRestApiConfig.TAG)
public class CredentialManagementRestController implements CredentialManagementRestApi {

    private final CredentialsManager credentialsManager;

    public CredentialManagementRestController(CredentialsManager credentialsManager) {
        this.credentialsManager = credentialsManager;
    }

    @Override
    public ResponseEntity<CredentialReadDTO> getCredentialById(UUID credentialId) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        var credential = this.credentialsManager.getCredentialByIdForUser(credentialId, userAccessToken);
        return ResponseEntity.ok(credential);
    }

    @Override
    @PreAuthorize("authentication.tokenAttributes['client_id'] == 'resource_management'")
    public ResponseEntity<CredentialData> getCredentialDataById(UUID credentialId, String impersonatedGroupId) {
        var credentialData = this.credentialsManager.getCredentialDataById(credentialId, impersonatedGroupId);
        return ResponseEntity.ok(credentialData);
    }

    @Override
    public ResponseEntity<Void> createCredential(CredentialCreateRequest credentialCreateRequest) {
        credentialCreateRequest.getCredential().setId(UUID.randomUUID());
        this.credentialsManager.createCredential(
                credentialCreateRequest.getCredential(),
                credentialCreateRequest.getEntityLinks(),
                credentialCreateRequest.getFullPathOwnerGroupId());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> createOrUpdateCredential(UUID credentialId, CredentialCreateRequest credentialCreateRequest) {
        credentialCreateRequest.getCredential().setId(credentialId);
        this.credentialsManager.createCredential(
                credentialCreateRequest.getCredential(),
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
        var credentials = this.credentialsManager.getCredentialsOfEntityForUser(entityId, entityType, userAccessToken);
        return ResponseEntity.ok(credentials);
    }

    @Override
    public ResponseEntity<List<CredentialReadDTO>> getCredentialsOfUser() {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        if (jwtAuthenticationToken.getToken().getClaims().get("groups") instanceof List<?> groupsClaimList) {
            @SuppressWarnings("unchecked")
            var userGroups = (List<String>) groupsClaimList;
            var credentials = this.credentialsManager.getAllCredentialsForUser(userGroups, userAccessToken);
            return ResponseEntity.ok(credentials);
        }

        throw new IllegalStateException("User groups claim is missing or invalid in the JWT token");
    }

    @Override
    public ResponseEntity<Void> linkCredentialToEntity(UUID credentialId, CredentialEntityLinkCreateDTO entityLink) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        this.credentialsManager.getCredentialByIdForUser(credentialId, userAccessToken);
        this.credentialsManager.linkCredentialToEntity(credentialId, entityLink);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteCredentialEntityLink(UUID credentialId, String entityType, String entityId, boolean deleteIfOrphaned) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        this.credentialsManager.getCredentialByIdForUser(credentialId, userAccessToken);
        this.credentialsManager.deleteCredentialEntityLink(credentialId, entityType, entityId, deleteIfOrphaned);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> addCredentialScopes(UUID credentialId, List<String> scopes) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        this.credentialsManager.getCredentialByIdForUser(credentialId, userAccessToken);
        this.credentialsManager.addCredentialScopes(credentialId, scopes);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> removeCredentialScopes(UUID credentialId, List<String> scopes) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        this.credentialsManager.getCredentialByIdForUser(credentialId, userAccessToken);
        this.credentialsManager.removeCredentialScopes(credentialId, scopes);
        return ResponseEntity.ok().build();
    }
}


