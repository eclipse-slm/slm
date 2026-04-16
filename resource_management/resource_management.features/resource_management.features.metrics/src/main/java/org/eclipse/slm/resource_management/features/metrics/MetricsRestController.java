package org.eclipse.slm.resource_management.features.metrics;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.aas.clients.auth.JwtAuthenticationTokenAuthRequestInterceptor;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClient;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClientFactory;
import org.eclipse.slm.aas.clients.submodelservice.SubmodelServiceClient;
import org.eclipse.slm.common.restserver.annotations.AuthorizedAsSlmUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(MetricsRestApiConfig.BASE_PATH)
@Tag(name = MetricsRestApiConfig.TAG)
public class MetricsRestController implements MetricsRestApi {

    private final Logger LOG = LoggerFactory.getLogger(MetricsRestController.class);

    private final SubmodelRegistryClient submodelRegistryClient;

    public MetricsRestController(SubmodelRegistryClientFactory submodelRegistryClientFactory) {
        this.submodelRegistryClient = submodelRegistryClientFactory.getClient();
    }

    @Override
    public ResponseEntity<Map<String, Object>> getMetric(
            UUID resourceId
    ) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> monitoringValues = new HashMap<>();
        try {
            var submodelId = "PlatformResources-" + resourceId;
            var platformResourcesSubmodelDescriptorOptional = this.submodelRegistryClient.getSubmodelDescriptor(submodelId);

            if (platformResourcesSubmodelDescriptorOptional.isPresent()) {
                var endpoints = platformResourcesSubmodelDescriptorOptional.get().getEndpoints();
                if (endpoints.size() > 0) {
                    var submodelEndpoint = endpoints.get(0);
                    var submodelServiceEndpointUrl = submodelEndpoint.getProtocolInformation().getHref();
                    var submodelServiceClient = new SubmodelServiceClient(submodelServiceEndpointUrl, new JwtAuthenticationTokenAuthRequestInterceptor(jwtAuthenticationToken));
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
