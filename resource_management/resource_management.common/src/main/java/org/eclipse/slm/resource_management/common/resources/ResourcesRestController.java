package org.eclipse.slm.resource_management.common.resources;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.resource_types.ResourceTypesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(ResourcesRestControllerConfig.BASE_PATH)
@Tag(name = ResourcesRestControllerConfig.TAG)
public class ResourcesRestController {

    private final static Logger LOG = LoggerFactory.getLogger(ResourcesRestController.class);

    private final ResourcesManager resourcesManager;

    @Autowired
    public ResourcesRestController(
            ResourcesManager resourcesManager, ResourceTypesManager resourceTypeSManager
    ) {
        this.resourcesManager = resourcesManager;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get all resources")
    public ResponseEntity<List<ResourceDTO>> getResources() throws ResourceNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var resources = this.resourcesManager.getResources(jwtAuthenticationToken);
        var resourceDTOs = ResourceMapper.INSTANCE.toDto(resources);

        return ResponseEntity.ok(resourceDTOs);
    }

    @RequestMapping(value = "/{resourceId}", method = RequestMethod.GET)
    @Operation(summary = "Get resource")
    public ResponseEntity<ResourceDTO> getResource(
            @PathVariable(name = "resourceId") UUID resourceId
    ) throws ResourceNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var resource = this.resourcesManager.getResourceByIdOrThrow(
                jwtAuthenticationToken,
                resourceId
        );
        var resourceDTO = ResourceMapper.INSTANCE.toDto(resource);

        return ResponseEntity.ok(resourceDTO);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @Operation(summary = "Add existing resource")
    public @ResponseBody ResponseEntity<UUID> addExistingResource(
            @RequestBody CreateResourceRequest createResourceRequest
    ) throws ResourceNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UUID resourceId = UUID.randomUUID();
        this.resourcesManager.addResource(
                jwtAuthenticationToken,
                resourceId,
                null,
                createResourceRequest.getResourceHostname(),
                createResourceRequest.getResourceIp(),
                null,
                null,
                createResourceRequest.getDigitalNameplateV3()
        );

        return new ResponseEntity<>(resourceId, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{resourceId}", method = RequestMethod.PUT)
    @Operation(summary = "Add existing resource with id")
    public ResponseEntity addExistingResourceWithId(
            @PathVariable(name = "resourceId") UUID resourceId,
            @RequestBody CreateResourceRequest createResourceRequest
    ) throws  ResourceNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        this.resourcesManager.addResource(
                jwtAuthenticationToken,
                resourceId,
                null,
                createResourceRequest.getResourceHostname(),
                createResourceRequest.getResourceIp(),
                null,
                null,
                createResourceRequest.getDigitalNameplateV3()
        );

        return new ResponseEntity(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{resourceId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete resource")
    public ResponseEntity deleteResource(@PathVariable(name = "resourceId") UUID resourceId) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        this.resourcesManager.deleteResource(jwtAuthenticationToken, resourceId);

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

    @RequestMapping(value = "/{resourceId}/connection-parameters", method = RequestMethod.PUT)
    @Operation(summary = "Set connection parameters of resource with id")
    public @ResponseBody ResponseEntity setConnectionParametersOfResource(
            @PathVariable(name = "resourceId")  UUID resourceId,
            @RequestParam(name = "connectionParameters", required = false)  String connectionParameters) {

        this.resourcesManager.setConnectionParametersOfResource(resourceId, connectionParameters);

        return ResponseEntity.ok().build();
    }
}
