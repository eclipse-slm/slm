package org.eclipse.slm.resource_management.service.rest.capabilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.model.capabilities.*;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceInternalErrorException;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotCreatedException;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesManager;
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityService;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import org.keycloak.KeycloakPrincipal;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/resources")
public class CapabilitiesRestController {
    private final static Logger LOG = LoggerFactory.getLogger(CapabilitiesRestController.class);
    private final ResourcesManager resourcesManager;
    private final CapabilitiesManager capabilitiesManager;
    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public CapabilitiesRestController(
            ResourcesManager resourcesManager,
            CapabilitiesManager capabilitiesManager
    ) {
        this.resourcesManager = resourcesManager;
        this.capabilitiesManager = capabilitiesManager;

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

        capabilityList = capabilitiesManager.getCapabilities(filter);

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
    ) throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, ResourceNotCreatedException, JsonProcessingException, ResourceInternalErrorException {
        Capability capability = modelMapper.map(capabilityDTOApi, Capability.class);
        capabilitiesManager.addCapability(capability);
        LOG.info("Added capability: " + capabilityDTOApi.toString());

        return new ResponseEntity(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/capabilities/{capabilityId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete capability")
    public ResponseEntity<Object> deleteCapability(
            @PathVariable(name = "capabilityId") UUID capabilityId
    ) throws ConsulLoginFailedException {
        if (this.capabilitiesManager.deleteCapability(capabilityId))
            return ResponseEntity.ok().build();
        else
        {
            LOG.info("Capability with uuid '" + capabilityId  + "' doesn't exist");
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/{resourceId}/deployment-capabilities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get deployment capabilities of resource")
    public ResponseEntity<List<DeploymentCapability>> getDeploymentCapabilitiesOfResource(
            @PathVariable(name = "resourceId")           UUID resourceId
    ) throws ResourceNotFoundException, ConsulLoginFailedException {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // ToDo: K3S - Check if endpoint works for clusters
        var deploymentCapabilitiesOfResource =
                this.capabilitiesManager.getDeploymentCapabilitiesOfResource(
                        resourceId,
                        new ConsulCredential(keycloakPrincipal)
                );

        return ResponseEntity.ok(deploymentCapabilitiesOfResource);
    }

    @RequestMapping(value = "/{resourceId}/capability-services", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get capability services of resource")
    public ResponseEntity<List<CapabilityService>> getCapabilityServicesOfResource(
            @PathVariable(name = "resourceId") UUID resourceId
    ) throws ConsulLoginFailedException {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<CapabilityService> capabilityServices = capabilitiesManager
                .getCapabilityServicesOfResourceById(resourceId, new ConsulCredential(keycloakPrincipal));

        return ResponseEntity.ok(capabilityServices);
    }

    @RequestMapping(value = "/{resourceId}/capabilities", method = RequestMethod.PUT)
    @Operation(summary = "Install capability on resource")
    public ResponseEntity installCapabilityOnSingleHost(
            @PathVariable(name = "resourceId")                                                   UUID resourceId,
            @RequestParam(name = "capabilityId")                                                 UUID capabilityId,
            @RequestParam(name = "skipInstall", required = false, defaultValue = "false")        Boolean skipInstall,
            @RequestBody                                                                         Map<String, String> configParameters
    ) throws ResourceNotFoundException, SSLException, ConsulLoginFailedException, CapabilityNotFoundException, JsonProcessingException, IllegalAccessException {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        this.capabilitiesManager.installCapabilityOnResource(
                resourceId,
                capabilityId,
                skipInstall,
                configParameters,
                keycloakPrincipal
        );

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value="/{resourceId}/capabilities", method = RequestMethod.DELETE)
    @Operation(summary = "Uninstall capability from resource")
    public ResponseEntity removeCapabilityFromSingleHost(
            @PathVariable(name = "resourceId")           UUID resourceId,
            @RequestParam(name = "capabilityId")      UUID capabilityId
    ) throws SSLException, ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        this.capabilitiesManager.uninstallCapabilityFromResource(
                resourceId,
                capabilityId,
                keycloakPrincipal
        );

        return ResponseEntity.ok().build();
    }
}
