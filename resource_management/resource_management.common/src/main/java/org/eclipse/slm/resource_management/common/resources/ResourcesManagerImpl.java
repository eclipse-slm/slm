package org.eclipse.slm.resource_management.common.resources;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.slm.common.aas.model.shellrepository.exceptions.ShellNotFoundException;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClientFactory;
import org.eclipse.slm.resource_management.common.adapters.ResourcesVaultClient;
import org.eclipse.slm.resource_management.common.aas.ResourcesAasHandler;
import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3;
import org.eclipse.slm.resource_management.common.exceptions.ResourceDefinitionException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceRuntimeException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.ports.ICapabilitiesManager;
import org.eclipse.slm.resource_management.common.location.LocationJpaRepository;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ResourcesManagerImpl implements ResourcesManager, ResourceUpdatedListener {
    private final static Logger LOG = LoggerFactory.getLogger(ResourcesManagerImpl.class);

    private final ResourcesConsulClientFactory resourcesConsulClientFactory;
    private final ResourcesConsulClient resourcesConsulAdminClient;
    private final ResourcesVaultClient resourcesVaultClient;

    private final Optional<ICapabilitiesManager> capabilitiesService;

    private final LocationJpaRepository locationJpaRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final ResourcesAasHandler resourcesAasHandler;

    private final ResourceEventMessageSender resourceEventMessageSender;

    private final RemoteAccessManager remoteAccessManager;

    @Autowired
    public ResourcesManagerImpl(
            ResourcesConsulClientFactory resourcesConsulClientFactory,
            ResourcesVaultClient resourcesVaultClient,
            Optional<ICapabilitiesManager> capabilitiesService,
            LocationJpaRepository locationJpaRepository,
            ApplicationEventPublisher applicationEventPublisher, ResourcesAasHandler resourcesAasHandler,
            ResourceEventMessageSender resourceEventMessageSender,
            RemoteAccessManager remoteAccessManager
    ) {
        this.resourcesConsulClientFactory = resourcesConsulClientFactory;
        this.resourcesConsulAdminClient = resourcesConsulClientFactory.createAdminClient();
        this.resourcesVaultClient = resourcesVaultClient;
        this.capabilitiesService = capabilitiesService;
        this.locationJpaRepository = locationJpaRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.resourcesAasHandler = resourcesAasHandler;
        this.resourceEventMessageSender = resourceEventMessageSender;
        this.remoteAccessManager = remoteAccessManager;
        this.remoteAccessManager.registerResourceUpdatedListener(this);
    }

    public List<BasicResource> getResources(String jwtAccessToken) throws ResourceNotFoundException, ResourceRuntimeException {
        try {
            var resourcesConsulClient = this.resourcesConsulClientFactory.create(jwtAccessToken);
            List<BasicResource> resources = resourcesConsulClient.getResources();

            for (var resource : resources) {
                this.addDetailsToResource(resource, jwtAccessToken);
            }

            // remove resource if cluster // ToDo: clusters - include a more specific property to differentiate between cluster/non-clusters
            resources.removeIf(r -> r.getIp().contains("-cluster"));

            return resources;
        } catch (Exception e) {
            LOG.error("Failed to get resources: {}", e.getMessage(), e);
            throw new ResourceRuntimeException("Failed to get resources:" + e.getMessage(), e);
        }
    }

    @Override
    public Optional<BasicResource> getResourceById(UUID resourceId, String jwtAccessToken) throws ResourceRuntimeException {
        try {
            return Optional.of(this.getResourceByIdOrThrow(resourceId, jwtAccessToken));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }


    public BasicResource getResourceByIdOrThrow(UUID resourceId, String jwtAccessToken) throws ResourceRuntimeException, ResourceNotFoundException {
        try {
            var resourcesConsulClient = this.resourcesConsulClientFactory.create(jwtAccessToken);
            Optional<BasicResource> optionalResource = resourcesConsulClient.getResourceById(resourceId);

            if (optionalResource.isEmpty()) {
                throw new ResourceNotFoundException(resourceId);
            }

            var resource = this.addDetailsToResource(optionalResource.get(), jwtAccessToken);

            return resource;
        } catch (ResourceNotFoundException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ResourceRuntimeException("Failed to get resource by id: " + resourceId + " - " + e.getMessage(), e);
        }
    }

    private BasicResource addDetailsToResource(BasicResource resource, String jwtAccessToken) {
        if (this.capabilitiesService.isPresent()) {
            var capabilityServicesIds = this.capabilitiesService.get().getCapabilityServiceIdsOfResource(resource.getId());
            resource.setCapabilityServiceIds(capabilityServicesIds);

            var isClusterMember = this.capabilitiesService.get().isResourceClusterMember(resource.getId());
            resource.setClusterMember(isClusterMember);
        }

        var remoteAccessServicesIds = this.remoteAccessManager.getRemoteAccessIdsOfResource(resource.getId(), jwtAccessToken);
        resource.setRemoteAccessIds(remoteAccessServicesIds);

        return resource;
    }

    public BasicResource createResource(
            UUID resourceId,
            String assetId,
            String resourceHostname,
            String resourceIp,
            String firmwareVersion,
            String driverId,
            DigitalNameplateV3 digitalNameplateV3,
            String fullPathOwnerGroupId
    ) throws ResourceNotFoundException, ResourceRuntimeException, ResourceDefinitionException {
        try {
            if (resourceHostname == null || resourceHostname.isEmpty()) {
                throw new ResourceDefinitionException("Resource hostname cannot be null or empty");
            }
            if (resourceIp == null || resourceIp.isEmpty()) {
                throw new ResourceDefinitionException("Resource IP cannot be null or empty");
            }

            if (driverId == null) {
                driverId = "N/A";
            }

            var resource = new BasicResource(resourceId, resourceHostname, resourceIp);
            resource.setAssetId(assetId);
            resource.setFirmwareVersion(firmwareVersion);
            resource.setDriverId(driverId);
            resource = this.resourcesConsulAdminClient.addResource(resource, fullPathOwnerGroupId);

            this.resourcesVaultClient.initResourceKV(resourceId, fullPathOwnerGroupId);
            this.resourcesVaultClient.createIntermediateCertificateAuthority(resource.getId(), resource.getIp(), resource.getHostname());

            this.resourcesAasHandler.createResourceAasAndSubmodels(resource, digitalNameplateV3);

            this.resourceEventMessageSender.sendMessage(resource, ResourceEventType.CREATED);

            return resource;
        }
        catch (Exception e) {
            LOG.error("Failed to add resource: {}", e.getMessage(), e);
            throw new ResourceRuntimeException("Failed to add resource: " + e.getMessage(), e);
        }
    }

    public void deleteResource(UUID resourceId, String jwtAccessToken) throws ResourceNotFoundException, ResourceRuntimeException {
        try {
            var resource = this.getResourceByIdOrThrow(resourceId, jwtAccessToken);

            for (var remoteAccessServiceId : resource.getRemoteAccessIds()) {
                this.remoteAccessManager.deleteRemoteAccessById(resourceId, remoteAccessServiceId, jwtAccessToken, false);
            }

            this.resourcesConsulAdminClient.deleteResource(resource);
            this.resourcesVaultClient.removeSecretsForResource(resource.getId());
            this.resourcesVaultClient.removeIntermediateCertificateAuthority(resource.getId());

            this.resourceEventMessageSender.sendMessage(resource, ResourceEventType.DELETED);
            this.applicationEventPublisher.publishEvent(new ResourceEvent(this, resourceId, ResourceEvent.Operation.DELETE));
        } catch (ShellNotFoundException ignored) {
        } catch (Exception e) {
            throw new ResourceRuntimeException("Failed to delete resource: " + e.getMessage(), e);
        }
    }

    @Override
    public void setLocationOfResource(UUID resourceId, UUID locationId, String jwtAccessToken)  {
        this.getResourceByIdOrThrow(resourceId, jwtAccessToken); // check if resource exists and use has access - exception will be thrown if not
        var optionalLocation = locationJpaRepository.findById(locationId);

        this.resourcesConsulAdminClient.setResourceLocation(resourceId, optionalLocation.get());
    }

    @Override
    public String getConnectionParametersOfResource(UUID resourceId) {
        var kvContent = this.resourcesVaultClient.getSecretsForResource(resourceId, "ConnectionParameters");
        var connectionParametersContent = kvContent.get("ConnectionParameters");

        return connectionParametersContent;
    }

    @Override
    public void setConnectionParametersOfResource(UUID resourceId, String connectionParameters) {
        this.resourcesVaultClient.addSecretsForResource(resourceId, "ConnectionParameters", Map.of("ConnectionParameters", connectionParameters));
    }

    @Override
    public void setFirmwareVersionOfResource(UUID resourceId, String firmwareVersion) {
        try {
            this.resourcesConsulAdminClient.getResourceById(resourceId).ifPresentOrElse(
                (resource) -> {
                    resource.setFirmwareVersion(firmwareVersion);
                        this.resourcesConsulAdminClient.updateResource(resource);
                },
                () -> {
                    LOG.error("Resource with id: " + resourceId + " not found. Cannot set firmware version.");
                });
        } catch (ResourceRuntimeException e) {
            LOG.error("Failed to set firmware version for resource with id: {}", resourceId, e);
        }
    }

    @Override
    public void updateResource(UUID resourceId, ResourceUpdateRequest updateResourceRequest, String jwtAccessToken)
            throws ResourceNotFoundException, ResourceRuntimeException {
        var resource = this.getResourceByIdOrThrow(resourceId, jwtAccessToken); // check if resource exists and use has access - exception will be thrown if not

        // TODO: Improve update of AAS Nameplate Submodel information - currently each property is updated separately and this class needs to know about the
        //  structure of the Nameplate Submodel. Maybe implement a method in the ResourcesAasHandler that takes care of updating the Nameplate Submodel based
        //  on the provided ResourceUpdateRequest
        if (updateResourceRequest.getProduct() != null) {
            var updatedSubmodelElement = new DefaultMultiLanguageProperty.Builder()
                    .idShort("ManufacturerProductDesignation")
                    .value(new DefaultLangStringTextType.Builder()
                                    .language("en")
                                    .text(updateResourceRequest.getProduct())
                            .build()
                    ).build();
            resourcesAasHandler.updateSubmodelElementOfResourceAasSubmodel(
                    resourceId,
                    "Nameplate",
                    "ManufacturerProductDesignation",
                    updatedSubmodelElement);
        }
        if (updateResourceRequest.getVendor() != null) {
            var updatedSubmodelElement = new DefaultMultiLanguageProperty.Builder()
                    .idShort("ManufacturerName")
                    .value(new DefaultLangStringTextType.Builder()
                            .language("en")
                            .text(updateResourceRequest.getVendor())
                            .build()
                    ).build();
            resourcesAasHandler.updateSubmodelElementOfResourceAasSubmodel(
                    resourceId,
                    "Nameplate",
                    "ManufacturerName",
                    updatedSubmodelElement);
        }
        if (updateResourceRequest.getAssetId() != null) {
            var updatedSubmodelElement = new DefaultProperty.Builder()
                    .idShort("URIOfTheProduct")
                    .valueType(DataTypeDefXsd.STRING)
                    .value(updateResourceRequest.getAssetId())
                    .build();
            resourcesAasHandler.updateSubmodelElementOfResourceAasSubmodel(
                    resourceId,
                    "Nameplate",
                    "URIOfTheProduct",
                    updatedSubmodelElement);
        }

        var consulUpdateRequired = false;
        if (updateResourceRequest.getHostname() != null) {
            resource.setHostname(updateResourceRequest.getHostname());
            consulUpdateRequired = true;
        }
        if (updateResourceRequest.getIp() != null) {
            resource.setIp(updateResourceRequest.getIp());
            consulUpdateRequired = true;
        }
        if (consulUpdateRequired) {
            this.resourcesConsulAdminClient.updateResource(resource);
        }

        this.onResourceUpdated(resourceId, jwtAccessToken);
    }

    //region ResourceUpdatedListener
    @Override
    public void onResourceUpdated(UUID resourceId, String jwtAccessToken) {
        var resource = this.getResourceByIdOrThrow(resourceId, jwtAccessToken);
        this.resourceEventMessageSender.sendMessage(resource, ResourceEventType.UPDATED);
    }
    //endregion ResourceUpdatedListener
}
