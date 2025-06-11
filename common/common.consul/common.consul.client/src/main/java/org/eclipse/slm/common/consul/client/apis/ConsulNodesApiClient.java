package org.eclipse.slm.common.consul.client.apis;

import com.orbitz.consul.model.catalog.CatalogRegistration;
import com.orbitz.consul.model.catalog.ImmutableCatalogDeregistration;
import com.orbitz.consul.model.catalog.ImmutableCatalogRegistration;
import com.orbitz.consul.model.catalog.TaggedAddresses;
import com.orbitz.consul.option.ImmutableQueryOptions;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.utils.ConsulObjectMapper;
import org.eclipse.slm.common.consul.model.catalog.CatalogNode;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class ConsulNodesApiClient extends AbstractConsulApiClient {

    public final static Logger LOG = LoggerFactory.getLogger(ConsulNodesApiClient.class);
    private final ConsulAclApiClient consulAclApiClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public ConsulNodesApiClient(
                                @Value("${consul.scheme}")       String consulScheme,
                                @Value("${consul.host}")         String consulHost,
                                @Value("${consul.port}")         int consulPort,
                                @Value("${consul.acl-token}")    String consulToken,
                                @Value("${consul.datacenter}")   String consulDatacenter,
                                ConsulAclApiClient               consulAclApiClient
    ) {
        super(consulScheme, consulHost, consulPort, consulToken, consulDatacenter);
        this.consulAclApiClient = consulAclApiClient;
    }

    public List<Node> getNodes(ConsulCredential consulCredential)
            throws ConsulLoginFailedException {
        var nodes = this.getConsulClient(consulCredential).catalogClient().getNodes().getResponse();
        return ConsulObjectMapper.mapAll(nodes, Node.class);
    }

    public Optional<Node> getNodeById(ConsulCredential consulCredential, UUID nodeId)
            throws ConsulLoginFailedException {
        var filterId = String.format("ID == `%s`", nodeId);

        var catalogNodeListResponse = this.getConsulClient(consulCredential).catalogClient().getNodes(ImmutableQueryOptions.builder().filter(filterId).build());

        var catalogNodeList = catalogNodeListResponse.getResponse();

        if (catalogNodeList.size() == 0) {
            return Optional.empty();
        } else {
            var n = ConsulObjectMapper.map(catalogNodeList.get(0), Node.class);
            return Optional.of(n);
        }
    }

    public Node getNodeByName(ConsulCredential consulCredential, String nodeName) throws ConsulLoginFailedException {
        com.orbitz.consul.model.health.Node getNode = this.getConsulClient(consulCredential).catalogClient().getNode(nodeName).getResponse().getNode();
        Optional<TaggedAddresses> optionalTaggedAddresses = getNode.getTaggedAddresses();
        org.eclipse.slm.common.consul.model.catalog.TaggedAddresses taggedAddresses = new org.eclipse.slm.common.consul.model.catalog.TaggedAddresses();

        if(optionalTaggedAddresses.isPresent()) {
            if(optionalTaggedAddresses.get().getLan().isPresent())
                taggedAddresses.setLan(optionalTaggedAddresses.get().getLan().get());
            if(optionalTaggedAddresses.get().getWan().isPresent())
                taggedAddresses.setWan(optionalTaggedAddresses.get().getWan().get());
        }

        Node node = new Node();
        node.setId(getNode.getId().get());
        node.setNode(getNode.getNode());
        node.setDatacenter(String.valueOf(getNode.getDatacenter()));
        node.setAddress(getNode.getAddress());
        node.setTaggedAddresses(taggedAddresses);
        if (getNode.getNodeMeta().isPresent()) {
            node.setMeta(getNode.getNodeMeta().get());
        }

        return node;
    }

    public void registerNode(ConsulCredential consulCredential, CatalogNode node)
            throws ConsulLoginFailedException {
        var catalogRegistration = ConsulObjectMapper.map(node, CatalogRegistration.class);
        this.getConsulClient(consulCredential).catalogClient().register(catalogRegistration);
        this.consulAclApiClient.addReadAccessViaKeycloakRole(node.getId(), node.getNode(), "node");
    }

    public void deleteNode(ConsulCredential consulCredential, String consulNodeName)
            throws ConsulLoginFailedException {
        var deregistration = ImmutableCatalogDeregistration.builder()
                .setNode(consulNodeName)
                .build();
        this.getConsulClient(consulCredential).catalogClient().deregister(deregistration);
    }

    public void addMetaDataToNode(ConsulCredential consulCredential, UUID nodeId, Map<String, String> additionalMetaData)
            throws ConsulLoginFailedException {
        var node = this.getNodeById(consulCredential, nodeId).orElseThrow();
        node.getMeta().putAll(additionalMetaData);

        var catalogRegistration = ImmutableCatalogRegistration.builder()
                .putAllNodeMeta(node.getMeta())
                .setNode(node.getNode())
                .setAddress(node.getAddress())
                .setDatacenter(node.getDatacenter())
                .setId(node.getId().toString())
                .build();
        this.getConsulClient(consulCredential).catalogClient().register(catalogRegistration);

    }
}
