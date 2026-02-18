package org.eclipse.slm.resource_management.common.resources;

import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class ResourcesRestController implements ResourcesRestApi {

    private final static Logger LOG = LoggerFactory.getLogger(ResourcesRestController.class);

    private final ResourcesManager resourcesManager;

    @Autowired
    public ResourcesRestController(ResourcesManager resourcesManager) {
        this.resourcesManager = resourcesManager;
    }

    @Override
    public ResponseEntity<List<ResourceDTO>> getResources() throws ResourceNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        var resources = this.resourcesManager.getResources(accessToken);
        var resourceDTOs = ResourceMapper.INSTANCE.toDto(resources);

        return ResponseEntity.ok(resourceDTOs);
    }

    @Override
    public ResponseEntity<ResourceDTO> getResource(UUID resourceId) throws ResourceNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);

        var resource = this.resourcesManager.getResourceByIdOrThrow(resourceId, accessToken);
        var resourceDTO = ResourceMapper.INSTANCE.toDto(resource);

        return ResponseEntity.ok(resourceDTO);
    }

    @Override
    public ResponseEntity<UUID> addExistingResource(CreateResourceRequest createResourceRequest) throws ResourceNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UUID resourceId = UUID.randomUUID();
        this.resourcesManager.createResource(
                resourceId,
                null,
                createResourceRequest.getResourceHostname(),
                createResourceRequest.getResourceIp(),
                null,
                null,
                createResourceRequest.getDigitalNameplateV3(),
                createResourceRequest.getFullPathOwnerGroupId()
        );

        return new ResponseEntity<>(resourceId, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> addExistingResourceWithId(UUID resourceId, CreateResourceRequest createResourceRequest) throws ResourceNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        this.resourcesManager.createResource(
                resourceId,
                null,
                createResourceRequest.getResourceHostname(),
                createResourceRequest.getResourceIp(),
                null,
                null,
                createResourceRequest.getDigitalNameplateV3(),
                createResourceRequest.getFullPathOwnerGroupId()
        );

        return new ResponseEntity(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteResource(UUID resourceId) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);

        this.resourcesManager.deleteResource(resourceId, accessToken);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> setLocationOfResource(UUID resourceId, UUID locationId) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);

        this.resourcesManager.setLocationOfResource(resourceId, locationId, accessToken);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> setConnectionParametersOfResource(UUID resourceId, String connectionParameters) {
        this.resourcesManager.setConnectionParametersOfResource(resourceId, connectionParameters);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> updateResource(UUID resourceId, ResourceUpdateRequest updateResourceRequest) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);

        this.resourcesManager.updateResource(resourceId, updateResourceRequest, accessToken);
        return ResponseEntity.ok().build();
    }
}
