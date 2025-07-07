package org.eclipse.slm.common.aas.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

@Component
public class SubmodelRegistryClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SubmodelRegistryClientFactory.class);

    private final static String SUBMODEL_REGISTRY_DISCOVERY_INSTANCE_ID = "submodel-registry";

    private final String submodelRegistryUrlApplicationProperties;

    private final DiscoveryClient discoveryClient;

    public SubmodelRegistryClientFactory(@Value("${aas.submodel-registry.url}") String submodelRegistryUrlApplicationProperties,
                                         DiscoveryClient discoveryClient) {
        this.submodelRegistryUrlApplicationProperties = submodelRegistryUrlApplicationProperties;
        this.discoveryClient = discoveryClient;
    }

    public SubmodelRegistryClient getClient() {
        var submodelRegistryUrl = this.submodelRegistryUrlApplicationProperties;
        if (this.discoveryClient != null) {
            var submodelRegistryServiceInstances = this.discoveryClient.getInstances(SubmodelRegistryClientFactory.SUBMODEL_REGISTRY_DISCOVERY_INSTANCE_ID);
            if (!submodelRegistryServiceInstances.isEmpty()) {
                var submodelRegistryServiceInstance = submodelRegistryServiceInstances.get(0);
                var path = "";
                if (submodelRegistryServiceInstance.getMetadata().get("path") != null) {
                    path = submodelRegistryServiceInstance.getMetadata().get("path");
                }
                if (submodelRegistryServiceInstance != null) {
                    submodelRegistryUrl = "http://" + submodelRegistryServiceInstance.getHost()
                            + ":" + submodelRegistryServiceInstance.getPort() + path;
                    LOG.debug("Using submodel registry URL from discovery client: " + submodelRegistryUrl);
                }
                else {
                    LOG.warn("No service instance '" + SubmodelRegistryClientFactory.SUBMODEL_REGISTRY_DISCOVERY_INSTANCE_ID + "' found via discovery client. Using default URL '"
                            + submodelRegistryUrl + "' from application.yml.");
                }
            }
        }
        else {
            LOG.debug("Discovery client is not available. Using submodel registry URL from application properties: " + submodelRegistryUrl);
        }

        var submodelRegistryClient = new SubmodelRegistryClient(submodelRegistryUrl);
        return submodelRegistryClient;
    }
}
