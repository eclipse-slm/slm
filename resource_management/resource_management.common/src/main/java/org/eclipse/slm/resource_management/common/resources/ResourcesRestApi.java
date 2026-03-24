package org.eclipse.slm.resource_management.common.resources;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceDefinitionException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

public interface ResourcesRestApi {

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get all resources")
    ResponseEntity<List<ResourceDTO>> getResources() throws ResourceNotFoundException;

    @RequestMapping(value = "/{resourceId}", method = RequestMethod.GET)
    @Operation(summary = "Get resource")
    ResponseEntity<ResourceDTO> getResource(
            @PathVariable(name = "resourceId") UUID resourceId
    ) throws ResourceNotFoundException;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @Operation(summary = "Add existing resource")
    ResponseEntity<UUID> addExistingResource(
            @RequestBody CreateResourceRequest createResourceRequest
    ) throws ResourceNotFoundException, ResourceDefinitionException;

    @RequestMapping(value = "/{resourceId}", method = RequestMethod.PUT)
    @Operation(summary = "Add existing resource with id")
    ResponseEntity<Void> addExistingResourceWithId(
            @PathVariable(name = "resourceId") UUID resourceId,
            @RequestBody CreateResourceRequest createResourceRequest
    ) throws ResourceNotFoundException, ResourceDefinitionException;

    @RequestMapping(value = "/{resourceId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete resource")
    ResponseEntity<Void> deleteResource(
            @PathVariable(name = "resourceId") UUID resourceId
    );

    @RequestMapping(value = "/{resourceId}/location", method = RequestMethod.PUT)
    @Operation(summary = "Set location of resource with id")
    ResponseEntity<Void> setLocationOfResource(
            @PathVariable(name = "resourceId") UUID resourceId,
            @RequestParam(name = "locationId", required = false) UUID locationId
    ) throws ConsulLoginFailedException;

    @RequestMapping(value = "/{resourceId}/connection-parameters", method = RequestMethod.PUT)
    @Operation(summary = "Set connection parameters of resource with id")
    ResponseEntity<Void> setConnectionParametersOfResource(
            @PathVariable(name = "resourceId") UUID resourceId,
            @RequestParam(name = "connectionParameters", required = false) String connectionParameters
    );

    @RequestMapping(value = "/{resourceId}", method = RequestMethod.PATCH)
    @Operation(summary = "Update selected properties of resource")
    ResponseEntity<Void> updateResource(
            @PathVariable(name = "resourceId") UUID resourceId,
            @RequestBody ResourceUpdateRequest resourceUpdateRequest
    );
}
