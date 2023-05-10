package org.eclipse.slm.resource_management.service.rest.metrics;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType;
import org.eclipse.basyx.submodel.metamodel.map.identifier.Identifier;
import org.eclipse.basyx.vab.exception.provider.ProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private final IAASRegistry registry;
    private final ConnectedAssetAdministrationShellManager manager;

    MetricsRestController(
            @Value("${basyx.aas-registry.url}") String aasRegistryUrl
    ) {
        this.registry = new AASRegistryProxy(aasRegistryUrl);
        this.manager = new ConnectedAssetAdministrationShellManager(registry);
    }

    @RequestMapping(value = "/{resourceId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getMetric(
            @PathVariable(name = "resourceId") UUID resourceId
    ) {
        Map<String, Object> monitoringValues = new HashMap<>();
        try {
            var resourceAASDescriptor = registry.lookupAAS(new Identifier(IdentifierType.CUSTOM, resourceId.toString()));
            var monitoringSubmodelDescriptor = resourceAASDescriptor.getSubmodelDescriptorFromIdShort("PlatformResources");
            var monitoringSubmodel = manager.retrieveSubmodel(resourceAASDescriptor.getIdentifier(), monitoringSubmodelDescriptor.getIdentifier());
            monitoringValues = monitoringSubmodel.getValues();
        } catch (ProviderException e) {
            LOG.info("Monitoring for resource with id '" + resourceId + "' not available (no AAS found)");
        } catch (NullPointerException e) {
            LOG.info("Monitoring for resource with id '" + resourceId + "' not available (submodel not found)");
        }

        return ResponseEntity.ok(monitoringValues);
    }
}
