package org.eclipse.slm.resource_management.service.rest.metrics;

import org.eclipse.slm.common.aas.clients.AasRegistryClient;
import org.eclipse.slm.common.aas.clients.AasRepositoryClient;
import org.eclipse.slm.common.aas.clients.SubmodelRegistryClient;
import org.eclipse.slm.common.aas.clients.SubmodelServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/metrics")
public class MetricsRestController {

    private final Logger LOG = LoggerFactory.getLogger(MetricsRestController.class);
    private final AasRegistryClient registry;
    private final AasRepositoryClient aasRepositoryClient;

    private final SubmodelRegistryClient submodelRegistryClient;

    public MetricsRestController(AasRegistryClient registry, AasRepositoryClient aasRepositoryClient, SubmodelRegistryClient submodelRegistryClient) {
        this.registry = registry;
        this.aasRepositoryClient = aasRepositoryClient;
        this.submodelRegistryClient = submodelRegistryClient;
    }

    @RequestMapping(value = "/{resourceId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getMetric(
            @PathVariable(name = "resourceId") UUID resourceId
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> monitoringValues = new HashMap<>();
        try {
            var submodelId = "PlatformResources-" + resourceId;
            var platformResourcesSubmodelDescriptorOptional = submodelRegistryClient.findSubmodelDescriptor(submodelId);

            if (platformResourcesSubmodelDescriptorOptional.isPresent()) {
                var endpoints = platformResourcesSubmodelDescriptorOptional.get().getEndpoints();
                if (endpoints.size() > 0) {
                    var submodelEndpoint = endpoints.get(0);
                    var submodelServiceEndpointUrl = submodelEndpoint.getProtocolInformation().getHref();
                    var submodelServiceClient = new SubmodelServiceClient(submodelServiceEndpointUrl, jwtAuthenticationToken);
                    var submodelValues = submodelServiceClient.getSubmodelValues();

                    return ResponseEntity.ok(submodelValues);
                }
            }
        } catch (NullPointerException e) {
            LOG.info("Monitoring for resource with id '" + resourceId + "' not available (submodel not found)");
        } catch (org.eclipse.digitaltwin.basyx.client.internal.ApiException e) {
            if (e.getMessage().equals("java.net.ConnectException")) {
                LOG.warn("Monitoring for resource with id '" + resourceId + "' not available (submodel not accessible)");
                return ResponseEntity.ok(monitoringValues);
            }
            else {
                LOG.error(e.getMessage());
            }
        }

        return ResponseEntity.ok(monitoringValues);
    }
}
