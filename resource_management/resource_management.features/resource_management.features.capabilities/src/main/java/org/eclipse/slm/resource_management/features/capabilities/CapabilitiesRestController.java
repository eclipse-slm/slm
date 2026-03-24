package org.eclipse.slm.resource_management.features.capabilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotCreatedException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.dto.CapabilityDTOApi;
import org.eclipse.slm.resource_management.features.capabilities.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(CapabilitiesRestApiConfig.BASE_PATH)
@Tag(name = CapabilitiesRestApiConfig.TAG)
public class CapabilitiesRestController implements CapabilitiesRestApi {
    private final static Logger LOG = LoggerFactory.getLogger(CapabilitiesRestController.class);

    private final CapabilitiesManager capabilitiesService;

    @Autowired
    public CapabilitiesRestController(CapabilitiesManager capabilitiesService) {
        this.capabilitiesService = capabilitiesService;
    }

    public ResponseEntity<List<CapabilityDTOApi>> getCapabilities(Optional<CapabilityFilter> filter) {
        List<Capability> capabilityList;

        capabilityList = capabilitiesService.getCapabilities(filter);

        List<CapabilityDTOApi> capabilityDTOApiList = capabilityList
                .stream()
                .map(CapabilityMapper.INSTANCE::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(capabilityDTOApiList);
    }

    public ResponseEntity<Void> createCapability(
            CapabilityDTOApi capabilityDTOApi
    ) throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, ResourceNotCreatedException, JsonProcessingException {
        Capability capability = CapabilityMapper.INSTANCE.toModel(capabilityDTOApi);
        capabilitiesService.addCapability(capability);
        LOG.info("Added capability: " + capabilityDTOApi.toString());

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    public ResponseEntity<Object> deleteCapability(
            UUID capabilityId
    ) throws ConsulLoginFailedException {
        if (this.capabilitiesService.deleteCapability(capabilityId))
            return ResponseEntity.ok().build();
        else
        {
            LOG.info("Capability with uuid '" + capabilityId  + "' doesn't exist");
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<List<CapabilityServiceDTO>> getCapabilityServicesOfResource(
            UUID resourceId
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var capabilityServices = capabilitiesService.getCapabilityServicesOfResource(resourceId);

        return ResponseEntity.ok(capabilityServices);
    }

    public ResponseEntity<List<CapabilityServiceDTO>> getAllCapabilityServices(
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var capabilityServices = capabilitiesService.getAllCapabilityServices(jwtAuthenticationToken);

        return ResponseEntity.ok(capabilityServices);
    }


}
