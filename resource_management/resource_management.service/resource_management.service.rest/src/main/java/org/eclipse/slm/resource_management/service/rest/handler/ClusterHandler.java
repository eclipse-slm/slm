package org.eclipse.slm.resource_management.service.rest.handler;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.model.cluster.Cluster;
import org.eclipse.slm.resource_management.model.cluster.ClusterCreateRequest;
import org.eclipse.slm.resource_management.model.consul.capability.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.service.rest.handler.ClusterJob;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            KeycloakPrincipal keycloakPrincipal,
            ClusterCreateRequest clusterCreateRequest
    ) throws SSLException, ConsulLoginFailedException {
        return this.clusterCreateFunctions.create(multiHostCapabilityService, keycloakPrincipal, clusterCreateRequest);
    }

    public void delete(
            KeycloakPrincipal keycloakPrincipal,
            UUID consulServiceUuid
    ) throws SSLException, ConsulLoginFailedException {
        this.clusterDeleteFunctions.delete(keycloakPrincipal, consulServiceUuid);
    }

    public int scaleUp(
            KeycloakPrincipal keycloakPrincipal,
            UUID consulServiceUuid,
            UUID resourceId
    ) throws ConsulLoginFailedException, SSLException, ResourceNotFoundException {
        return this.clusterScaleFunctions.scaleUp(keycloakPrincipal, consulServiceUuid, resourceId);
    }

    public int scaleDown(
            KeycloakPrincipal keycloakPrincipal,
            UUID consulServiceUuid,
            UUID resourceId
    ) throws SSLException, ConsulLoginFailedException, ResourceNotFoundException {
        return this.clusterScaleFunctions.scaleDown(keycloakPrincipal, consulServiceUuid, resourceId);
    }
}
