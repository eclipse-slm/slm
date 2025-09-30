package org.eclipse.slm.resource_management.common.resources;

import org.eclipse.slm.common.aas.clients.exceptions.ShellNotFoundException;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.adapters.ResourcesVaultClient;
import org.eclipse.slm.resource_management.common.aas.ResourcesAasHandler;
import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3;
import org.eclipse.slm.resource_management.common.exceptions.ResourceRuntimeException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.ports.ICapabilitiesService;
import org.eclipse.slm.resource_management.common.location.LocationJpaRepository;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ResourcesManagerImpl implements ResourcesManager, ResourceUpdatedListener {
    private final static Logger LOG = LoggerFactory.getLogger(ResourcesManagerImpl.class);

    private final KeycloakAdminClient keycloakAdminClient;

    private final ResourcesConsulClient resourcesConsulClient;
    private final ResourcesVaultClient resourcesVaultClient;

    private final Optional<ICapabilitiesService> capabilitiesService;

    private final LocationJpaRepository locationJpaRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final ResourcesAasHandler resourcesAasHandler;

    private final ResourceEventMessageSender resourceEventMessageSender;

    private final RemoteAccessManager remoteAccessManager;

    @Autowired
    public ResourcesManagerImpl(
            ResourcesConsulClient resourcesConsulClient,
            ResourcesVaultClient resourcesVaultClient,
            KeycloakAdminClient keycloakAdminClient,
            Optional<ICapabilitiesService> capabilitiesService,
            LocationJpaRepository locationJpaRepository,
            ApplicationEventPublisher applicationEventPublisher, ResourcesAasHandler resourcesAasHandler,
            ResourceEventMessageSender resourceEventMessageSender,
            RemoteAccessManager remoteAccessManager
    ) {
        this.resourcesConsulClient = resourcesConsulClient;
        this.resourcesVaultClient = resourcesVaultClient;
        this.keycloakAdminClient = keycloakAdminClient;
        this.capabilitiesService = capabilitiesService;
        this.locationJpaRepository = locationJpaRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.resourcesAasHandler = resourcesAasHandler;
        this.resourceEventMessageSender = resourceEventMessageSender;
        this.remoteAccessManager = remoteAccessManager;
        this.remoteAccessManager.registerResourceUpdatedListener(this);
    }

    public List<BasicResource> getResources(
            JwtAuthenticationToken jwtAuthenticationToken
    ) throws ResourceNotFoundException, ResourceRuntimeException {
        try {
            ConsulCredential consulCredential = new ConsulCredential(jwtAuthenticationToken);

            List<BasicResource> resources = resourcesConsulClient.getResources(consulCredential);

            for (var resource : resources) {
                this.addDetailsToResource(resource);
            }

            // remove resource if cluster // ToDo: clusters - include a more specific property to differentiate between cluster/non-clusters
            resources.removeIf(r -> r.getIp().contains("-cluster"));

            return resources;
        } catch (Exception e) {
            LOG.error("Failed to get resources: {}", e.getMessage(), e);
            throw new ResourceRuntimeException("Failed to get resources:" + e.getMessage());
        }
    }

    public BasicResource getResourceByIdOrThrow(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID resourceId
    ) throws ResourceRuntimeException, ResourceNotFoundException {
        Optional<BasicResource> optionalResource;
        try {
            var consulCredential = new ConsulCredential();
            optionalResource = resourcesConsulClient.getResourceById(
                    consulCredential,
                    resourceId
            );
        } catch (Exception e) {
            LOG.error("Failed to get resource by id '{}': {}", resourceId, e.getMessage(), e);
            throw new ResourceRuntimeException("Failed to get resource by id: " + resourceId + " - " + e.getMessage());
        }

        if (optionalResource.isEmpty()) {
            throw new ResourceNotFoundException(resourceId);
        }

        var resource = this.addDetailsToResource(optionalResource.get());

        return resource;

    }

    public BasicResource getResourceByIdOrThrow(
            UUID resourceId
    ) throws ResourceRuntimeException, ResourceNotFoundException {
        try {
            var consulCredential = new ConsulCredential();

            Optional<BasicResource> optionalResource = resourcesConsulClient.getResourceById(
                    consulCredential,
                    resourceId
            );

            if (optionalResource.isEmpty()) {
                throw new ResourceNotFoundException(resourceId);
            }

            var resource = this.addDetailsToResource(optionalResource.get());

            return resource;
        } catch (Exception e) {
            LOG.error("Failed to get resource by id '{}': {}", resourceId, e.getMessage(), e);
            throw new ResourceRuntimeException("Failed to get resource by id: " + resourceId + " - " + e.getMessage());
        }
    }

    private BasicResource addDetailsToResource(BasicResource resource) {
        if (this.capabilitiesService.isPresent()) {
            var capabilityServicesIds = this.capabilitiesService.get().getCapabilityServiceIdsOfResource(resource.getId());
            resource.setCapabilityServiceIds(capabilityServicesIds);

            var isClusterMember = this.capabilitiesService.get().isResourceClusterMember(resource.getId());
            resource.setClusterMember(isClusterMember);
        }

        var remoteAccessServicesIds = this.remoteAccessManager.getRemoteAccessServiceIdsOfResource(resource.getId());
        resource.setRemoteAccessIds(remoteAccessServicesIds);

        return resource;
    }

    public BasicResource addResource(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID resourceId,
            String assetId,
            String resourceHostname,
            String resourceIp,
            String firmwareVersion,
            String driverId,
            DigitalNameplateV3 digitalNameplateV3
    ) throws ResourceNotFoundException, ResourceRuntimeException {
        try {
            // Create realm role in Keycloak for new resource
            var resourceKeycloakRoleName = "resource_" + resourceId;
            var ownerUserId = jwtAuthenticationToken.getToken().getSubject();
            this.keycloakAdminClient.createRealmRoleAndAssignToUser(ownerUserId, resourceKeycloakRoleName);

            if (driverId == null) {
                driverId = "N/A";
            }

            var resource = new BasicResource(resourceId, resourceHostname, resourceIp);
            resource.setAssetId(assetId);
            resource.setFirmwareVersion(firmwareVersion);
            resource.setDriverId(driverId);
            resource = this.resourcesConsulClient.addResource(resource);

            this.resourcesVaultClient.initResourceKV(resourceId);
            this.resourcesVaultClient.createIntermediateCertificateAuthority(resource.getId(), resource.getIp(), resource.getHostname());

            this.resourcesAasHandler.createResourceAasAndSubmodels(resource, digitalNameplateV3);

            this.resourceEventMessageSender.sendMessage(resource, ResourceEventType.CREATED);

            return resource;
        }
        catch (Exception e) {
            LOG.error("Failed to add resource: {}", e.getMessage(), e);
            throw new ResourceRuntimeException("Failed to add resource: " + e.getMessage());
        }
    }

    public void deleteResource(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID resourceId
    ) throws ResourceNotFoundException, ResourceRuntimeException {
        try {
            var resource = this.getResourceByIdOrThrow(jwtAuthenticationToken, resourceId);

            for (var remoteAccessServiceId : resource.getRemoteAccessIds()) {
                this.remoteAccessManager.deleteRemoteAccess(resourceId, remoteAccessServiceId);
            }

            var realmRolesToDelete = new ArrayList<String>();
            realmRolesToDelete.add("resource_" + resourceId);

            this.keycloakAdminClient.deleteRealmRoles(realmRolesToDelete);
            this.resourcesConsulClient.deleteResource(new ConsulCredential(), resource);
            this.resourcesVaultClient.removeSecretsForResource(new VaultCredential(), resource.getId());
            this.resourcesVaultClient.removeIntermediateCertificateAuthority(resource.getId());

            this.resourceEventMessageSender.sendMessage(resource, ResourceEventType.DELETED);
            this.applicationEventPublisher.publishEvent(new ResourceEvent(this, resourceId, ResourceEvent.Operation.DELETE));
        } catch (ShellNotFoundException ignored) {
        } catch (Exception e) {
            throw new ResourceRuntimeException("Failed to delete resource: " + e.getMessage());
        }
    }

    public void setLocationOfResource(UUID resourceId, UUID locationId) throws ConsulLoginFailedException {
        var optionalLocation = locationJpaRepository.findById(locationId);

        this.resourcesConsulClient.setResourceLocation(resourceId, optionalLocation.get());
    }

    public String getConnectionParametersOfResource(UUID resourceId) {
        var kvContent = this.resourcesVaultClient.getSecretsForResource(new VaultCredential(), resourceId, "ConnectionParameters");

        var connectionParametersContent = kvContent.get("ConnectionParameters");

        return connectionParametersContent;
    }

    public void setConnectionParametersOfResource(UUID resourceId, String connectionParameters) {
        this.resourcesVaultClient.addSecretsForResource(new VaultCredential(), resourceId, "ConnectionParameters", Map.of("ConnectionParameters", connectionParameters));
    }

    public void setFirmwareVersionOfResource(UUID resourceId, String firmwareVersion) {
        try {
            this.resourcesConsulClient.getResourceById(new ConsulCredential(), resourceId).ifPresentOrElse(
                (resource) -> {
                    resource.setFirmwareVersion(firmwareVersion);
                    try {
                        this.resourcesConsulClient.addResource(resource);
                    } catch (ConsulLoginFailedException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    LOG.error("Resource with id: " + resourceId + " not found. Cannot set firmware version.");
                });
        } catch (ConsulLoginFailedException e) {
            LOG.error("Failed to set firmware version for resource with id: " + resourceId, e);
        }
    }

    //region ResourceUpdatedListener
    @Override
    public void onResourceUpdated(UUID resourceId) {
        var resource = this.getResourceByIdOrThrow(resourceId);
        this.resourceEventMessageSender.sendMessage(resource, ResourceEventType.UPDATED);
    }
    //endregion ResourceUpdatedListener
}
