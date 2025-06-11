package org.eclipse.slm.resource_management.service.rest.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.model.capabilities.*;
import org.eclipse.slm.resource_management.model.resource.ConnectionType;
import org.eclipse.slm.resource_management.model.resource.ConnectionTypeDTO;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceDefinitionException;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.model.resource.BasicResource;
import org.eclipse.slm.resource_management.service.rest.utils.ConnectionTypeUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/resources")
public class ResourcesRestController {

    private final static Logger LOG = LoggerFactory.getLogger(ResourcesRestController.class);

    private final ResourcesManager resourcesManager;

    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public ResourcesRestController(
            ResourcesManager resourcesManager
    ) {
        this.resourcesManager = resourcesManager;

        // DTO >>> Entity
        modelMapper.typeMap(DeploymentCapabilityDTOApi.class, Capability.class)
                .setProvider(provisionRequest -> modelMapper.map(provisionRequest.getSource(), DeploymentCapability.class));

        // Entity >>> DTO
        modelMapper.typeMap(DeploymentCapability.class, CapabilityDTOApi.class)
                .setProvider(provisionRequest -> modelMapper.map(provisionRequest.getSource(), DeploymentCapabilityDTOApi.class));
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get all resources")
    public ResponseEntity<List<BasicResource>> getResources() throws JsonProcessingException, ResourceNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        List<BasicResource> resources = new ArrayList<>();
        try {
            resources = this.resourcesManager.getResourcesWithCredentialsByRemoteAccessService(jwtAuthenticationToken);
        } catch (ConsulLoginFailedException e) {
            LOG.warn(e.getMessage());
        }

        return ResponseEntity.ok(resources);
    }

    @RequestMapping(value = "/{resourceId}", method = RequestMethod.GET)
    @Operation(summary = "Get resource")
    public ResponseEntity<BasicResource> getResource(
            @PathVariable(name = "resourceId") UUID resourceId
    ) throws ResourceNotFoundException, ConsulLoginFailedException, JsonProcessingException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        BasicResource resource = this.resourcesManager.getResourceWithCredentialsByRemoteAccessService(
                jwtAuthenticationToken,
                resourceId
        );

        return ResponseEntity.ok(resource);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @Operation(summary = "Add existing resource")
    public @ResponseBody ResponseEntity<UUID> addExistingResource(
            @RequestBody CreateResourceRequest createResourceRequest
    ) throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, CapabilityNotFoundException, SSLException, JsonProcessingException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UUID resourceId = UUID.randomUUID();
        this.resourcesManager.addExistingResource(
                jwtAuthenticationToken,
                resourceId,
                null,
                createResourceRequest.getResourceHostname(),
                createResourceRequest.getResourceIp(),
                null,
                createResourceRequest.getDigitalNameplateV3()
        );

        return new ResponseEntity(resourceId, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{resourceId}", method = RequestMethod.PUT)
    @Operation(summary = "Add existing resource with id")
    public ResponseEntity addExistingResourceWithId(
            @PathVariable(name = "resourceId") UUID resourceId,
            @RequestBody CreateResourceRequest createResourceRequest
    ) throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, CapabilityNotFoundException, SSLException, JsonProcessingException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        this.resourcesManager.addExistingResource(
                jwtAuthenticationToken,
                resourceId,
                null,
                createResourceRequest.getResourceHostname(),
                createResourceRequest.getResourceIp(),
                null,
                createResourceRequest.getDigitalNameplateV3()
        );

        return new ResponseEntity(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{resourceId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete resource")
    public ResponseEntity deleteResource(@PathVariable(name = "resourceId") UUID resourceId)
            throws ConsulLoginFailedException, JsonProcessingException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        this.resourcesManager.deleteResource(jwtAuthenticationToken, resourceId);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{resourceId}/remote-access", method = RequestMethod.PUT)
    @Operation(summary = "Set remote access of resource with id")
    public @ResponseBody ResponseEntity setRemoteAccessOfResource(
        @PathVariable(name = "resourceId")  UUID resourceId,
        @RequestParam(name = "resourceUsername", required = false)                              String resourceUsername,
        @RequestParam(name = "resourcePassword", required = false)                              String resourcePassword,
        @RequestParam(name = "resourceConnectionType", required = false)                        ConnectionType connectionType,
        @RequestParam(name = "resourceConnectionPort", required = false, defaultValue = "0")    int connectionPort)
            throws ConsulLoginFailedException {

        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        this.resourcesManager.setRemoteAccessOfResource(jwtAuthenticationToken,
                resourceId, resourceUsername, resourcePassword, connectionType, connectionPort);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{resourceId}/location", method = RequestMethod.PUT)
    @Operation(summary = "Set location of resource with id")
    public @ResponseBody ResponseEntity setLocationOfResource(
            @PathVariable(name = "resourceId")  UUID resourceId,
            @RequestParam(name = "locationId", required = false)  UUID locationId) throws ConsulLoginFailedException {

        this.resourcesManager.setLocationOfResource(resourceId, locationId);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/connection-types", method = RequestMethod.GET)
    @Operation(summary="Get available remote connection types")
    public List<ConnectionTypeDTO> getResourceConnectionTypes() {
        return ConnectionTypeUtils.getRemoteConnectionTypeDTOs();
    }
}
