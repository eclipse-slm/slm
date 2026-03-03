package org.eclipse.slm.common.consul.client;

import org.eclipse.slm.common.consul.client.auth.ConsulAuthentication;
import org.eclipse.slm.common.consul.client.utils.ConsulMapper;
import org.eclipse.slm.common.consul.model.catalog.CatalogDeregistration;

import org.eclipse.slm.common.consul.model.catalog.CatalogRegistration;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulNodeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Consul Nodes Client which provides functionality to interact with nodes in Consul catalog.
 */
public class ConsulNodesClient extends AbstractConsulClient {

    private final static Logger LOG = LoggerFactory.getLogger(ConsulNodesClient.class);

    public ConsulNodesClient(String consulUrl, ConsulAuthentication consulAuthentication) {
        super(consulUrl, consulAuthentication);
    }

    public List<Node> getNodes() {
        return this.getNodes(null);
    }

    public List<Node> getNodes(String filter) {
        var nodes = this.consulCatalogApiClient.listNodes(this.consulDatacenter, null, filter, null);

        return nodes;
    }

    public Node getNodeByIdOrThrow(UUID nodeId) {
        return this.getNodeById(nodeId).orElseThrow(() -> new ConsulNodeNotFoundException(nodeId));
    }

    public Optional<Node> getNodeById(UUID nodeId) {
        var idFilter = String.format("ID == `%s`", nodeId);
        var filteredGetNodes = this.getNodes(idFilter);

        if (filteredGetNodes.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(filteredGetNodes.get(0));
        }
    }

    public void registerEntity(CatalogRegistration catalogRegistration) {
        this.consulCatalogApiClient.registerEntity(catalogRegistration);
    }

    public void registerNode(Node node) {
        var catalogRegistration = ConsulMapper.INSTANCE.toCatalogRegistration(node);
        this.registerEntity(catalogRegistration);
    }

    public void deleteNodeByName(String nodeName) {
        var catalogDeregistration = new CatalogDeregistration(nodeName, null, null, null, null, null);
        this.consulCatalogApiClient.deregisterEntity(catalogDeregistration);
    }

    public void deleteNodeById(UUID nodeId) throws ConsulLoginFailedException {
        var node = this.getNodeByIdOrThrow(nodeId);
        var nodeName = node.getNodeName();
        this.deleteNodeByName(nodeName);
    }

    public void addMetaDataToNode(UUID nodeId, Map<String, String> additionalMetaData) {
        var node = this.getNodeById(nodeId).orElseThrow(() -> new ConsulNodeNotFoundException(nodeId));
        if (node.getMeta() != null) {
            node.getMeta().putAll(additionalMetaData);
        } else {
            node.setMeta(additionalMetaData);
        }

        this.registerNode(node);
    }
}
