package org.eclipse.slm.resource_management.common.remote_access;

import org.eclipse.slm.resource_management.common.resources.ResourceUpdatedListener;

import java.util.List;
import java.util.UUID;

public interface RemoteAccessManager {

    List<RemoteAccessDTOReadMinimal> getRemoteAccessesOfResource(UUID resourceId, String jwtAccessToken);

    List<UUID> getRemoteAccessIdsOfResource(UUID resourceId, String jwtAccessToken);

    RemoteAccessDTOReadFull getRemoteAccessByIdOrThrow(UUID resourceId, UUID remoteAccessId, String jwtAccessToken) throws RemoteAccessRuntimeException;

    void deleteRemoteAccessById(UUID resourceId, UUID remoteAccessId, String jwtAccessToken, boolean deleteCredentialIfOrphaned);

    RemoteAccessDTOReadFull addRemoteAccessForResource(
            UUID resourceId,
            RemoteAccessCreateDTO remoteAccess,
            String userAccessToken
    );

    void registerResourceUpdatedListener(ResourceUpdatedListener resourceUpdatedListener);

}
