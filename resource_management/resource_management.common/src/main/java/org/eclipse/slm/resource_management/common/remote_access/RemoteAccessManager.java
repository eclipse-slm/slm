package org.eclipse.slm.resource_management.common.remote_access;

import org.eclipse.slm.resource_management.common.resources.ResourceUpdatedListener;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.UUID;

public interface RemoteAccessManager {

    List<UUID> getRemoteAccessServiceIdsOfResource(UUID resourceId);

    RemoteAccessDTO getRemoteAccessService(UUID resourceId, UUID remoteAccessId, JwtAuthenticationToken jwtAuthenticationToken);

    void deleteRemoteAccess(UUID resourceId, UUID remoteAccessId);

    RemoteAccessDTO addUsernamePasswordRemoteAccessService(
            String ownerUserId,
            UUID resourceId,
            ConnectionType connectionType,
            int connectionPort,
            String username,
            String password
    );

    void registerResourceUpdatedListener(ResourceUpdatedListener resourceUpdatedListener);

}
