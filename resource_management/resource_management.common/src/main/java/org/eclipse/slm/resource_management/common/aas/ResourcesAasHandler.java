package org.eclipse.slm.resource_management.common.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException;
import org.eclipse.slm.common.aas.clients.*;
import org.eclipse.slm.common.aas.clients.exceptions.ShellNotFoundException;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.aas.submodels.ResourcesSubmodelRepositoryHTTPApiController;
import org.eclipse.slm.resource_management.common.aas.submodels.deviceinfo.DeviceInfoSubmodel;
import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3;
import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3Submodel;
import org.eclipse.slm.resource_management.common.resources.ResourceEvent;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.resources.BasicResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ResourcesAasHandler implements ApplicationListener<ResourceEvent> {

    private final Logger LOG = LoggerFactory.getLogger(ResourcesAasHandler.class);

    private final AasRegistryClient aasRegistryClient;

    private final AasRepositoryClient aasRepositoryClient;

    private final SubmodelRegistryClient submodelRegistryClient;

    private final SubmodelRepositoryClient submodelRepositoryClient;

    private final ResourcesConsulClient resourcesConsulClient;

    private final String monitoringServiceUrl;

    private final String externalUrl;


    public ResourcesAasHandler(AasRegistryClientFactory aasRegistryClientFactory,
                               AasRepositoryClientFactory aasRepositoryClientFactory,
                               SubmodelRegistryClientFactory submodelRegistryClientFactory,
                               SubmodelRepositoryClientFactory submodelRepositoryClientFactory,
                               ResourcesConsulClient resourcesConsulClient,
                               @Value("${monitoring.service.url}") String monitoringServiceUrl,
                               @Value("${deployment.url}") String externalUrl) {
        this.aasRegistryClient = aasRegistryClientFactory.getClient();
        this.aasRepositoryClient = aasRepositoryClientFactory.getClient();
        this.submodelRegistryClient = submodelRegistryClientFactory.getClient();
        this.submodelRepositoryClient = submodelRepositoryClientFactory.getClient();
        this.resourcesConsulClient = resourcesConsulClient;
        this.monitoringServiceUrl = monitoringServiceUrl;
        this.externalUrl = externalUrl;
    }

    @PostConstruct
    public void init() {
        // Create AAS for all resources
        try {
            var resources = resourcesConsulClient.getResources(new ConsulCredential());

            for (var resource: resources) {
                var digitalNameplateV3 = new DigitalNameplateV3.Builder("N/A", "N/A", "N/A", "N/A").build();
                this.createResourceAasAndSubmodels(resource, digitalNameplateV3);
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
                    ResourceAas.createAasIdFromResourceId(resourceId));
            return aasDescriptorOptional;
        } catch (org.eclipse.digitaltwin.basyx.aasregistry.client.ApiException e) {
            LOG.error(e.getMessage());
            return Optional.empty();
        }
    }

    private String getResourcesSubmodelRepositoryUrl(Base64UrlEncodedIdentifier aasId) {
        var basePath = ResourcesSubmodelRepositoryHTTPApiController.class.getAnnotation(RequestMapping.class).value()[0];
        basePath = basePath.replace("{aasId}", aasId.getEncodedIdentifier());
        var url = externalUrl + basePath;

        return url;
    }

    public void createResourceAasAndSubmodels(BasicResource resource, DigitalNameplateV3 digitalNameplateV3) {
        try {
            // Create AAS if it does not exist
            AssetAdministrationShell resourceAAS;
            var resourceAASOptional = this.aasRepositoryClient.getAas(ResourceAas.createAasIdFromResourceId(resource.getId()));
            if (resourceAASOptional.isEmpty()) {
                resourceAAS = new ResourceAas(resource);
                this.aasRepositoryClient.createOrUpdateAas(resourceAAS);
            }
            else {
                resourceAAS = resourceAASOptional.get();
            }
            var resourceAASIdEncoded = new Base64UrlEncodedIdentifier(resourceAAS.getId());

            // Create submodel DigitalNameplate (if it does not exist)
            var digitalNameplateSubmodelExists = new AtomicBoolean(false);
            for (var submodelRef : resourceAAS.getSubmodels()) {
                var submodelId = submodelRef.getKeys().get(0).getValue();
                var optionalSubmodelDescriptor = this.submodelRegistryClient.findSubmodelDescriptor(submodelId);
                optionalSubmodelDescriptor.ifPresent(submodelDescriptor -> {
                    if (submodelDescriptor.getSemanticId() != null) {
                        var semanticId = submodelDescriptor.getSemanticId().getKeys().get(0).getValue();
                        if (semanticId.equals(IDTASubmodelTemplates.NAMEPLATE_V2_SUBMODEL_SEMANTIC_ID)
                                || semanticId.equals(IDTASubmodelTemplates.NAMEPLATE_V3_SUBMODEL_SEMANTIC_ID)) {
                            digitalNameplateSubmodelExists.set(true);
                            LOG.info("DigitalNameplate submodel already exists for resource [id='" + resource.getId() + "'], " +
                                    "skipping registration of digital nameplate submodel");
                        }
                    }
                });
            }

            if (!digitalNameplateSubmodelExists.get()) {
                var digitalNameplateSubmodelId = DigitalNameplateV3Submodel.SUBMODEL_IDSHORT + "-" + resource.getId();
                var digitalNameplateSubmodel = new DigitalNameplateV3Submodel(digitalNameplateSubmodelId, digitalNameplateV3);
                this.submodelRepositoryClient.createOrUpdateSubmodel(digitalNameplateSubmodel);
                this.aasRepositoryClient.addSubmodelReferenceToAas(resourceAAS.getId(), digitalNameplateSubmodelId);
            }

            // Create submodel PlatformResources
            var platformResourcesSubmodelId = "PlatformResources-" + resource.getId();
            var platformResourcesSubmodelUrl = this.monitoringServiceUrl + "/" + resource.getId() + "/submodel";
            this.aasRepositoryClient.addSubmodelReferenceToAas(resourceAAS.getId(), platformResourcesSubmodelId);
            this.submodelRegistryClient.registerSubmodel(
                    platformResourcesSubmodelUrl,
                    platformResourcesSubmodelId,
                    platformResourcesSubmodelId,
                    null);

            // Create submodel DeviceInfo
            var deviceInfoSubmodelId =  DeviceInfoSubmodel.SUBMODEL_ID_SHORT + "-" + resource.getId();
            var deviceInfoSubmodelIdEncoded = new Base64UrlEncodedIdentifier(deviceInfoSubmodelId);
            var deviceInfoSubmodelUrl = this.getResourcesSubmodelRepositoryUrl(resourceAASIdEncoded)
                    + "/submodels/" + deviceInfoSubmodelIdEncoded.getEncodedIdentifier();
            this.aasRepositoryClient.addSubmodelReferenceToAas(resourceAAS.getId(), deviceInfoSubmodelId);
            this.submodelRegistryClient.registerSubmodel(
                    deviceInfoSubmodelUrl,
                    deviceInfoSubmodelId,
                    deviceInfoSubmodelId,
                    DeviceInfoSubmodel.SEMANTIC_ID_VALUE);

        }
        catch (ApiException e) {
            LOG.error("Unable to create AAS and submodels for resource [id='" + resource.getId() + "']: " + e.getMessage());
        }
    }

    private void deleteResourceAasAndSubmodels (UUID resourceId) {
        try {
            var resourceAasId = ResourceAas.createAasIdFromResourceId(resourceId);
            var resourceAasOptional = aasRepositoryClient.getAas(resourceAasId);
            if (resourceAasOptional.isEmpty()) {
                LOG.error("Resource AAS with ID {} not found", resourceAasId);
                throw new ShellNotFoundException(resourceAasId);
            }
            var resourceAas = resourceAasOptional.get();

            for (var submodelRef : resourceAas.getSubmodels()) {
                if (submodelRef.getKeys().get(0).getType().equals(KeyTypes.SUBMODEL)) {
                    var submodelId = submodelRef.getKeys().get(0).getValue();

                    var submodelDescriptorOptional = this.submodelRegistryClient.findSubmodelDescriptor(submodelId);
                    if (submodelDescriptorOptional.isPresent()) {
                        var endpoint = submodelDescriptorOptional.get().getEndpoints().get(0).getProtocolInformation().getHref();

                        if (endpoint.startsWith(this.submodelRepositoryClient.getSubmodelRepositoryUrl())) {
                            this.submodelRepositoryClient.deleteSubmodel(submodelId);
                        }
                        else {
                            try {
                                this.submodelRegistryClient.unregisterSubmodel(submodelId);
                            } catch (ApiException e) {
                                LOG.error("Unable to unregister submodel [id='" + submodelId + "']: " + e.getMessage());
                            }
                        }
                    }
                }
            }
            this.aasRepositoryClient.deleteAAS(resourceAasId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onApplicationEvent(ResourceEvent resourceEvent) {
        switch (resourceEvent.getOperation()) {
            case CREATE -> {
                // Handled in ResourcesManager.addExistingResource
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
