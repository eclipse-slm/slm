package org.eclipse.slm.common.aas.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

@Component
public class AasRegistryClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AasRegistryClientFactory.class);

    private static final String AAS_REGISTRY_DISCOVERY_INSTANCE_ID = "aas-registry";

    private final String aasRegistryUrlApplicationProperties;

    private final DiscoveryClient discoveryClient;


    public AasRegistryClientFactory(@Value("${aas.aas-registry.url}") String aasRegistryUrlApplicationProperties,
                                    DiscoveryClient discoveryClient) {
        this.aasRegistryUrlApplicationProperties = aasRegistryUrlApplicationProperties;
        this.discoveryClient = discoveryClient;
    }

    public AasRegistryClient getClient() {
        var aasRegistryUrl = this.aasRegistryUrlApplicationProperties;
        if (this.discoveryClient != null) {
            var aasRegistryServiceInstances = this.discoveryClient.getInstances(AasRegistryClientFactory.AAS_REGISTRY_DISCOVERY_INSTANCE_ID);
            if (!aasRegistryServiceInstances.isEmpty()) {
                var aasRegistryServiceInstance = aasRegistryServiceInstances.get(0);
                var path = "";
                if (aasRegistryServiceInstance.getMetadata().get("path") != null) {
                    path = aasRegistryServiceInstance.getMetadata().get("path");
                }
                if (aasRegistryServiceInstance != null) {
                    aasRegistryUrl = "http://" + aasRegistryServiceInstance.getHost()
                            + ":" + aasRegistryServiceInstance.getPort() + path;
                    LOG.debug("Using aas registry URL from discovery client: " + aasRegistryUrl);
                }
                else {
                    LOG.warn("No service instance '" + AasRegistryClientFactory.AAS_REGISTRY_DISCOVERY_INSTANCE_ID + "' found via discovery client. Using default URL '"
                            + aasRegistryUrl + "' from application.yml.");
                }
            }
        }
        else {
            LOG.debug("Discovery client is not available. Using aas registry URL from application properties: " + aasRegistryUrl);
        }

        var aasRegistryClient = new AasRegistryClient(aasRegistryUrl);
        return aasRegistryClient;
    }
}
