package org.eclipse.slm.resource_management.common.remote_access;

import org.eclipse.slm.common.credentials.model.CredentialDataType;
import org.eclipse.slm.common.credentials.model.CredentialDataUsernamePasswordReadDTO;
import org.eclipse.slm.common.credentials.model.CredentialEntityLinkCreateDTO;
import org.eclipse.slm.common.restclient.feign.FeignResponseException;
import org.eclipse.slm.resource_management.common.access.AccessControlObjectType;
import org.eclipse.slm.resource_management.common.access.AccessControlService;
import org.eclipse.slm.resource_management.common.access.UserContext;
import org.eclipse.slm.resource_management.common.credentials.ResourceCredentialEntityType;
import org.eclipse.slm.resource_management.common.credentials.ResourceCredentialScope;
import org.eclipse.slm.resource_management.common.credentials.ResourceCredentialsManager;
import org.eclipse.slm.resource_management.common.resources.ResourceUpdatedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RemoteAccessManagerImpl implements RemoteAccessManager {

    private static final String POLICY_PREFIX = "remote-access_";

    private final static Logger LOG = LoggerFactory.getLogger(RemoteAccessManagerImpl.class);

    private final RemoteAccessJpaRepository remoteAccessJpaRepository;
    private final AccessControlService accessControlService;
    private final ResourceCredentialsManager resourceCredentialsManager;
    private final JwtDecoder jwtDecoder;
    private final List<ResourceUpdatedListener> resourceUpdatedListeners = new ArrayList<>();

    public RemoteAccessManagerImpl(
            RemoteAccessJpaRepository remoteAccessJpaRepository,
            AccessControlService accessControlService,
            ResourceCredentialsManager resourceCredentialsManager,
            JwtDecoder jwtDecoder) {
        this.remoteAccessJpaRepository = remoteAccessJpaRepository;
        this.accessControlService = accessControlService;
        this.resourceCredentialsManager = resourceCredentialsManager;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public List<RemoteAccessDTOReadMinimal> getRemoteAccessesOfResource(UUID resourceId, String jwtAccessToken) {
        var userContext = UserContext.fromJwt(new JwtAuthenticationToken(jwtDecoder.decode(jwtAccessToken)));
        var accessibleIds = accessControlService.getAccessibleObjectIds(userContext, AccessControlObjectType.REMOTE_ACCESS);
        var entities = remoteAccessJpaRepository.findByResourceId(resourceId);
        return entities.stream()
                .filter(e -> accessibleIds.isEmpty() || accessibleIds.get().contains(e.getId()))
                .map(this::toMinimalDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UUID> getRemoteAccessIdsOfResource(UUID resourceId) {
        return remoteAccessJpaRepository.findByResourceId(resourceId).stream()
                .map(RemoteAccessEntity::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<UUID> getRemoteAccessIdsOfResource(UUID resourceId, String jwtAccessToken) {
        return getRemoteAccessesOfResource(resourceId, jwtAccessToken).stream()
                .map(RemoteAccessDTOReadMinimal::getId)
                .collect(Collectors.toList());
    }

    @Override
    public RemoteAccessDTOReadFull getRemoteAccessByIdOrThrow(UUID resourceId, UUID remoteAccessId, String jwtAccessToken)
            throws RemoteAccessRuntimeException {
        try {
            var entity = remoteAccessJpaRepository.findById(remoteAccessId)
                    .orElseThrow(() -> new RemoteAccessNotFoundException(remoteAccessId, resourceId));

            var credential = resourceCredentialsManager.getCredentialByIdForUser(entity.getCredentialId(), jwtAccessToken);
            var username = entity.getUsername() != null ? entity.getUsername() : "";
            if (credential.getData().getCredentialDataType().equals(CredentialDataType.USERNAME_PASSWORD)) {
                username = ((CredentialDataUsernamePasswordReadDTO) credential.getData()).getUsername();
            }

            return new RemoteAccessDTOReadFull(
                    entity.getId(),
                    credential,
                    username,
                    entity.getConnectionPort(),
                    entity.getConnectionType()
            );
        } catch (RemoteAccessNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteAccessRuntimeException(
                    "Error while retrieving remote access service '" + remoteAccessId + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteRemoteAccessById(UUID resourceId, UUID remoteAccessId, String jwtAccessToken,
                                       boolean deleteCredentialIfOrphaned) {
        try {
            var entity = remoteAccessJpaRepository.findById(remoteAccessId)
                    .orElseThrow(() -> new RemoteAccessNotFoundException(remoteAccessId, resourceId));

            try {
                var credential = resourceCredentialsManager.getCredentialByIdForUser(entity.getCredentialId(), jwtAccessToken);
                var hasOtherScopes = credential.getScopes().stream()
                        .anyMatch(scope -> !ResourceCredentialScope.REMOTE_ACCESS.equals(scope));

                resourceCredentialsManager.removeCredentialScopes(entity.getCredentialId(),
                        List.of(ResourceCredentialScope.REMOTE_ACCESS.name()), jwtAccessToken);
                resourceCredentialsManager.deleteCredentialEntityLink(
                        entity.getCredentialId(), ResourceCredentialEntityType.REMOTE_ACCESS,
                        remoteAccessId, false, jwtAccessToken);

                if (!hasOtherScopes) {
                    resourceCredentialsManager.deleteCredentialEntityLink(
                            entity.getCredentialId(), ResourceCredentialEntityType.RESOURCE,
                            resourceId, deleteCredentialIfOrphaned, jwtAccessToken);
                }
            } catch (FeignResponseException e) {
                LOG.warn("Failed to delete or unlink credential of remote access service '{}': {}", remoteAccessId, e.getMessage());
            }

            remoteAccessJpaRepository.deleteById(remoteAccessId);
            accessControlService.removeObjectFromAllPolicies(AccessControlObjectType.REMOTE_ACCESS, remoteAccessId);

            for (var listener : resourceUpdatedListeners) {
                listener.onResourceUpdated(resourceId, new UserContext(Set.of(), true));
            }
            LOG.info("Deleted remote access service '{}' of resource '{}'", remoteAccessId, resourceId);
        } catch (RemoteAccessNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteAccessRuntimeException("Error while deleting remote access service: " + e.getMessage(), e);
        }
    }

    @Override
    public RemoteAccessDTOReadFull addRemoteAccessForResource(UUID resourceId, RemoteAccessCreateDTO remoteAccess,
                                                               String jwtAccessToken) {
        try {
            var entity = new RemoteAccessEntity();
            entity.setResourceId(resourceId);
            entity.setConnectionType(remoteAccess.getConnectionType());
            entity.setCredentialId(remoteAccess.getCredentialId());
            entity.setUsername(remoteAccess.getUsername());
            entity.setConnectionPort(remoteAccess.getConnectionPort());
            remoteAccessJpaRepository.save(entity);

            accessControlService.createSingleObjectPolicy(
                    POLICY_PREFIX + entity.getId(),
                    remoteAccess.getFullPathOwnerGroupId(),
                    AccessControlObjectType.REMOTE_ACCESS,
                    entity.getId());

            var credentialEntityLinks = List.of(
                    new CredentialEntityLinkCreateDTO(
                            ResourceCredentialEntityType.RESOURCE.toString(), resourceId.toString()),
                    new CredentialEntityLinkCreateDTO(
                            ResourceCredentialEntityType.REMOTE_ACCESS.toString(), entity.getId().toString())
            );
            resourceCredentialsManager.addEntityLinksToCredential(
                    remoteAccess.getCredentialId(), credentialEntityLinks, jwtAccessToken);

            for (var listener : resourceUpdatedListeners) {
                listener.onResourceUpdated(resourceId, new UserContext(Set.of(), true));
            }
            LOG.info("Added remote access '{}' for resource '{}'", entity.getId(), resourceId);

            return getRemoteAccessByIdOrThrow(resourceId, entity.getId(), jwtAccessToken);
        } catch (Exception ex) {
            throw new RemoteAccessRuntimeException("Error while adding remote access service: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void registerResourceUpdatedListener(ResourceUpdatedListener resourceUpdatedListener) {
        this.resourceUpdatedListeners.add(resourceUpdatedListener);
    }

    private RemoteAccessDTOReadMinimal toMinimalDto(RemoteAccessEntity entity) {
        return new RemoteAccessDTOReadMinimal(
                entity.getId(),
                entity.getCredentialId(),
                entity.getUsername(),
                entity.getConnectionPort(),
                entity.getConnectionType()
        );
    }
}
