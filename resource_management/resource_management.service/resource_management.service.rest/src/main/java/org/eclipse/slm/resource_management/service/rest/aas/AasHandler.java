package org.eclipse.slm.resource_management.service.rest.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException;
import org.eclipse.slm.common.aas.clients.AasRegistryClient;
import org.eclipse.slm.common.aas.clients.AasRepositoryClient;
import org.eclipse.slm.common.aas.clients.SubmodelRegistryClient;
import org.eclipse.slm.common.aas.clients.SubmodelRepositoryClient;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.model.resource.BasicResource;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.service.rest.aas.resources.ResourceAAS;
import org.eclipse.slm.resource_management.service.rest.aas.resources.ResourcesSubmodelRepositoryApiHTTPController;
import org.eclipse.slm.resource_management.service.rest.aas.resources.consul.ConsulSubmodel;
import org.eclipse.slm.resource_management.service.rest.resources.ResourceEvent;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;

@Component
public class AasHandler implements ApplicationListener<ResourceEvent> {

    private final Logger LOG = LoggerFactory.getLogger(AasHandler.class);

    private final AasRegistryClient aasRegistryClient;

    private final AasRepositoryClient aasRepositoryClient;

    private final SubmodelRegistryClient submodelRegistryClient;

    private final SubmodelRepositoryClient submodelRepositoryClient;

    private final ResourcesConsulClient resourcesConsulClient;

    private final String monitoringServiceUrl;

    private final String externalScheme;
    private final String externalHostname;
    private final String externalPort;


    public AasHandler(AasRegistryClient aasRegistryClient, AasRepositoryClient aasRepositoryClient,
                      SubmodelRegistryClient submodelRegistryClient, SubmodelRepositoryClient submodelRepositoryClient,
                      ResourcesConsulClient resourcesConsulClient,
                      @Value("${monitoring.service.url}") String monitoringServiceUrl,
                      @Value("${deployment.scheme}") String externalScheme,
                      @Value("${deployment.hostname}") String externalHostname,
                      @Value("${deployment.port}") String externalPort) {
        this.aasRegistryClient = aasRegistryClient;
        this.aasRepositoryClient = aasRepositoryClient;
        this.submodelRegistryClient = submodelRegistryClient;
        this.submodelRepositoryClient = submodelRepositoryClient;
        this.resourcesConsulClient = resourcesConsulClient;
        this.monitoringServiceUrl = monitoringServiceUrl;
        this.externalScheme = externalScheme;
        this.externalHostname = externalHostname;
        this.externalPort = externalPort;
    }

    @PostConstruct
    public void init() {
        // Create AAS for all resources
        try {
            var resources = resourcesConsulClient.getResources(new ConsulCredential());

            for (var resource: resources) {
                this.createResourceAasAndSubmodels(resource);
            }
        } catch (ConsulLoginFailedException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            LOG.error(e.getMessage());
        }
    }

    public Optional<AssetAdministrationShellDescriptor> getResourceAasDescriptor(UUID resourceId) {
        try {
            var aasDescriptorOptional = this.aasRegistryClient.getAasDescriptor(
                    ResourceAAS.createAasIdFromResourceId(resourceId));
            return aasDescriptorOptional;
        } catch (org.eclipse.digitaltwin.basyx.aasregistry.client.ApiException e) {
            LOG.error(e.getMessage());
            return Optional.empty();
        }
    }

    private String getResourcesSubmodelRepositoryUrl(Base64UrlEncodedIdentifier aasId) {
        var basePath = ResourcesSubmodelRepositoryApiHTTPController.class.getAnnotation(RequestMapping.class).value()[0];
        basePath = basePath.replace("{aasId}", aasId.getEncodedIdentifier());
        var url = this.externalScheme + "://" + this.externalHostname + ":" + this.externalPort + basePath;

        return url;
    }

    private void createResourceAasAndSubmodels(BasicResource resource) {
        try {
            var resourceAAS = new ResourceAAS(resource);
            var resourceAASIdEncoded = new Base64UrlEncodedIdentifier(resourceAAS.getId());
            this.aasRepositoryClient.createOrUpdateAas(resourceAAS);

            var platformResourcesSubmodelId = "PlatformResources-" + resource.getId();
            var platformResourcesSubmodelUrl = this.monitoringServiceUrl + "/" + resource.getId() + "/submodel";
            this.aasRepositoryClient.addSubmodelReferenceToAas(resourceAAS.getId(), platformResourcesSubmodelId);
            this.submodelRegistryClient.registerSubmodel(
                    platformResourcesSubmodelUrl,
                    platformResourcesSubmodelId,
                    platformResourcesSubmodelId,
                    null);

            var consulSubmodelId = "Consul-" + resource.getId();
            var consulSubmodelIdEncoded = new Base64UrlEncodedIdentifier(consulSubmodelId);
            var consulSubmodelUrl = this.getResourcesSubmodelRepositoryUrl(resourceAASIdEncoded)
                    + "/submodels/" + consulSubmodelIdEncoded.getEncodedIdentifier();
            this.aasRepositoryClient.addSubmodelReferenceToAas(resourceAAS.getId(), consulSubmodelId);
            this.submodelRegistryClient.registerSubmodel(
                    consulSubmodelUrl,
                    consulSubmodelId,
                    consulSubmodelId,
                    ConsulSubmodel.SEMANTIC_ID.getKeys().get(0).getValue());
        }
        catch (ApiException e) {
            LOG.error("Unable to create AAS and submodels for resource [id='" + resource.getId() + "']: " + e.getMessage());
        }
    }

    private void deleteResourceAasAndSubmodels (UUID resourceId) {
        try {
            var resourceAasId = ResourceAAS.createAasIdFromResourceId(resourceId);
            var resourceAAS = this.aasRepositoryClient.getAas(resourceAasId);

            for (var submodelRef : resourceAAS.getSubmodels()) {
                if (submodelRef.getKeys().get(0).getType().equals(KeyTypes.SUBMODEL)) {
                    var submodelId = submodelRef.getKeys().get(0).getValue();

                    var submodelDescriptorOptional = this.submodelRegistryClient.findSubmodelDescriptor(submodelId);
                    if (submodelDescriptorOptional.isPresent()) {
                        var endpoint = submodelDescriptorOptional.get().getEndpoints().get(0).getProtocolInformation().getHref();

                        if (endpoint.startsWith(this.submodelRepositoryClient.getSubmodelRepositoryUrl())) {
                            this.submodelRepositoryClient.deleteSubmodel(submodelId);
                        }
                    }
                    this.submodelRegistryClient.unregisterSubmodel(submodelId);
                }
            }
            this.aasRepositoryClient.deleteAAS(resourceAasId);

        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onApplicationEvent(ResourceEvent resourceEvent) {
        switch (resourceEvent.getOperation()) {
            case CREATE -> {
                try {
                    var optionalResource = resourcesConsulClient.getResourceById(new ConsulCredential(), resourceEvent.getResourceId());
                    optionalResource.ifPresent(this::createResourceAasAndSubmodels);
                } catch (ConsulLoginFailedException e) {
                    throw new RuntimeException(e);
                } catch (ResourceNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            case DELETE -> {
                this.deleteResourceAasAndSubmodels(resourceEvent.getResourceId());
            }
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
