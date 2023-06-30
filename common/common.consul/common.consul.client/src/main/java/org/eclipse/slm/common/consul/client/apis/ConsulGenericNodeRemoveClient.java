package org.eclipse.slm.common.consul.client.apis;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ConsulGenericNodeRemoveClient extends AbstractConsulApiClient {
    private final static Logger LOG = LoggerFactory.getLogger(ConsulGenericNodeRemoveClient.class);
    private final ConsulHealthApiClient consulHealthApiClient;
    private final ConsulNodesApiClient consulNodesApiClient;
    private final ConsulAgentApiClient consulAgentApiClient;

    public ConsulGenericNodeRemoveClient(
            @Value("${consul.scheme}")       String consulScheme,
            @Value("${consul.host}")         String consulHost,
            @Value("${consul.port}")         int consulPort,
            @Value("${consul.acl-token}")    String consulToken,
            @Value("${consul.datacenter}")   String consulDatacenter,
            ConsulHealthApiClient consulHealthApiClient,
            ConsulNodesApiClient consulNodesApiClient,
            ConsulAgentApiClient consulAgentApiClient
    ) {
        super(consulScheme, consulHost, consulPort, consulToken, consulDatacenter);
        this.consulHealthApiClient = consulHealthApiClient;
        this.consulNodesApiClient = consulNodesApiClient;
        this.consulAgentApiClient = consulAgentApiClient;
    }

    public void removeNode(ConsulCredential consulCredential, String nodeName) throws ConsulLoginFailedException {
        Node node = consulNodesApiClient.getNodeByName(consulCredential, nodeName);

        if(consulHealthApiClient.hasNodeAgent(consulCredential, node.getId())) {
            consulAgentApiClient.leaveGracefully(consulCredential, node.getId());
        } else {
            consulNodesApiClient.deleteNode(consulCredential, nodeName);
        }
    }
}
