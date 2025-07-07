package org.eclipse.slm.common.aas.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

@Component
public class SubmodelRepositoryClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SubmodelRepositoryClientFactory.class);

    private final static String SUBMODEL_REPOSITORY_DISCOVERY_INSTANCE_ID = "submodel-repository";

    private final String submodelRepositoryUrlApplicationProperties;

    private final DiscoveryClient discoveryClient;

    public SubmodelRepositoryClientFactory(@Value("${aas.submodel-repository.url}") String submodelRepositoryUrlApplicationProperties,
                                           DiscoveryClient discoveryClient) {
        this.submodelRepositoryUrlApplicationProperties = submodelRepositoryUrlApplicationProperties;
        this.discoveryClient = discoveryClient;
    }

    public SubmodelRepositoryClient getClient() {
        var submodelRepositoryUrl = this.submodelRepositoryUrlApplicationProperties;
        if (this.discoveryClient != null) {
            var submodelRepositoryServiceInstances = this.discoveryClient.getInstances(SubmodelRepositoryClientFactory.SUBMODEL_REPOSITORY_DISCOVERY_INSTANCE_ID);
            if (!submodelRepositoryServiceInstances.isEmpty()) {
                var submodelRepositoryServiceInstance = submodelRepositoryServiceInstances.get(0);
                var path = "";
                if (submodelRepositoryServiceInstance.getMetadata().get("path") != null) {
                    path = submodelRepositoryServiceInstance.getMetadata().get("path");
                }
                if (submodelRepositoryServiceInstance != null) {
                    submodelRepositoryUrl = "http://" + submodelRepositoryServiceInstance.getHost()
                            + ":" + submodelRepositoryServiceInstance.getPort() + path;
                    LOG.debug("Using submodel repository URL from discovery client: " + submodelRepositoryUrl);
                }
                else {
                    LOG.warn("No service instance '" + SubmodelRepositoryClientFactory.SUBMODEL_REPOSITORY_DISCOVERY_INSTANCE_ID + "' found via discovery client. Using default URL '"
                            + submodelRepositoryUrl + "' from application.yml.");
                }
            }
        }
        else {
            LOG.debug("Discovery client is not available. Using submodel repository URL from application properties: " + submodelRepositoryUrl);
        }

        var submodelRepositoryClient = new SubmodelRepositoryClient(submodelRepositoryUrl);
        return submodelRepositoryClient;
    }

}
