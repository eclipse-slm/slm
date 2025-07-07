package org.eclipse.slm.common.aas.clients;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class AasRepositoryClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AasRepositoryClientFactory.class);

    private static final String AAS_REPOSITORY_DISCOVERY_INSTANCE_ID = "aas-repository";

    private final String aasRepositoryUrlApplicationProperties;

    private final DiscoveryClient discoveryClient;

    public AasRepositoryClientFactory(@Value("${aas.aas-repository.url}") String aasRepositoryUrlApplicationProperties,
                                      DiscoveryClient discoveryClient) {
        this.aasRepositoryUrlApplicationProperties = aasRepositoryUrlApplicationProperties;
        this.discoveryClient = discoveryClient;
    }

    public AasRepositoryClient getClient() {
        return getClient(null);
    }

    public AasRepositoryClient getClient(JwtAuthenticationToken jwtAuthenticationToken) {
        var aasRepositoryUrl = this.aasRepositoryUrlApplicationProperties;
        if (this.discoveryClient != null) {
            var aasRepositoryServiceInstances = this.discoveryClient.getInstances(AasRepositoryClientFactory.AAS_REPOSITORY_DISCOVERY_INSTANCE_ID);
            if (!aasRepositoryServiceInstances.isEmpty()) {
                var aasRepositoryServiceInstance = aasRepositoryServiceInstances.get(0);
                var path = "";
                if (aasRepositoryServiceInstance.getMetadata().get("path") != null) {
                    path = aasRepositoryServiceInstance.getMetadata().get("path");
                }
                if (aasRepositoryServiceInstance != null) {
                    aasRepositoryUrl = "http://" + aasRepositoryServiceInstance.getHost()
                            + ":" + aasRepositoryServiceInstance.getPort() + path;
                    LOG.debug("Using aas repository URL from discovery client: " + aasRepositoryUrl);
                }
                else {
                    LOG.warn("No service instance '" + AasRepositoryClientFactory.AAS_REPOSITORY_DISCOVERY_INSTANCE_ID + "' found via discovery client. Using default URL '"
                            + aasRepositoryUrl + "' from application.yml.");
                }
            }
        }
        else {
            LOG.debug("Discovery client is not available. Using aas repository URL from application properties: " + aasRepositoryUrl);
        }

        var aasRepositoryClient = new AasRepositoryClient(aasRepositoryUrl, jwtAuthenticationToken);
        return aasRepositoryClient;
    }

    public static AasRepositoryClient FromShellDescriptor(AssetAdministrationShellDescriptor shellDescriptor) {
        var shellEndpoint = shellDescriptor.getEndpoints().get(0).getProtocolInformation().getHref();

        if (shellEndpoint.contains("/shells/")) {
            var regExPattern = Pattern.compile("(.*)/shells");
            var matcher = regExPattern.matcher(shellEndpoint);
            var matchesFound = matcher.find();
            if (matchesFound) {
                var aasRepositoryBaseUrl = matcher.group(1);
                var aasRepositoryClient = new AasRepositoryClient(aasRepositoryBaseUrl);

                return aasRepositoryClient;
            }
        }

        LOG.error("Could not create AasRepositoryClient from shell descriptor with endpoint '" + shellEndpoint + "'. No valid endpoint found.");
        throw new IllegalArgumentException("Could not create AasRepositoryClient from shell descriptor with endpoint '" + shellEndpoint + "'. No valid endpoint found.");
    }
}
