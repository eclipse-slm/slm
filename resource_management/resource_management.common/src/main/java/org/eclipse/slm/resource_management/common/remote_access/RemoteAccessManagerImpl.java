package org.eclipse.slm.resource_management.common.remote_access;

import org.eclipse.slm.common.credentials.model.CredentialDataType;
import org.eclipse.slm.common.credentials.model.CredentialDataUsernamePasswordReadDTO;
import org.eclipse.slm.common.credentials.model.CredentialEntityLinkCreateDTO;
import org.eclipse.slm.common.restclient.feign.FeignResponseException;
import org.eclipse.slm.resource_management.common.adapters.RemoteAccessConsulClient;
import org.eclipse.slm.resource_management.common.adapters.RemoteAccessConsulClientFactory;
import org.eclipse.slm.resource_management.common.credentials.ResourceCredentialEntityType;
import org.eclipse.slm.resource_management.common.credentials.ResourceCredentialScope;
import org.eclipse.slm.resource_management.common.credentials.ResourceCredentialsManager;
import org.eclipse.slm.resource_management.common.resources.ResourceUpdatedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RemoteAccessManagerImpl implements RemoteAccessManager {

    private final static Logger LOG = LoggerFactory.getLogger(RemoteAccessManagerImpl.class);

    private final RemoteAccessConsulClientFactory remoteAccessConsulClientFactory;
    private final RemoteAccessConsulClient remoteAccessConsulAdminClient;

    private final ResourceCredentialsManager resourceCredentialsManager;

    private final List<ResourceUpdatedListener> resourceUpdatedListeners = new ArrayList<>();

    public RemoteAccessManagerImpl(RemoteAccessConsulClientFactory remoteAccessConsulClientFactory,
                                   ResourceCredentialsManager resourceCredentialsManager) {
        this.remoteAccessConsulClientFactory = remoteAccessConsulClientFactory;
        this.remoteAccessConsulAdminClient = remoteAccessConsulClientFactory.createAdminClient();
        this.resourceCredentialsManager = resourceCredentialsManager;
    }

    @Override
    public List<RemoteAccessDTOReadMinimal> getRemoteAccessesOfResource(UUID resourceId, String jwtAccessToken) {
        try {
            var remoteAccessConsulClient = this.remoteAccessConsulClientFactory.create(jwtAccessToken);
            var remoteAccesses = remoteAccessConsulClient.getRemoteAccesses(resourceId);

            return remoteAccesses;

        } catch (Exception e) {
            throw new RemoteAccessRuntimeException("Error while retrieving remote access services of resource '" + resourceId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<UUID> getRemoteAccessIdsOfResource(UUID resourceId, String jwtAccessToken) {
        var remoteAccessServiceIds = new ArrayList<UUID>();

        try {
            var remoteAccesses = this.getRemoteAccessesOfResource(resourceId, jwtAccessToken);

            for (var remoteAccess : remoteAccesses) {
                remoteAccessServiceIds.add(remoteAccess.getId());
            }

        } catch (Exception e) {
            throw new RemoteAccessRuntimeException("Error while retrieving remote access services of resource '" + resourceId + "': " + e.getMessage(), e);
        }

        return remoteAccessServiceIds;
    }

    @Override
    public RemoteAccessDTOReadFull getRemoteAccessByIdOrThrow(UUID resourceId, UUID remoteAccessId, String jwtAccessToken)
            throws RemoteAccessRuntimeException {
        try {
            var remoteAccessConsulClient = this.remoteAccessConsulClientFactory.create(jwtAccessToken);
            var remoteAccessOptional = remoteAccessConsulClient.getRemoteAccessById(resourceId, remoteAccessId);

            if (remoteAccessOptional.isEmpty()) {
                throw new RemoteAccessNotFoundException(remoteAccessId, resourceId);
            }
            var remoteAccessDTOReadMinimal = remoteAccessOptional.get();

            var remoteAccessCredential = this.resourceCredentialsManager.getCredentialByIdForCurrentUser(remoteAccessDTOReadMinimal.getCredentialId(), jwtAccessToken);
            var username = remoteAccessDTOReadMinimal.getUsername();
            if (remoteAccessCredential.getData().getCredentialDataType().equals(CredentialDataType.USERNAME_PASSWORD)) {
                username = ((CredentialDataUsernamePasswordReadDTO) remoteAccessCredential.getData()).getUsername();
            }

            var remoteAccessDTOReadFull = new RemoteAccessDTOReadFull(
                    remoteAccessDTOReadMinimal.getId(),
                    remoteAccessCredential,
                    username,
                    remoteAccessDTOReadMinimal.getConnectionPort(),
                    remoteAccessDTOReadMinimal.getConnectionType()
            );

            return remoteAccessDTOReadFull;
        } catch (Exception e) {
            throw new RemoteAccessRuntimeException("Error while retrieving remote access services of resource '" + resourceId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteRemoteAccessById(UUID resourceId, UUID remoteAccessId, String jwtAccessToken, boolean deleteCredentialIfOrphaned) {
        try {
            var remoteAccess = this.getRemoteAccessByIdOrThrow(resourceId, remoteAccessId, jwtAccessToken);
            try {
                var credential = this.resourceCredentialsManager.getCredentialByIdForCurrentUser(remoteAccess.getCredential().getId(), jwtAccessToken);
                var hasOtherScopes = credential.getScopes().stream().anyMatch(scope -> !ResourceCredentialScope.REMOTE_ACCESS.equals(scope));

                this.resourceCredentialsManager.removeCredentialScopes(remoteAccess.getCredential().getId(),
                        List.of(ResourceCredentialScope.REMOTE_ACCESS.name()),
                        jwtAccessToken);
                this.resourceCredentialsManager.deleteCredentialEntityLink(
                        remoteAccess.getCredential().getId(),
                        ResourceCredentialEntityType.REMOTE_ACCESS,
                        remoteAccessId,
                        false,
                        jwtAccessToken);

                if (!hasOtherScopes) {
                    this.resourceCredentialsManager.deleteCredentialEntityLink(
                            remoteAccess.getCredential().getId(),
                            ResourceCredentialEntityType.RESOURCE,
                            resourceId,
                            deleteCredentialIfOrphaned,
                            jwtAccessToken);
                }
            } catch (FeignResponseException e) {
                LOG.warn("Failed to delete or unlink credential of remote access service '{}': {}", remoteAccessId, e.getMessage());
            }
            this.remoteAccessConsulAdminClient.removeRemoteAccess(resourceId, remoteAccessId);
            for (var listener : this.resourceUpdatedListeners) {
                listener.onResourceUpdated(resourceId, jwtAccessToken);
            }
            LOG.info("Deleted remote access service '{}' of resource '{}'", remoteAccessId, resourceId);
        } catch (Exception e) {
            if (e instanceof RemoteAccessNotFoundException) {
                throw (RemoteAccessNotFoundException) e;
            }
            throw new RemoteAccessRuntimeException("Error while deleting remote access service: " + e.getMessage(), e);
        }
    }

    @Override
    public RemoteAccessDTOReadFull addRemoteAccessForResource(UUID resourceId, RemoteAccessCreateDTO remoteAccess, String jwtAccessToken) {
        try {
            var remoteAccessCreated = this.remoteAccessConsulAdminClient.addRemoteAccess(remoteAccess, resourceId);
            var credentialEntityLinks = List.of(
                    new CredentialEntityLinkCreateDTO(ResourceCredentialEntityType.RESOURCE.toString(), resourceId.toString()),
                    new CredentialEntityLinkCreateDTO(ResourceCredentialEntityType.REMOTE_ACCESS.toString(), remoteAccessCreated.getId().toString())
            );
            this.resourceCredentialsManager.addEntityLinksToCredential(remoteAccess.getCredentialId(), credentialEntityLinks, jwtAccessToken);

            for (var listener : this.resourceUpdatedListeners) {
                listener.onResourceUpdated(resourceId,jwtAccessToken);
            }

            LOG.info("Added remote access '{}' for resource '{}'", remoteAccessCreated.getId(), resourceId);
            var remoteAccessCreatedFull = this.getRemoteAccessByIdOrThrow(resourceId, remoteAccessCreated.getId(), jwtAccessToken);

            return remoteAccessCreatedFull;
        } catch (Exception ex) {
            throw new RemoteAccessRuntimeException("Error while adding remote access service: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void registerResourceUpdatedListener(ResourceUpdatedListener resourceUpdatedListener) {
        this.resourceUpdatedListeners.add(resourceUpdatedListener);
    }
}
