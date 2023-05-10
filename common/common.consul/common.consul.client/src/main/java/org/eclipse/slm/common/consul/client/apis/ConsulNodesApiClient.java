package org.eclipse.slm.common.consul.client.apis;

import com.orbitz.consul.model.catalog.CatalogRegistration;
import com.orbitz.consul.model.catalog.ImmutableCatalogDeregistration;
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

import java.util.*;

@Component
public class ConsulNodesApiClient extends AbstractConsulApiClient {

    public final static Logger LOG = LoggerFactory.getLogger(ConsulNodesApiClient.class);
    private final ConsulAclApiClient consulAclApiClient;

    @Value("${consul.skipAcl:false}")
    private Boolean skipAcl;

    @Autowired
    public ConsulNodesApiClient(
                                @Value("${consul.scheme}")       String consulScheme,
                                @Value("${consul.host}")         String consulHost,
                                @Value("${consul.port}")         int consulPort,
                                @Value("${consul.acl-token}")    String consulToken,
                                @Value("${consul.datacenter}")   String consulDatacenter,
                                ConsulAclApiClient               consulAclApiClient) {
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

    public void registerNode(ConsulCredential consulCredential, CatalogNode node)
            throws ConsulLoginFailedException {
        var catalogRegistration = ConsulObjectMapper.map(node, CatalogRegistration.class);
        this.getConsulClient(consulCredential).catalogClient().register(catalogRegistration);
        this.consulAclApiClient.addReadAccessViaKeycloakRole(node.getId(), node.getNode(), "node");
    }

    public void deleteNode(ConsulCredential consulCredential, CatalogNode catalogNode)
            throws ConsulLoginFailedException {
        this.deleteNode(consulCredential, catalogNode.getNode());
    }

    public void deleteNode(ConsulCredential consulCredential, String consulNodeName)
            throws ConsulLoginFailedException {
        var deregistration = ImmutableCatalogDeregistration.builder()
                .setNode(consulNodeName)
                .build();
        this.getConsulClient(consulCredential).catalogClient().deregister(deregistration);
    }

}
