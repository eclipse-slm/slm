package org.eclipse.slm.common.consul.client;

import org.eclipse.slm.common.consul.client.auth.ConsulAuthentication;
import org.eclipse.slm.common.consul.model.catalog.CatalogDeregistration;
import org.eclipse.slm.common.consul.model.catalog.CatalogRegistration;
import org.eclipse.slm.common.consul.model.health.Check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Consul Health Client which provides functionality to interact with Consul health checks.
 */
public class ConsulHealthClient extends AbstractConsulClient {

    private final static Logger LOG = LoggerFactory.getLogger(ConsulHealthClient.class);

    /**
     * Consul check ID for Serf health checks.
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/health#sample-response-3">Consul Serf Health Check Documentation</a>.
     */
    public final static String CHECK_ID_SERF = "serfHealth";

    private final ConsulNodesClient consulNodesClient;

    public ConsulHealthClient(String consulUrl, ConsulAuthentication consulAuthentication, ConsulNodesClient consulNodesClient) {
        super(consulUrl, consulAuthentication);
        this.consulNodesClient = consulNodesClient;
    }

    public List<Check> getChecksOfNode(UUID nodeId) {
        var node = this.consulNodesClient.getNodeByIdOrThrow(nodeId);
        var nodeChecks = this.consulHealthApiClient.listChecksForNode(node.getNodeName(), null, null, null);
        return nodeChecks;
    }

    public List<Check> getSerfHealthChecksOfNode(UUID nodeId) {
        var nodeChecks = this.getChecksOfNode(nodeId);
        var nodeSerfChecks = nodeChecks.stream().filter(check -> check.getCheckId().equals(CHECK_ID_SERF)).toList();
        return nodeSerfChecks;
    }

    public void addCheckToService(String nodeName, CatalogRegistration.Check check) {
        var catalogRegistration = new CatalogRegistration.Builder()
                .nodeName(nodeName)
                .skipNodeUpdate(true)
                .check(check)
                .build();
        this.consulCatalogApiClient.registerEntity(catalogRegistration);
    }

    public void removeCheckFromNode(String nodeName, String checkId) {
        var catalogDeregistration = new CatalogDeregistration.Builder(nodeName)
                .checkId(checkId)
                .build();
        this.consulCatalogApiClient.deregisterEntity(catalogDeregistration);
    }

    public void removeCheckFromNode(UUID nodeId, String checkId) {
        var node = this.consulNodesClient.getNodeByIdOrThrow(nodeId);
        removeCheckFromNode(node.getNodeName(), checkId);
    }
}
