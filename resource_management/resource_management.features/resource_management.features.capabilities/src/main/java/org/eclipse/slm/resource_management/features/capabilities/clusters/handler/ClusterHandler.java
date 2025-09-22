package org.eclipse.slm.resource_management.features.capabilities.clusters.handler;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.Cluster;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.ClusterCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.UUID;

@Component
public class ClusterHandler {

    private final static Logger LOG = LoggerFactory.getLogger(ClusterHandler.class);

    private final ClusterCreateFunctions clusterCreateFunctions;

    private final ClusterDeleteFunctions clusterDeleteFunctions;

    private final ClusterScaleFunctions clusterScaleFunctions;

    private  final ClusterGetFunctions clusterGetFunctions;

    public ClusterHandler(ClusterCreateFunctions clusterCreateFunctions, ClusterDeleteFunctions clusterDeleteFunctions, ClusterScaleFunctions clusterScaleFunctions, ClusterGetFunctions clusterGetFunctions) {
        this.clusterCreateFunctions = clusterCreateFunctions;
        this.clusterDeleteFunctions = clusterDeleteFunctions;
        this.clusterScaleFunctions = clusterScaleFunctions;
        this.clusterGetFunctions = clusterGetFunctions;
    }

    public List<Cluster> getClusters(ConsulCredential consulCredential) {
        return this.clusterGetFunctions.getClusters(consulCredential);
    }

    public List<CatalogService> getClusterMembers(ConsulCredential consulCredential, String clusterName)
            throws ConsulLoginFailedException {
        return this.clusterGetFunctions.getClusterMembers(consulCredential, clusterName);
    }

    public ClusterJob create(
            MultiHostCapabilityService multiHostCapabilityService,
            JwtAuthenticationToken jwtAuthenticationToken,
            ClusterCreateRequest clusterCreateRequest
    ) throws SSLException, ConsulLoginFailedException {
        return this.clusterCreateFunctions.create(multiHostCapabilityService, jwtAuthenticationToken, clusterCreateRequest);
    }

    public void delete(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID consulServiceUuid
    ) throws SSLException, ConsulLoginFailedException {
        this.clusterDeleteFunctions.delete(jwtAuthenticationToken, consulServiceUuid);
    }

    public int scaleUp(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID consulServiceUuid,
            UUID resourceId
    ) throws ConsulLoginFailedException, SSLException, ResourceNotFoundException {
        return this.clusterScaleFunctions.scaleUp(jwtAuthenticationToken, consulServiceUuid, resourceId);
    }

    public int scaleDown(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID consulServiceUuid,
            UUID resourceId
    ) throws SSLException, ConsulLoginFailedException, ResourceNotFoundException {
        return this.clusterScaleFunctions.scaleDown(jwtAuthenticationToken, consulServiceUuid, resourceId);
    }
}
