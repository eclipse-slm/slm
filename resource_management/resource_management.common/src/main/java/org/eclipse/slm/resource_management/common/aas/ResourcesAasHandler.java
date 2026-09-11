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

import java.net.URI;
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
                    if (submodelDescriptor.getSemanticId() != null && !submodelDescriptor.getSemanticId().getKeys().isEmpty()) {
                        var semanticId = submodelDescriptor.getSemanticId().getKeys().get(0).getValue();
                        if (semanticId.equals(IDTASubmodelTemplates.NAMEPLATE_V2_SUBMODEL_SEMANTIC_ID)
                                || semanticId.equals(IDTASubmodelTemplates.NAMEPLATE_V3_SUBMODEL_SEMANTIC_ID)) {
                            digitalNameplateSubmodelExists.set(true);
                            LOG.info("DigitalNameplate submodel [id='{}'] already exists for resource [id='{}'], skipping creation",
                                    submodelId, resource.getId());
                        }
                    }
                });
            }

            if (!digitalNameplateSubmodelExists.get()) {
                var digitalNameplateSubmodelId = DigitalNameplateV3Submodel.SUBMODEL_IDSHORT + "-" + resource.getId();
                LOG.info("Creating DigitalNameplate submodel [id='{}'] for resource [id='{}']", digitalNameplateSubmodelId, resource.getId());
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
            LOG.error("Unable to create AAS and submodels for resource [id='" + resource.getId() + "']: " + e.getMessage(), e);
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

                        if (isHostedInSubmodelRepository(endpoint)) {
                            LOG.info("Deleting submodel [id='{}'] from submodel repository (registry endpoint='{}')", submodelId, endpoint);
                            this.submodelRepositoryClient.deleteSubmodel(submodelId);
                        }
                        else {
                            try {
                                LOG.info("Unregistering externally hosted submodel [id='{}'] (registry endpoint='{}')", submodelId, endpoint);
                                this.submodelRegistryClient.unregisterSubmodel(submodelId);
                            } catch (FeignResponseException e) {
                                LOG.error("Unable to unregister submodel [id='" + submodelId + "']: " + e.getMessage());
                            }
                        }
                    }
                    // If submodel descriptor was not found try to delete submodel in the default submodel repository
                    else {
                        try {
                            LOG.info("No descriptor found in registry for submodel [id='{}'], deleting it from default submodel repository", submodelId);
                            this.submodelRepositoryClient.deleteSubmodel(submodelId);
                        }
                        catch (Exception e) {
                            LOG.warn("Unable to cleanup submodel [id='{}'] in default submodel repository: {}", submodelId, e.getMessage(), e);
                        }
                    }
                }
            }
            this.aasRepositoryClient.deleteAAS(resourceAasId);
            LOG.info("Deleted resource AAS [id='{}'] and cleaned up its submodels", resourceAasId);
        } catch (Exception e) {
            LOG.error("Failed to delete AAS and submodels for resource [id='{}']: {}", resourceId, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Determines whether a submodel is physically stored in the (BaSyx) submodel repository by comparing the
     * <em>path</em> of the registered endpoint with the submodel repository path.
     * <p>
     * The comparison is intentionally path-based: the endpoint auto-registered by the submodel repository uses
     * the externally reachable host/scheme/port, which differs from the internally configured submodel repository
     * URL used by this service. A full-URL comparison therefore misclassifies repository-hosted submodels (e.g. the
     * Digital Nameplate) as externally hosted, which would only unregister them instead of deleting their data -
     * leaving orphaned submodel data behind.
     */
    private boolean isHostedInSubmodelRepository(String endpoint) {
        var submodelRepositoryUrl = this.submodelRepositoryClient.getSubmodelRepositoryUrl();
        try {
            var repositoryPath = URI.create(submodelRepositoryUrl).getPath();
            if (repositoryPath == null || repositoryPath.isBlank() || repositoryPath.equals("/")) {
                // No meaningful repository path configured -> fall back to full-URL comparison
                return endpoint.startsWith(submodelRepositoryUrl);
            }
            var endpointPath = URI.create(endpoint).getPath();
            return endpointPath != null && endpointPath.startsWith(repositoryPath);
        } catch (IllegalArgumentException e) {
            // Malformed URL -> fall back to the original full-URL comparison
            return endpoint.startsWith(submodelRepositoryUrl);
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
