package org.eclipse.slm.resource_management.service.rest.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakUtil;
import org.eclipse.slm.common.messaging.resources.ResourceMessageSender;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.notification_service.model.Category;
import org.eclipse.slm.notification_service.model.JobGoal;
import org.eclipse.slm.notification_service.model.JobTarget;
import org.eclipse.slm.notification_service.service.client.NotificationServiceClient;
import org.eclipse.slm.resource_management.model.resource.*;
import org.eclipse.slm.resource_management.service.rest.aas.AasHandler;
import org.eclipse.slm.resource_management.service.rest.aas.resources.digitalnameplate.DigitalNameplateV3;
import org.eclipse.slm.resource_management.service.rest.capabilities.SingleHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.model.capabilities.CapabilityNotFoundException;
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityService;
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.model.consul.capability.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.model.consul.capability.SingleHostCapabilityService;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.persistence.api.LocationJpaRepository;
import org.eclipse.slm.resource_management.service.rest.capabilities.CapabilitiesManager;
import org.eclipse.slm.resource_management.service.rest.capabilities.CapabilitiesConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import javax.net.ssl.SSLException;
import java.util.*;

@Component
public class ResourcesManager {
    private final static Logger LOG = LoggerFactory.getLogger(ResourcesManager.class);
    private final NotificationServiceClient notificationServiceClient;
    private final KeycloakUtil keycloakUtil;
    private final ResourcesConsulClient resourcesConsulClient;
    private final ResourcesVaultClient resourcesVaultClient;
    private final ConsulAclApiClient consulAclApiClient;
    private final CapabilitiesManager capabilitiesManager;
    private final CapabilitiesConsulClient capabilitiesConsulClient;
    private final SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient;
    private final LocationJpaRepository locationJpaRepository;
    private final ApplicationEventPublisher publisher;
    private final AasHandler aasHandler;
    private final ResourceMessageSender resourceMessageSender;

    @Autowired
    public ResourcesManager(
            ResourcesConsulClient resourcesConsulClient,
            ResourcesVaultClient resourcesVaultClient,
            ConsulAclApiClient consulAclApiClient,
            NotificationServiceClient notificationServiceClient,
            KeycloakUtil keycloakUtil,
            CapabilitiesManager capabilitiesManager,
            CapabilitiesConsulClient capabilitiesConsulClient,
            SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient,
            LocationJpaRepository locationJpaRepository,
            ApplicationEventPublisher publisher, AasHandler aasHandler,
            ResourceMessageSender resourceMessageSender
    ) {
        this.resourcesConsulClient = resourcesConsulClient;
        this.resourcesVaultClient = resourcesVaultClient;
        this.consulAclApiClient = consulAclApiClient;
        this.notificationServiceClient = notificationServiceClient;
        this.keycloakUtil = keycloakUtil;
        this.capabilitiesManager = capabilitiesManager;
        this.capabilitiesConsulClient = capabilitiesConsulClient;
        this.singleHostCapabilitiesConsulClient = singleHostCapabilitiesConsulClient;
        this.locationJpaRepository = locationJpaRepository;
        this.publisher = publisher;
        this.aasHandler = aasHandler;
        this.resourceMessageSender = resourceMessageSender;
    }

