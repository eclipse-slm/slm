package org.eclipse.slm.resource_management.service.rest.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException;
import org.eclipse.slm.common.aas.AasRegistryClient;
import org.eclipse.slm.common.aas.AasRepositoryClient;
import org.eclipse.slm.common.aas.SubmodelRegistryClient;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.service.rest.metrics.MetricsRestController;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;

@Component
public class AasHandler {

    private final Logger LOG = LoggerFactory.getLogger(AasHandler.class);

    private final AasRegistryClient aasRegistryClient;

    private final AasRepositoryClient aasRepositoryClient;

    private final SubmodelRegistryClient submodelRegistryClient;

    private final ResourcesConsulClient resourcesConsulClient;

    private final String monitoringServiceUrl;

    public AasHandler(AasRegistryClient aasRegistryClient, AasRepositoryClient aasRepositoryClient,
                      SubmodelRegistryClient submodelRegistryClient,
                      ResourcesConsulClient resourcesConsulClient,
                      @Value("${monitoring.service.url}") String monitoringServiceUrl) {
        this.aasRegistryClient = aasRegistryClient;
        this.aasRepositoryClient = aasRepositoryClient;
        this.submodelRegistryClient = submodelRegistryClient;
        this.resourcesConsulClient = resourcesConsulClient;
        this.monitoringServiceUrl = monitoringServiceUrl;
    }

    @PostConstruct
    public void init() {
        // Create AAS for all resources
        try {
            var resources = resourcesConsulClient.getResources(new ConsulCredential());

            for (var resource: resources) {
                var resourceAAS = new ResourceAAS(resource);
                this.aasRepositoryClient.createOrUpdateAas(resourceAAS);
                var platformResourceSubmodelId = "PlatformResources-" + resource.getId();
                var submodelUrl = this.monitoringServiceUrl + "/" + resource.getId() + "/submodel";
                this.aasRepositoryClient.addSubmodelReferenceToAas(resourceAAS.getId(), platformResourceSubmodelId);
                this.submodelRegistryClient.registerSubmodel(submodelUrl, platformResourceSubmodelId, platformResourceSubmodelId);
            }
        } catch (ConsulLoginFailedException e) {
            throw new RuntimeException(e);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<AssetAdministrationShellDescriptor> getResourceAasDescriptor(UUID resourceId) {
        try {
            var aasDescriptorOptional = this.aasRegistryClient.getAasDescriptor(ResourceAAS.createAasIdFromResourceId(resourceId));
            return aasDescriptorOptional;
        } catch (org.eclipse.digitaltwin.basyx.aasregistry.client.ApiException e) {
            LOG.error(e.getMessage());
            return Optional.empty();
        }
    }
}
