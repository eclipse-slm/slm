package org.eclipse.slm.common.consul.client.apis;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
@Component
public class ConsulGenericServicesClient {
    private final static Logger LOG = LoggerFactory.getLogger(ConsulGenericServicesClient.class);
    private final ConsulHealthApiClient consulHealthApiClient;
    private final ConsulServicesApiClient consulServicesApiClient;
    private final ConsulAgentApiClient consulAgentApiClient;

    public ConsulGenericServicesClient(
            ConsulHealthApiClient consulHealthApiClient,
            ConsulServicesApiClient consulServicesApiClient,
            ConsulAgentApiClient consulAgentApiClient
    ) {
        this.consulHealthApiClient = consulHealthApiClient;
        this.consulServicesApiClient = consulServicesApiClient;
        this.consulAgentApiClient = consulAgentApiClient;
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
        if(consulHealthApiClient.hasNodeAgent(consulCredential, nodeId)) {
            consulAgentApiClient.registerService(
                    consulCredential,
                    nodeId,
                    serviceName,
                    serviceId,
                    servicePort,
                    serviceTags,
                    serviceMetaData
            );
        } else {
            consulServicesApiClient.registerService(
                    consulCredential,
                    nodeId,
                    serviceName,
                    serviceId,
                    servicePort,
                    serviceTags,
                    serviceMetaData
            );
        }
    }

    public void deregisterService(
            ConsulCredential consulCredential,
            UUID nodeId,
            String serviceName
    ) throws ConsulLoginFailedException {
        if(consulHealthApiClient.hasNodeAgent(consulCredential, nodeId)) {
                consulAgentApiClient.removeServiceByName(
                        consulCredential,
                        nodeId,
                        serviceName
                );
        } else {
            consulServicesApiClient.removeServiceByName(
                    consulCredential,
                    nodeId,
                    serviceName
            );
        }
    }

}
