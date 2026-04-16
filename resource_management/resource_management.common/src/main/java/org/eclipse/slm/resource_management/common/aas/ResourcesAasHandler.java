package org.eclipse.slm.resource_management.common.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.slm.aas.clients.*;
import org.eclipse.slm.aas.clients.base.FeignResponseException;
import org.eclipse.slm.aas.model.shellrepository.exceptions.ShellNotFoundException;
import org.eclipse.slm.aas.clients.shellregistry.AasRegistryClient;
import org.eclipse.slm.aas.clients.shellregistry.AasRegistryClientFactory;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClient;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClientFactory;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClient;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClientFactory;
import org.eclipse.slm.aas.clients.submodelrepository.SubmodelRepositoryClient;
import org.eclipse.slm.aas.clients.submodelrepository.SubmodelRepositoryClientFactory;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.aas.submodels.ResourcesSubmodelRepositoryHTTPApiController;
import org.eclipse.slm.resource_management.common.aas.submodels.deviceinfo.DeviceInfoSubmodel;
import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3;
import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3Submodel;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClientFactory;
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

import java.util.Collections;
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

    private final ResourcesConsulClientFactory resourcesConsulClientFactory;
    private final ResourcesConsulClient resourcesConsulAdminClient;

    private final String monitoringServiceUrl;

    private final String externalUrl;

    public static final String SUBMODEL_PLATFORM_RESOURCES_ID_SHORT = "PlatformResources";
    public static final String SEMANTIC_ID_PLATFORM_RESOURCES_VALUE = "https://eclipse.dev/slm/aas/sm/PlatformResources";

    public static final Reference SEMANTIC_ID_PLATFORM_RESOURCES = new DefaultReference.Builder()
            .type(ReferenceTypes.EXTERNAL_REFERENCE)
            .keys(Collections.singletonList(
            new DefaultKey.Builder()
                            .value(SEMANTIC_ID_PLATFORM_RESOURCES_VALUE)
                            .type(KeyTypes.GLOBAL_REFERENCE)
                            .build()))
            .build();


    public ResourcesAasHandler(AasRegistryClientFactory aasRegistryClientFactory,
                               AasRepositoryClientFactory aasRepositoryClientFactory,
                               SubmodelRegistryClientFactory submodelRegistryClientFactory,
                               SubmodelRepositoryClientFactory submodelRepositoryClientFactory,
                               ResourcesConsulClientFactory resourcesConsulClientFactory,
                               @Value("${monitoring.service.url}") String monitoringServiceUrl,
                               @Value("${deployment.url}") String externalUrl) {
        this.aasRegistryClient = aasRegistryClientFactory.getClient();
        this.aasRepositoryClient = aasRepositoryClientFactory.getClient();
        this.submodelRegistryClient = submodelRegistryClientFactory.getClient();
        this.submodelRepositoryClient = submodelRepositoryClientFactory.getClient();
        this.resourcesConsulClientFactory = resourcesConsulClientFactory;
        this.resourcesConsulAdminClient = resourcesConsulClientFactory.createAdminClient();
        this.monitoringServiceUrl = monitoringServiceUrl;
        this.externalUrl = externalUrl;
    }

    @PostConstruct
    public void init() {
        // Create AAS for all resources
        try {
            var resources = resourcesConsulAdminClient.getResources();

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
        } catch (Exception e) {
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
                var optionalSubmodelDescriptor = this.submodelRegistryClient.getSubmodelDescriptor(submodelId);
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
            var platformResourcesSubmodelId = SUBMODEL_PLATFORM_RESOURCES_ID_SHORT + "-" + resource.getId();
            var platformResourcesSubmodelUrl = this.monitoringServiceUrl + "/" + resource.getId() + "/submodel";
            this.aasRepositoryClient.addSubmodelReferenceToAas(resourceAAS.getId(), platformResourcesSubmodelId);
            this.submodelRegistryClient.registerSubmodel(
                    platformResourcesSubmodelUrl,
                    platformResourcesSubmodelId,
                    platformResourcesSubmodelId,
                    ResourcesAasHandler.SEMANTIC_ID_PLATFORM_RESOURCES);

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
        catch (FeignResponseException e) {
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

                    var submodelDescriptorOptional = this.submodelRegistryClient.getSubmodelDescriptor(submodelId);
                    if (submodelDescriptorOptional.isPresent()) {
                        var endpoint = submodelDescriptorOptional.get().getEndpoints().get(0).getProtocolInformation().getHref();

                        if (endpoint.startsWith(this.submodelRepositoryClient.getSubmodelRepositoryUrl())) {
                            this.submodelRepositoryClient.deleteSubmodel(submodelId);
                        }
                        else {
                            try {
                                this.submodelRegistryClient.unregisterSubmodel(submodelId);
                            } catch (FeignResponseException e) {
                                LOG.error("Unable to unregister submodel [id='" + submodelId + "']: " + e.getMessage());
                            }
                        }
                    }
                    // If submodel descriptor was not found try to delete submodel in the default submodel repository
                    else {
                        try {
                            this.submodelRepositoryClient.deleteSubmodel(submodelId);
                        }
                        catch (Exception e) {
                            LOG.debug("Unable to cleanup submodel [id='" + submodelId + "'] in default submodel repository: " + e.getMessage(), e);
                        }
                    }
                }
            }
            this.aasRepositoryClient.deleteAAS(resourceAasId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateSubmodelElementOfResourceAasSubmodel(UUID resourceId, String submodelIdShort, String submodelElementIdShortPath, SubmodelElement submodelElement) {
        try {
            var resourceAASOptional = this.aasRepositoryClient.getAas(ResourceAas.createAasIdFromResourceId(resourceId));

            if (resourceAASOptional.isPresent()) {
                for (var submodelRef : resourceAASOptional.get().getSubmodels()) {
                    if (submodelRef.getKeys().get(0).getType().equals(KeyTypes.SUBMODEL)) {
                        var submodelId = submodelRef.getKeys().get(0).getValue();

                        var submodelDescriptorOptional = this.submodelRegistryClient.getSubmodelDescriptor(submodelId);
                        if (submodelDescriptorOptional.isPresent()) {
                            if (submodelDescriptorOptional.get().getIdShort().equals(submodelIdShort)) {
                                var submodelRepositoryClient = SubmodelRepositoryClientFactory.FromSubmodelDescriptor(submodelDescriptorOptional.get());

                                submodelRepositoryClient.createOrUpdateSubmodelElement(
                                        submodelDescriptorOptional.get().getId(),
                                        submodelElementIdShortPath,
                                        submodelElement);
                            }
                        }
                    }
                }
            } else {
                throw new ShellNotFoundException(ResourceAas.createAasIdFromResourceId(resourceId));
            }
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
