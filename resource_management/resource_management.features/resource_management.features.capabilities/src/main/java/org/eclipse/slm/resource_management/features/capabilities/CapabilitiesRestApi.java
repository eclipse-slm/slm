package org.eclipse.slm.resource_management.features.capabilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotCreatedException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.dto.CapabilityDTOApi;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityFilter;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface CapabilitiesRestApi {

    @RequestMapping(value = "/capabilities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get capabilities")
    ResponseEntity<List<CapabilityDTOApi>> getCapabilities(Optional<CapabilityFilter> filter);

    @RequestMapping(value = "/capabilities", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add capability")
    ResponseEntity<Void> createCapability(
            @RequestBody CapabilityDTOApi capabilityDTOApi
    ) throws ResourceNotFoundException, IllegalAccessException, ResourceNotCreatedException, JsonProcessingException;

    @RequestMapping(value = "/capabilities/{capabilityId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete capability")
    ResponseEntity<Object> deleteCapability(
            @PathVariable(name = "capabilityId") UUID capabilityId
    );

    @RequestMapping(value = "/{resourceId}/capabilities/services", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get capability services of resource")
    ResponseEntity<List<CapabilityServiceDTO>> getCapabilityServicesOfResource(
            @PathVariable(name = "resourceId") UUID resourceId
    );

    @RequestMapping(value = "/capabilities/services", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all capability services")
    ResponseEntity<List<CapabilityServiceDTO>> getAllCapabilityServices();
}

