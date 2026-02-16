package org.eclipse.slm.resource_management.features.capabilities.clusters;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.eclipse.slm.common.consul.model.catalog.Service;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.dto.CapabilityDTOApi;
import org.eclipse.slm.resource_management.features.capabilities.dto.DeploymentCapabilityDTOApi;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityMapper;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.Cluster;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.ClusterCreateRequest;
import org.eclipse.slm.resource_management.features.capabilities.clusters.handler.ClusterHandler;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.SSLException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/resources/clusters")
@Tag(name = "Clusters")
public class ClustersRestController {
    private final static Logger LOG = LoggerFactory.getLogger(ClustersRestController.class);

    private final ClusterHandler clusterHandler;

    private final CapabilityJpaRepository capabilityJpaRepository;

    @Autowired
    public ClustersRestController(
            ClusterHandler clusterHandler, CapabilityJpaRepository capabilityJpaRepository
    ) {
        this.clusterHandler = clusterHandler;
        this.capabilityJpaRepository = capabilityJpaRepository;
    }

    @RequestMapping(value = "/types", method = RequestMethod.GET)
    @Operation(summary = "Get all available cluster types")
    public ResponseEntity<List<CapabilityDTOApi>> getClusterTypes() {
        List<Capability> capabilityList = capabilityJpaRepository.findAll();

        capabilityList = capabilityList
                .stream()
                .filter(c -> !c.getClusterMemberTypes().isEmpty())
                .toList();

        List<CapabilityDTOApi> capabilityDTOApiList = capabilityList
                .stream()
                .map(CapabilityMapper.INSTANCE::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(capabilityDTOApiList);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get all cluster resources of user")
    public @ResponseBody
    List<Cluster> getClusterResources() throws NotImplementedException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return clusterHandler.getClusters();
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @Operation(summary = "Create a cluster")
    public void createClusterResource(@RequestBody ClusterCreateRequest clusterCreateRequest)
            throws SSLException, ConsulLoginFailedException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        Optional<Capability> clusterCapability = capabilityJpaRepository.findById(clusterCreateRequest.getClusterTypeId());

        if(clusterCapability.isPresent()) {
            MultiHostCapabilityService multiHostCapabilityService = new MultiHostCapabilityService(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    clusterCapability.get(),
                    clusterCreateRequest.getClusterMembers(),
                    CapabilityServiceStatus.INSTALL,
                    clusterCreateRequest.getSkipInstall(),
                    new HashMap<>()
            );

            clusterHandler.create(
                    multiHostCapabilityService,
                    jwtAuthenticationToken,
                    clusterCreateRequest
            );
        }
    }

    @RequestMapping(value = "/{clusterUuid}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete cluster resource")
    public void deleteClusterResource(
            @PathVariable(name = "clusterUuid") UUID consulServiceUuid
    ) throws SSLException, ConsulLoginFailedException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        clusterHandler.delete(
                jwtAuthenticationToken,
                consulServiceUuid
        );
    }

    //TODO: Use ClusterUUID instead
    @RequestMapping(value = "/{clusterName}/members", method = RequestMethod.GET)
    @Operation(summary = "Get members of cluster")
    public List<Service> getClusterMembers(
            @PathVariable(name = "clusterName") String clusterName
    ) throws ConsulLoginFailedException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var clusterNodes = this.clusterHandler.getClusterMembers( clusterName);

        return clusterNodes;
    }

    @RequestMapping(value = "/{clusterUuid}/members", method = RequestMethod.POST)
    @Operation(summary = "Add new Member to cluster")
    public void addClusterMember(
            @PathVariable(name = "clusterUuid") UUID consulServiceUuid,
            @RequestParam(name = "resourceId") UUID resourceId
    ) throws SSLException, ConsulLoginFailedException, ResourceNotFoundException {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        clusterHandler.scaleUp(
                jwtAuthenticationToken,
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
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        clusterHandler.scaleDown(
                jwtAuthenticationToken,
                consulServiceUuid,
                resourceId
        );
    }
}
