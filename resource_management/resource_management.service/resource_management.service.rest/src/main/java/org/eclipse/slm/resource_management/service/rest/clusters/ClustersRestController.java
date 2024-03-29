package org.eclipse.slm.resource_management.service.rest.clusters;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesManager;
import org.eclipse.slm.resource_management.model.cluster.ClusterCreateRequest;
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.model.consul.capability.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.model.cluster.Cluster;
import org.eclipse.slm.common.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.notification_service.service.client.NotificationServiceClient;
import org.eclipse.slm.resource_management.model.capabilities.Capability;
import org.eclipse.slm.resource_management.model.capabilities.CapabilityDTOApi;
import org.eclipse.slm.resource_management.model.capabilities.DeploymentCapability;
import org.eclipse.slm.resource_management.model.capabilities.DeploymentCapabilityDTOApi;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.persistence.api.CapabilityJpaRepository;
import org.eclipse.slm.resource_management.service.rest.handler.ClusterHandler;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.NotImplementedException;
import org.keycloak.KeycloakPrincipal;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.SSLException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/resources/clusters")
public class ClustersRestController {
    private final static Logger LOG = LoggerFactory.getLogger(ClustersRestController.class);
    private final ResourcesManager resourcesManager;
    private final ClusterHandler clusterHandler;
    private final AwxJobExecutor awxJobExecutor;
    private final NotificationServiceClient notificationServiceClient;
    private final VaultClient vaultClient;
    @Autowired
    private CapabilityJpaRepository capabilityJpaRepository;
    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public ClustersRestController(
            ResourcesManager resourcesManager,
            ClusterHandler clusterHandler,
            AwxJobExecutor awxJobExecutor,
            NotificationServiceClient notificationServiceClient,
            VaultClient vaultClient
    ) {
        this.resourcesManager = resourcesManager;
        this.clusterHandler = clusterHandler;
        this.awxJobExecutor = awxJobExecutor;
        this.notificationServiceClient = notificationServiceClient;
        this.vaultClient = vaultClient;

        // DTO >>> Entity
        modelMapper.typeMap(DeploymentCapabilityDTOApi.class, Capability.class)
                .setProvider(provisionRequest -> modelMapper.map(provisionRequest.getSource(), DeploymentCapability.class));

        // Entity >>> DTO
        modelMapper.typeMap(DeploymentCapability.class, CapabilityDTOApi.class)
                .setProvider(provisionRequest -> modelMapper.map(provisionRequest.getSource(), DeploymentCapabilityDTOApi.class));
    }

    @RequestMapping(value = "/types", method = RequestMethod.GET)
    @Operation(summary = "Get all available cluster types")
    public ResponseEntity<List<CapabilityDTOApi>> getClusterTypes() {
        List<Capability> capabilityList = capabilityJpaRepository.findAll();

        capabilityList = capabilityList
                .stream()
                .filter(c -> c.getClusterMemberTypes().size() > 0)
                .collect(Collectors.toList());

        List<CapabilityDTOApi> capabilityDTOApiList = capabilityList
                .stream()
                .map(cap -> modelMapper.map(cap, CapabilityDTOApi.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(capabilityDTOApiList);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get all cluster resources of user")
    public @ResponseBody
    List<Cluster> getClusterResources() throws NotImplementedException {
        var keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return clusterHandler.getClusters(new ConsulCredential(keycloakPrincipal));
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @Operation(summary = "Create a cluster")
    public void createClusterResource(@RequestBody ClusterCreateRequest clusterCreateRequest)
            throws SSLException, ConsulLoginFailedException {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<Capability> clusterCapability = capabilityJpaRepository.findById(clusterCreateRequest.getClusterTypeId());

        if(clusterCapability.isPresent()) {
            MultiHostCapabilityService multiHostCapabilityService = new MultiHostCapabilityService(
                    clusterCapability.get(),
                    clusterCreateRequest.getClusterMembers(),
                    CapabilityServiceStatus.INSTALL,
                    clusterCreateRequest.getSkipInstall()
            );

            clusterHandler.create(
                    multiHostCapabilityService,
                    keycloakPrincipal,
                    clusterCreateRequest
            );
        }
    }

    @RequestMapping(value = "/{clusterUuid}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete cluster resource")
    public void deleteClusterResource(
            @PathVariable(name = "clusterUuid") UUID consulServiceUuid
    ) throws SSLException, ConsulLoginFailedException {
        clusterHandler.delete(
                (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                consulServiceUuid
        );
    }

    //TODO: Use ClusterUUID instead
    @RequestMapping(value = "/{clusterName}/members", method = RequestMethod.GET)
    @Operation(summary = "Get members of cluster")
    public List<CatalogService> getClusterMembers(
            @PathVariable(name = "clusterName") String clusterName
    ) throws ConsulLoginFailedException {
        KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var clusterNodes = this.clusterHandler.getClusterMembers(new ConsulCredential(keycloakPrincipal), clusterName);

        return clusterNodes;
    }

    @RequestMapping(value = "/{clusterUuid}/members", method = RequestMethod.POST)
    @Operation(summary = "Add new Member to cluster")
    public void addClusterMember(
            @PathVariable(name = "clusterUuid") UUID consulServiceUuid,
            @RequestParam(name = "resourceId") UUID resourceId
    ) throws SSLException, ConsulLoginFailedException, ResourceNotFoundException {
        clusterHandler.scaleUp(
                (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                consulServiceUuid,
                resourceId
        );
    }


    @RequestMapping(value = "/{clusterUuid}/members", method = RequestMethod.DELETE)
    @Operation(summary = "Remove member of cluster")
    public void removeClusterMember(
            @PathVariable(name = "clusterUuid") UUID consulServiceUuid,
            @RequestParam(name = "resourceId") UUID resourceId
    ) throws SSLException, ConsulLoginFailedException, ResourceNotFoundException {
        clusterHandler.scaleDown(
                (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                consulServiceUuid,
                resourceId
        );
    }
}
