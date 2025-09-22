package org.eclipse.slm.resource_management.features.capabilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotCreatedException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.dto.BaseConfigurationCapabilityDTOApi;
import org.eclipse.slm.resource_management.features.capabilities.dto.CapabilityDTOApi;
import org.eclipse.slm.resource_management.features.capabilities.dto.DeploymentCapabilityDTOApi;
import org.eclipse.slm.resource_management.features.capabilities.dto.VirtualizationCapabilityDTOApi;
import org.eclipse.slm.resource_management.features.capabilities.model.*;
import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.resource_management.features.capabilities.model.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/resources")
@Tag(name = "Capabilities")
public class CapabilitiesRestController {
    private final static Logger LOG = LoggerFactory.getLogger(CapabilitiesRestController.class);

    private final CapabilitiesService capabilitiesService;

    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public CapabilitiesRestController(CapabilitiesService capabilitiesService) {
        this.capabilitiesService = capabilitiesService;

        // DTO >>> Entity
        modelMapper.typeMap(DeploymentCapabilityDTOApi.class, Capability.class)
                .setProvider(provisionRequest -> modelMapper.map(provisionRequest.getSource(), DeploymentCapability.class));

        modelMapper.typeMap(VirtualizationCapabilityDTOApi.class, Capability.class)
                .setProvider(provisionRequest -> modelMapper.map(provisionRequest.getSource(), VirtualizationCapability.class));

        modelMapper.typeMap(BaseConfigurationCapabilityDTOApi.class, Capability.class)
                .setProvider(provisionRequest -> modelMapper.map(provisionRequest.getSource(), BaseConfigurationCapability.class));

        // Entity >>> DTO
        modelMapper.typeMap(DeploymentCapability.class, CapabilityDTOApi.class)
                .setProvider(provisionRequest -> modelMapper.map(provisionRequest.getSource(), DeploymentCapabilityDTOApi.class));

        modelMapper.typeMap(VirtualizationCapability.class, CapabilityDTOApi.class)
                .setProvider(provisionRequest -> modelMapper.map(provisionRequest.getSource(), VirtualizationCapabilityDTOApi.class));

        modelMapper.typeMap(BaseConfigurationCapability.class, CapabilityDTOApi.class)
                .setProvider(provisionRequest -> modelMapper.map(provisionRequest.getSource(), BaseConfigurationCapabilityDTOApi.class));
    }

    @RequestMapping(value = "/capabilities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get capabilities")
    public ResponseEntity<List<CapabilityDTOApi>> getCapabilities(Optional<CapabilityFilter> filter) {
        List<Capability> capabilityList;

        capabilityList = capabilitiesService.getCapabilities(filter);

        List<CapabilityDTOApi> capabilityDTOApiList = capabilityList
                .stream()
                .map(cap -> modelMapper.map(cap, CapabilityDTOApi.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(capabilityDTOApiList);
    }

    @RequestMapping(value = "/capabilities", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add capability")
    public ResponseEntity<Void> createCapability(
            @RequestBody CapabilityDTOApi capabilityDTOApi
    ) throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, ResourceNotCreatedException, JsonProcessingException {
        Capability capability = modelMapper.map(capabilityDTOApi, Capability.class);
        capabilitiesService.addCapability(capability);
        LOG.info("Added capability: " + capabilityDTOApi.toString());

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/capabilities/{capabilityId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete capability")
    public ResponseEntity<Object> deleteCapability(
            @PathVariable(name = "capabilityId") UUID capabilityId
    ) throws ConsulLoginFailedException {
        if (this.capabilitiesService.deleteCapability(capabilityId))
            return ResponseEntity.ok().build();
        else
        {
            LOG.info("Capability with uuid '" + capabilityId  + "' doesn't exist");
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/{resourceId}/capabilities/services", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get capability services of resource")
    public ResponseEntity<List<CapabilityServiceDTO>> getCapabilityServicesOfResource(
            @PathVariable(name = "resourceId") UUID resourceId
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var capabilityServices = capabilitiesService.getCapabilityServicesOfResource(resourceId);

        return ResponseEntity.ok(capabilityServices);
    }

    @RequestMapping(value = "/capabilities/services", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all capability services")
    public ResponseEntity<List<CapabilityServiceDTO>> getAllCapabilityServices(
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var capabilityServices = capabilitiesService.getAllCapabilityServices(jwtAuthenticationToken);

        return ResponseEntity.ok(capabilityServices);
    }


}
