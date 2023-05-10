package org.eclipse.slm.resource_management.service.rest.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakUtil;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.notification_service.model.Category;
import org.eclipse.slm.notification_service.model.JobGoal;
import org.eclipse.slm.notification_service.model.JobTarget;
import org.eclipse.slm.notification_service.service.client.NotificationServiceClient;
import org.eclipse.slm.resource_management.model.resource.*;
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
import org.eclipse.slm.resource_management.service.rest.utils.ConnectionTypeUtils;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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
            ApplicationEventPublisher publisher
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
    }

    public List<BasicResource> getResourcesWithCredentialsByRemoteAccessService(
            KeycloakPrincipal keycloakPrincipal
    ) throws ConsulLoginFailedException, JsonProcessingException, ResourceNotFoundException {
        ConsulCredential consulCredential = new ConsulCredential(keycloakPrincipal);
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
                                keycloakPrincipal,
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

    public BasicResource getResourceWithCredentialsByRemoteAccessService(
            KeycloakPrincipal keycloakPrincipal,
            UUID resourceId
    ) throws ConsulLoginFailedException, ResourceNotFoundException, JsonProcessingException {
        ConsulCredential consulCredential = new ConsulCredential(keycloakPrincipal);

        Optional<BasicResource> optionalResource = resourcesConsulClient.getResourceById(
                new ConsulCredential(keycloakPrincipal),
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
                    keycloakPrincipal,
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
    public void addExistingResource(
            KeycloakPrincipal keycloakPrincipal,
            BasicResource basicResource,
            ConnectionType connectionType,
            int connectionPort,
            CredentialUsernamePassword credentialUsernamePassword,
            Optional<UUID> optionalLocationId,
            Optional<UUID> optionalResourceBaseConfigurationId
    ) throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, CapabilityNotFoundException, SSLException, JsonProcessingException {
        this.addExistingResource(
                keycloakPrincipal,
                basicResource.getId(),
                basicResource.getHostname(),
                basicResource.getIp(),
                optionalLocationId,
                credentialUsernamePassword.getUsername(),
                credentialUsernamePassword.getPassword(),
                connectionType,
                connectionPort,
                optionalResourceBaseConfigurationId
        );
    }

    public BasicResource addExistingResource(
            KeycloakPrincipal keycloakPrincipal,
            UUID resourceId,
            String resourceHostname,
            String resourceIp,
            Optional<UUID> optionalLocationId,
            String resourceUsername,
            String resourcePassword,
            ConnectionType connectionType,
            int connectionPort,
            Optional<UUID> optionalResourceBaseConfigurationId
    ) throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, CapabilityNotFoundException, SSLException, JsonProcessingException {
        /// Create realm role in Keycloak for new resource
        var resourceKeycloakRoleName = "resource_" + resourceId;
        this.keycloakUtil.createRealmRoleAndAssignToUser(keycloakPrincipal, resourceKeycloakRoleName);

        boolean remoteAccessAvailable = (connectionType != null && ConnectionTypeUtils.isRemote(connectionType));
        CredentialUsernamePassword credential = null;
        if(remoteAccessAvailable)
            credential = new CredentialUsernamePassword(resourceUsername, resourcePassword);

        Optional<Location> optionalLocation;

        if(optionalLocationId.isPresent())
            optionalLocation = locationJpaRepository.findById(optionalLocationId.orElse(null));
        else
            optionalLocation = Optional.empty();

        var resource = this.resourcesConsulClient.addResource(
                resourceId,
                resourceHostname,
                resourceIp,
                connectionType,
                optionalLocation
        );

        if(remoteAccessAvailable) {
            RemoteAccessService remoteAccessService = this.resourcesConsulClient.addConnectionService(
                    connectionType,
                    connectionPort,
                    resource,
                    credential
            );

            var serviceKeycloakRoleName = "service_" + remoteAccessService.getId();
            this.keycloakUtil.createRealmRoleAndAssignToUser(keycloakPrincipal, serviceKeycloakRoleName);


            this.resourcesVaultClient.addSecretsForConnectionService(
                    keycloakPrincipal,
                    remoteAccessService
            );
        }

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

        if(optionalResourceBaseConfigurationId.isPresent()) {
            UUID baseConfigurationId = optionalResourceBaseConfigurationId.get();
            capabilitiesManager.installBaseConfigurationOnResource(
                    keycloakPrincipal,
                    resource.getId(),
                    baseConfigurationId
            );
        }

        notificationServiceClient.postNotification(keycloakPrincipal, Category.Resources, JobTarget.RESOURCE, JobGoal.CREATE);
        publisher.publishEvent(new ResourceEvent(this, resourceId, ResourceEvent.Operation.CREATE));
        return resource;

    }


    public void deleteResource(
            KeycloakPrincipal keycloakPrincipal,
            UUID resourceId
    ) throws ConsulLoginFailedException, JsonProcessingException {
        BasicResource resource = null;
        UUID remoteAccessServiceId = null;
        try {
            resource = this.getResourceWithCredentialsByRemoteAccessService(keycloakPrincipal, resourceId);
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

//        var resourceRoleName = "resource_" + resourceId;
        this.keycloakUtil.deleteRealmRoles(keycloakPrincipal, realmRoles);
        this.resourcesConsulClient.deleteResource(new ConsulCredential(), resource);
        this.resourcesVaultClient.removeSecretsForResource(new VaultCredential(), resource);
        if(resource.getRemoteAccessService() != null)
            this.resourcesVaultClient.removeSecretsOfRemoteAccessService(
                    new VaultCredential(),
                    remoteAccessServiceId
            );

        notificationServiceClient.postNotification(keycloakPrincipal, Category.Resources, JobTarget.RESOURCE, JobGoal.DELETE);
        publisher.publishEvent(new ResourceEvent(this, resourceId, ResourceEvent.Operation.DELETE));
    }
    //endregion
}