    public List<BasicResource> getResourcesWithCredentialsByRemoteAccessService(
            JwtAuthenticationToken jwtAuthenticationToken
    ) throws ConsulLoginFailedException, JsonProcessingException, ResourceNotFoundException {
        ConsulCredential consulCredential = new ConsulCredential(jwtAuthenticationToken);
        List<BasicResource> resources = resourcesConsulClient.getResources(consulCredential);

        for(BasicResource basicResource : resources) {

            //region add SingleHostCapabilities to resource
//            List<Capability> capabilitiesOfResource = this.capabilitiesManager.getSingleHostCapabilitiesOfResource(
//                    basicResource.getId(),
//                    consulCredential
//            );
            List<SingleHostCapabilityService> singleHostCapabilityServices =
                this.capabilitiesManager.getSingleHostCapabilityServicesOfResourceById(
                        basicResource.getId(),
                        consulCredential
                );

            List<CapabilityService> capabilityServices = this.capabilitiesManager.getCapabilityServicesOfResourceById(
                    basicResource.getId(),
                    consulCredential
            );

            //TODO Fix because returns always false (capabilitiesOfResource has only singleHostCapabilities)
            if (capabilityServices.stream().anyMatch(cs -> cs.getServiceClass().equals(MultiHostCapabilityService.class.getSimpleName()))) {
                basicResource.setClusterMember(true);
            }
            basicResource.setCapabilityServices(singleHostCapabilityServices);
            //endregion


            //region add remote access service
            Optional<NodeService> nodeService = resourcesConsulClient.getRemoteAccessServiceOfResourceAsNodeService(
                    consulCredential,
                    basicResource.getId()
            );

            if(nodeService.isPresent()) {
                UUID serviceId = UUID.fromString(nodeService.get().getID());

                List<ConnectionType> connectionTypes = resourcesConsulClient.getConnectionTypesOfRemoteAccessService(
                        consulCredential,
                        serviceId
                );

                List<CredentialClass> credentialClasses = resourcesConsulClient.getCredentialClassesOfRemoteAccessService(
                        consulCredential,
                        serviceId
                );

                //get Credential from Vault
                Credential credential = null;
                if (credentialClasses.size() > 0 && connectionTypes.size() > 0) {
                    try {
                        credential = resourcesVaultClient.getCredentialOfRemoteAccessService(
                                jwtAuthenticationToken,
                                serviceId,
                                credentialClasses.get(0)
                        );
                    } catch (HttpClientErrorException e) {
                        LOG.warn(e.getMessage());
                    }

                    RemoteAccessService remoteAccessService = new RemoteAccessService(
                            nodeService.get(),
                            connectionTypes.get(0),
                            credential
                    );

                    basicResource.setRemoteAccessService(remoteAccessService);
                }
            }
            //endregion
        }

        // remove resource if cluster // ToDo: clusters - include a more specific property to differentiate between cluster/non-clusters
        resources.removeIf( r -> r.getIp().contains("-cluster"));

        return resources;
    }

    public Optional<BasicResource> getResourceWithoutCredentials(UUID resourceId) {
        Optional<BasicResource> optionalResource = null;
        try {
            optionalResource = resourcesConsulClient.getResourceById(
                    new ConsulCredential(),
                    resourceId
            );
        } catch (ResourceNotFoundException | ConsulLoginFailedException e) {
            LOG.error(e.getMessage());
            return Optional.empty();
        }

        return optionalResource;
    }

    public BasicResource getResourceWithCredentialsByRemoteAccessService(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID resourceId
    ) throws ConsulLoginFailedException, ResourceNotFoundException, JsonProcessingException {
        ConsulCredential consulCredential = new ConsulCredential(jwtAuthenticationToken);

        Optional<BasicResource> optionalResource = resourcesConsulClient.getResourceById(
                new ConsulCredential(jwtAuthenticationToken),
                resourceId
        );

        if(optionalResource.isEmpty())
            return null;

        BasicResource resource = optionalResource.get();

        Optional<NodeService> optionalNodeService = resourcesConsulClient.getRemoteAccessServiceOfResourceAsNodeService(
                consulCredential,
                resourceId
        );

        if(optionalNodeService.isEmpty())
            return resource;

        UUID serviceId = UUID.fromString(optionalNodeService.get().getID());

        List<ConnectionType> connectionTypes = resourcesConsulClient.getConnectionTypesOfRemoteAccessService(
                consulCredential,
                serviceId
        );
        List<CredentialClass> credentialClasses = resourcesConsulClient.getCredentialClassesOfRemoteAccessService(
                consulCredential,
                serviceId
        );

        if (credentialClasses.size() > 0 && connectionTypes.size() > 0) {
            Credential credential = resourcesVaultClient.getCredentialOfRemoteAccessService(
                    jwtAuthenticationToken,
                    serviceId,
                    credentialClasses.get(0)
            );
            RemoteAccessService remoteAccessService = new RemoteAccessService(
                    optionalNodeService.get(),
                    connectionTypes.get(0),
                    credential
            );
            resource.setRemoteAccessService(remoteAccessService);
        }

//        List<Capability> capabilitiesOfResource = this.capabilitiesManager.getSingleHostCapabilitiesOfResource(
//                resource.getId(),
//                consulCredential
//        );

        List<SingleHostCapabilityService> singleHostCapabilityServices = this.capabilitiesManager.getSingleHostCapabilityServicesOfResourceById(
                resource.getId(),
                consulCredential
        );

        if (singleHostCapabilityServices.stream().anyMatch(shcs -> shcs.getCapability().isCluster())) {
            resource.setClusterMember(true);
        }
        resource.setCapabilityServices(singleHostCapabilityServices);

        return resource;
    }

