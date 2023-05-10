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
import org.keycloak.KeycloakPrincipal;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<BasicResource> resources = new ArrayList<>();
        try {
            resources = this.resourcesManager.getResourcesWithCredentialsByRemoteAccessService(keycloakPrincipal);
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
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        BasicResource resource = this.resourcesManager.getResourceWithCredentialsByRemoteAccessService(
                keycloakPrincipal,
                resourceId
        );

        return ResponseEntity.ok(resource);
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    @Operation(summary = "Add existing resource")
    public @ResponseBody ResponseEntity<UUID> addExistingResource(
            @RequestParam(name = "resourceUsername", required = false)                              String resourceUsername,
            @RequestParam(name = "resourcePassword", required = false)                              String resourcePassword,
            @RequestParam(name = "resourceHostname")                                                String resourceHostname,
            @RequestParam(name = "resourceIp")                                                      String resourceIp,
            @RequestParam(name = "resourceConnectionType", required = false)                        ConnectionType connectionType,
            @RequestParam(name = "resourceConnectionPort", required = false, defaultValue = "0")    int connectionPort,
            @RequestParam(name = "resourceLocation", required = false)                              Optional<UUID> optionalLocationId,
            @RequestParam(name = "resourceBaseConfiguration", required = false)                     Optional<UUID> optionalResourceBaseConfigurationId
    ) throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, CapabilityNotFoundException, SSLException, JsonProcessingException {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UUID resourceId = UUID.randomUUID();
        this.resourcesManager.addExistingResource(
                keycloakPrincipal,
                resourceId,
                resourceHostname,
                resourceIp,
                optionalLocationId,
                resourceUsername,
                resourcePassword,
                connectionType,
                connectionPort,
                optionalResourceBaseConfigurationId
        );

        return new ResponseEntity(resourceId, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{resourceId}", method = RequestMethod.PUT)
    @Operation(summary = "Add existing resource with id")
    public @ResponseBody ResponseEntity addExistingResourceWithId(
            @PathVariable(name = "resourceId")                                                      UUID resourceId,
            @RequestParam(name = "resourceUsername", required = false)                              String resourceUsername,
            @RequestParam(name = "resourcePassword", required = false)                              String resourcePassword,
            @RequestParam(name = "resourceHostname")                                                String resourceHostname,
            @RequestParam(name = "resourceIp")                                                      String resourceIp,
            @RequestParam(name = "resourceConnectionType", required = false)                        ConnectionType connectionType,
            @RequestParam(name = "resourceConnectionPort", required = false, defaultValue = "0")    int connectionPort,
            @RequestParam(name = "resourceLocation", required = false)                              Optional<UUID> optionalLocationId,
            @RequestParam(name = "resourceBaseConfiguration", required = false)                     Optional<UUID> optionalResourceBaseConfigurationId
    ) throws ResourceDefinitionException, ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, CapabilityNotFoundException, SSLException, JsonProcessingException {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        this.resourcesManager.addExistingResource(
                keycloakPrincipal,
                resourceId,
                resourceHostname,
                resourceIp,
                optionalLocationId,
                resourceUsername,
                resourcePassword,
                connectionType,
                connectionPort,
                optionalResourceBaseConfigurationId
        );

        return new ResponseEntity(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{resourceId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete resource")
    public ResponseEntity deleteResource(@PathVariable(name = "resourceId") UUID resourceId)
            throws ConsulLoginFailedException, JsonProcessingException {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        this.resourcesManager.deleteResource(keycloakPrincipal, resourceId);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/connection-types", method = RequestMethod.GET)
    @Operation(summary="Get available remote connection types")
    public List<ConnectionTypeDTO> getResourceConnectionTypes() {
        return ConnectionTypeUtils.getRemoteConnectionTypeDTOs();
    }
}
