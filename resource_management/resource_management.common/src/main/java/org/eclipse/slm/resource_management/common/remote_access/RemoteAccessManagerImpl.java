package org.eclipse.slm.resource_management.common.remote_access;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.adapters.ResourcesVaultClient;
import org.eclipse.slm.resource_management.common.resources.ResourceUpdatedListener;
import org.eclipse.slm.resource_management.common.resources.ResourcesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RemoteAccessManagerImpl implements RemoteAccessManager {

    private final static Logger LOG = LoggerFactory.getLogger(RemoteAccessManagerImpl.class);

    private final KeycloakAdminClient keycloakAdminClient;

    private final ResourcesConsulClient resourcesConsulClient;

    private final ResourcesVaultClient resourcesVaultClient;

    private final List<ResourceUpdatedListener> resourceUpdatedListeners = new ArrayList<>();

    public RemoteAccessManagerImpl(KeycloakAdminClient keycloakAdminClient,
                                   ResourcesConsulClient resourcesConsulClient,
                                   ResourcesVaultClient resourcesVaultClient) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.resourcesConsulClient = resourcesConsulClient;
        this.resourcesVaultClient = resourcesVaultClient;
    }

    public List<UUID> getRemoteAccessServiceIdsOfResource(UUID resourceId) {
        var remoteAccessServiceIds = new ArrayList<UUID>();

        try {
            var consulCredential = new ConsulCredential();

            Optional<NodeService> optionalNodeService = resourcesConsulClient.getRemoteAccessServiceOfResourceAsNodeService(
                    consulCredential,
                    resourceId
            );
            if (optionalNodeService.isEmpty())
                return remoteAccessServiceIds;

            var serviceId = UUID.fromString(optionalNodeService.get().getID());
            remoteAccessServiceIds.add(serviceId);

        } catch (Exception e) {
            LOG.error("Error while retrieving remote access services of resource '{}': {}", resourceId, e.getMessage(), e);
            throw new RemoteAccessRuntimeException("Error while retrieving remote access services of resource '" + resourceId + "': " + e.getMessage());
        }

        return remoteAccessServiceIds;
    }

    public RemoteAccessDTO getRemoteAccessService(UUID resourceId, UUID remoteAccessId, JwtAuthenticationToken jwtAuthenticationToken) {
        try {
            var consulCredential = new ConsulCredential(jwtAuthenticationToken);

            Optional<NodeService> optionalNodeService = resourcesConsulClient.getRemoteAccessServiceOfResourceAsNodeService(
                    consulCredential,
                    resourceId
            );
            if (optionalNodeService.isEmpty())
                throw new RemoteAccessNotFoundException("Remote access service '" + remoteAccessId + "' not found for resource: " + resourceId);

            var serviceId = UUID.fromString(optionalNodeService.get().getID());

            if (!serviceId.equals(remoteAccessId)) {
                throw new RemoteAccessNotFoundException("Remote access service '" + remoteAccessId + "' not found for resource: " + resourceId);
            }

            var connectionTypes = resourcesConsulClient.getConnectionTypesOfRemoteAccessService(
                    consulCredential,
                    serviceId
            );
            var credentialClasses = resourcesConsulClient.getCredentialClassesOfRemoteAccessService(
                    consulCredential,
                    serviceId
            );

            if (credentialClasses.size() > 0 && connectionTypes.size() > 0) {
                Credential credential = resourcesVaultClient.getCredentialOfRemoteAccessService(
                        jwtAuthenticationToken,
                        resourceId,
                        remoteAccessId,
                        credentialClasses.get(0)
                );
                var remoteAccessConsulService = new RemoteAccessConsulService(
                        optionalNodeService.get(),
                        connectionTypes.get(0),
                        credential
                );

                var remoteAccessDTO = RemoteAccessMapper.INSTANCE.toDto(remoteAccessConsulService);

                return remoteAccessDTO;
            }
        } catch (Exception e) {
            throw new RemoteAccessRuntimeException("Error while retrieving remote access services of resource '" + resourceId + "': " + e.getMessage());
        }

        return null;
    }

    public void deleteRemoteAccess(UUID resourceId, UUID remoteAccessId) {
        try {
            this.resourcesVaultClient.removeSecretsOfRemoteAccessService(new VaultCredential(), resourceId, remoteAccessId);
            this.resourcesConsulClient.removeConnectionService(resourceId, remoteAccessId);
            for (var listener : this.resourceUpdatedListeners) {
                listener.onResourceUpdated(resourceId);
            }
            LOG.info("Deleted remote access service '{}' of resource '{}'", remoteAccessId, resourceId);
        } catch (Exception e) {
            LOG.error("Error while deleting remote access service: {}", e.getMessage(), e);
            throw new RemoteAccessRuntimeException("Error while deleting remote access service: " + e.getMessage());
        }
    }

    public RemoteAccessDTO addUsernamePasswordRemoteAccessService(
            String ownerUserId,
            UUID resourceId,
            ConnectionType connectionType,
            int connectionPort,
            String username,
            String password
    ) {
        try {
            var credential = new CredentialUsernamePassword(username, password);

            this.resourcesConsulClient.setResourceConnectionType(resourceId, connectionType);
            var remoteAccessService = this.resourcesConsulClient.addConnectionService(
                    connectionType,
                    connectionPort,
                    resourceId,
                    credential);

            this.resourcesVaultClient.addSecretsForConnectionService(resourceId, remoteAccessService);

            for (var listener : this.resourceUpdatedListeners) {
                listener.onResourceUpdated(resourceId);
            }

            LOG.info("Added remote access '{}' for resource '{}'", remoteAccessService.getId(), resourceId);

            return RemoteAccessMapper.INSTANCE.toDto(remoteAccessService);
        } catch (Exception ex) {
            LOG.error("Error while adding remote access service: {}", ex.getMessage(), ex);
            throw new RemoteAccessRuntimeException("Error while adding remote access service: " + ex.getMessage());
        }
    }

    public void registerResourceUpdatedListener(ResourceUpdatedListener resourceUpdatedListener) {
        this.resourceUpdatedListeners.add(resourceUpdatedListener);
    }
}
