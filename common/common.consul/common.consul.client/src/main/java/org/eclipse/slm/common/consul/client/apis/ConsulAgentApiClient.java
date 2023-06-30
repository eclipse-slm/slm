package org.eclipse.slm.common.consul.client.apis;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.option.ImmutableQueryOptions;
import com.orbitz.consul.option.ImmutableQueryParameterOptions;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class ConsulAgentApiClient extends AbstractConsulApiClient {
    private final ConsulNodesApiClient consulNodesApiClient;
    private final ConsulServicesApiClient consulServicesApiClient;
    public static final String CONSUL_CLIENT_SERVICE_NAME = "consul-client";
    private static final int CONSUL_CLIENT_DEFAULT_HTTP_PORT = 8500;

    public ConsulAgentApiClient(
            @Value("${consul.scheme}")      String consulScheme,
            @Value("${consul.host}")        String consulHost,
            @Value("${consul.port}")        int consulPort,
            @Value("${consul.acl-token}")   String consulToken,
            @Value("${consul.datacenter}")  String consulDatacenter,
            ConsulNodesApiClient consulNodesApiClient,
            ConsulServicesApiClient consulServicesApiClient
    ) {
        super(consulScheme, consulHost, consulPort, consulToken, consulDatacenter);
        this.consulNodesApiClient = consulNodesApiClient;
        this.consulServicesApiClient = consulServicesApiClient;
    }

    private int getConsulClientPort(ConsulCredential consulCredential, UUID nodeId) throws ConsulLoginFailedException {
        List<NodeService> nodeServices = consulServicesApiClient.getNodeServicesByNodeId(consulCredential, nodeId);

        Optional<NodeService> optionalConsulClientService = nodeServices
                .stream()
                .filter(service -> service.getService().equals(CONSUL_CLIENT_SERVICE_NAME))
                .findFirst();

        if(optionalConsulClientService.isEmpty())
            return CONSUL_CLIENT_DEFAULT_HTTP_PORT;
        else
           return optionalConsulClientService.get().getPort();
    }

    public void registerService(
            ConsulCredential consulCredential,
            int consulClientPort,
            UUID nodeId,
            String serviceName,
            UUID serviceId,
            Optional<Integer> servicePort,
            List<String> serviceTags,
            Map<String, String> serviceMetaData
    ) throws ConsulLoginFailedException {
        ImmutableRegistration newCatalogRegistration = ImmutableRegistration.builder()
                .setName(serviceName)
                .setId(serviceId.toString())
                .setMeta(serviceMetaData)
                .setTags(serviceTags)
                .setPort(servicePort.orElse(0))
                .build();

        try {
            getConsulAgentClient(consulCredential, consulClientPort, nodeId)
                    .agentClient()
                    .register(newCatalogRegistration);
        } catch (NullPointerException e) {
            LOG.error("Failed to register service because Node not found: " + e.getMessage());
        }
    }

    public void registerService(
            ConsulCredential consulCredential,
            UUID nodeId,
            String serviceName,
            UUID serviceId,
            Optional<Integer> servicePort,
            List<String> serviceTags,
            Map<String, String> serviceMetaData
    ) throws ConsulLoginFailedException {
        registerService(
                consulCredential,
                getConsulClientPort(consulCredential, nodeId),
                nodeId,
                serviceName,
                serviceId,
                servicePort,
                serviceTags,
                serviceMetaData
        );
    }

    private void removeServiceByName(
            ConsulCredential consulCredential,
            int consulClientPort,
            UUID nodeId,
            String service
    ) throws ConsulLoginFailedException {
        AgentClient agentClient = getConsulAgentClient(consulCredential, consulClientPort, nodeId).agentClient();
        Map<String, Service> services = agentClient.getServices(
                ImmutableQueryOptions.builder().filter("Service==\"" + service + "\"").build()
        );

        if(services.size() != 1)
            return;

        agentClient.deregister(services.keySet().stream().findFirst().get());
    }

    public void removeServiceByName(ConsulCredential consulCredential, UUID nodeId, String service) throws ConsulLoginFailedException {
        removeServiceByName(
                consulCredential,
                getConsulClientPort(consulCredential, nodeId),
                nodeId,
                service
        );
    }

    public void leaveGracefully(ConsulCredential consulCredential, UUID nodeId) throws ConsulLoginFailedException {
        Optional<Node> node = consulNodesApiClient.getNodeById(consulCredential, nodeId);
        int port = getConsulClientPort(consulCredential, nodeId);
        URI uri = null;

        if(node.isEmpty())
            return;

        try {
            uri = new URI("http", null, node.get().getAddress(), port, "/v1/agent/leave", null, null);
        } catch(URISyntaxException e) {
            LOG.error("Failed to set agent leave gracefully, because: " + e.getMessage());
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(getConsulToken(consulCredential));

        restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity<>(null, httpHeaders), String.class);
    }

    public void leaveForce(ConsulCredential consulCredential, String nodeName) throws ConsulLoginFailedException {
        getConsulClient(consulCredential)
                .agentClient()
                .forceLeave(nodeName, ImmutableQueryParameterOptions.builder().prune(true).build());
    }

    private Consul getConsulAgentClient(
            ConsulCredential consulCredential,
            int consulClientPort,
            UUID nodeId
    ) throws ConsulLoginFailedException {
        Optional<Node> optionalNode = consulNodesApiClient.getNodeById(
                consulCredential,
                nodeId
        );

        if(optionalNode.isEmpty())
            return null;

        return getConsulClient(
                consulCredential,
                optionalNode.get().getAddress(),
                consulClientPort
        );
    }
}