    //region ADD/DELETE
    public BasicResource addExistingResource(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID resourceId,
            String assetId,
            String resourceHostname,
            String resourceIp,
            String firmwareVersion,
            DigitalNameplateV3 digitalNameplateV3
    ) throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, CapabilityNotFoundException, SSLException, JsonProcessingException {
        /// Create realm role in Keycloak for new resource
        var resourceKeycloakRoleName = "resource_" + resourceId;
        this.keycloakUtil.createRealmRoleAndAssignToUser(jwtAuthenticationToken, resourceKeycloakRoleName);

        var resource = new BasicResource(resourceId, resourceHostname, resourceIp);
        resource.setAssetId(assetId);
        resource.setFirmwareVersion(firmwareVersion);
        resource = this.resourcesConsulClient.addResource(resource);

        //Add Health Checks if Capabilities with Health Checks are available:
        var capabilities = this.capabilitiesManager.getCapabilities();
        for (var capability : capabilities) {
            if (capability.getHealthCheck() != null) {
                singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                        new ConsulCredential(),
                        capability,
                        resource.getId(),
                        CapabilityServiceStatus.READY,
                        false,
                        new HashMap<>()
                );
            }
        }

        this.aasHandler.createResourceAasAndSubmodels(resource, digitalNameplateV3);

        notificationServiceClient.postNotification(jwtAuthenticationToken, Category.RESOURCES, JobTarget.RESOURCE, JobGoal.CREATE);

        resourceMessageSender.sendResourceCreatedMessage(resource.getId(), resource.getAssetId());

        return resource;
    }

    public void deleteResource(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID resourceId
    ) throws ConsulLoginFailedException, JsonProcessingException {
        BasicResource resource = null;
        UUID remoteAccessServiceId = null;
        try {
            resource = this.getResourceWithCredentialsByRemoteAccessService(jwtAuthenticationToken, resourceId);
        } catch(ResourceNotFoundException e) {
            LOG.warn(e.getMessage());
            LOG.warn("Delete of resource failed because resource with id = '"+resourceId+"' not found");
        }

        if(resource.getRemoteAccessService() != null)
            remoteAccessServiceId = resource.getRemoteAccessService().getId();

        List<String> realmRoles = new ArrayList<>();
        realmRoles.add("resource_" + resourceId);
        if(remoteAccessServiceId != null)
            realmRoles.add("service_" + remoteAccessServiceId);

        this.keycloakUtil.deleteRealmRoles(jwtAuthenticationToken, realmRoles);
        this.resourcesConsulClient.deleteResource(new ConsulCredential(), resource);
        this.resourcesVaultClient.removeSecretsForResource(new VaultCredential(), resource);
        if(resource.getRemoteAccessService() != null)
            this.resourcesVaultClient.removeSecretsOfRemoteAccessService(
                    new VaultCredential(),
                    remoteAccessServiceId
            );

        notificationServiceClient.postNotification(jwtAuthenticationToken, Category.RESOURCES, JobTarget.RESOURCE, JobGoal.DELETE);
        publisher.publishEvent(new ResourceEvent(this, resourceId, ResourceEvent.Operation.DELETE));
    }
    //endregion

    public void setRemoteAccessOfResource(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID resourceId,
            String resourceUsername,
            String resourcePassword,
            ConnectionType connectionType,
            int connectionPort) throws ConsulLoginFailedException {
        var credential = new CredentialUsernamePassword(resourceUsername, resourcePassword);

        this.resourcesConsulClient.setResourceConnectionType(resourceId, connectionType);
        var remoteAccessService = this.resourcesConsulClient.addConnectionService(
                connectionType,
                connectionPort,
                resourceId,
                credential);

        var serviceKeycloakRoleName = "service_" + remoteAccessService.getId();
        this.keycloakUtil.createRealmRoleAndAssignToUser(jwtAuthenticationToken, serviceKeycloakRoleName);

        this.resourcesVaultClient.addSecretsForConnectionService(
                remoteAccessService
        );

    }

    public void setLocationOfResource(UUID resourceId, UUID locationId) throws ConsulLoginFailedException {
        var optionalLocation = locationJpaRepository.findById(locationId);

        this.resourcesConsulClient.setResourceLocation(resourceId, optionalLocation.get());
    }
}
