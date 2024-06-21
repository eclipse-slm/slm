package org.eclipse.slm.resource_management.service.rest.metrics;

import org.eclipse.digitaltwin.basyx.aasregistry.client.ApiException;
import org.eclipse.slm.common.aas.AasRegistryClient;
import org.eclipse.slm.common.aas.AasRepositoryClient;
import org.eclipse.slm.common.aas.SubmodelRegistryClient;
import org.eclipse.slm.common.aas.SubmodelServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
        Map<String, Object> monitoringValues = new HashMap<>();
        try {
            var submodelId = "PlatformResources-" + resourceId;
            var platformResourcesSubmodelDescriptorOptional = submodelRegistryClient.findSubmodelDescriptor(submodelId);

            if (platformResourcesSubmodelDescriptorOptional.isPresent()) {
                var endpoints = platformResourcesSubmodelDescriptorOptional.get().getEndpoints();
                if (endpoints.size() > 0) {
                    var submodelEndpoint = endpoints.get(0);
                    var submodelServiceEndpointUrl = submodelEndpoint.getProtocolInformation().getHref();
                    var submodelServiceClient = new SubmodelServiceClient(submodelServiceEndpointUrl);
                    var submodelValues = submodelServiceClient.getSubmodelValues();

                    return ResponseEntity.ok(submodelValues);
                }
            }
        } catch (NullPointerException e) {
            LOG.info("Monitoring for resource with id '" + resourceId + "' not available (submodel not found)");
        }

        return ResponseEntity.ok(monitoringValues);
    }
}
