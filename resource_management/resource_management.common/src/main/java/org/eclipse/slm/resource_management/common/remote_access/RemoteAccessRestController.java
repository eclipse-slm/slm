package org.eclipse.slm.resource_management.common.remote_access;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.eclipse.slm.common.restserver.annotations.AuthorizedAsSlmUser;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(RemoteAccessRestApiConfig.BASE_PATH)
@Tag(name = RemoteAccessRestApiConfig.TAG)
public class RemoteAccessRestController implements RemoteAccessRestApi {

    private final static Logger LOG = LoggerFactory.getLogger(RemoteAccessRestController.class);

    private final RemoteAccessManager remoteAccessManager;

    @Autowired
    public RemoteAccessRestController(RemoteAccessManager remoteAccessManager) {
        this.remoteAccessManager = remoteAccessManager;
    }

    @Override
    public ResponseEntity<RemoteAccessDTOReadFull> getRemoteAccessOfResourceById(
            UUID resourceId,
            UUID remoteAccessId) {

        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);

        var remoteAccessDto = remoteAccessManager.getRemoteAccessByIdOrThrow(resourceId, remoteAccessId, accessToken);

        return ResponseEntity.ok(remoteAccessDto);
    }

    @Override
    public ResponseEntity<RemoteAccessDTOReadFull> addRemoteAccessForResource(
        UUID resourceId,
        RemoteAccessCreateDTO remoteAccessCreateDTO) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);

        var remoteAccessDto = this.remoteAccessManager.addRemoteAccessForResource(resourceId, remoteAccessCreateDTO, userAccessToken);

        return ResponseEntity.ok(remoteAccessDto);
    }

    @Override
    public ResponseEntity<Void> deleteRemoteAccessOfResourceById(
            UUID resourceId,
            UUID remoteAccessId,
            boolean deleteCredentialIfOrphaned) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userAccessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
        remoteAccessManager.deleteRemoteAccessById(resourceId, remoteAccessId, userAccessToken, deleteCredentialIfOrphaned);

        return ResponseEntity.ok().build();
    }

    @Override
    public List<ConnectionTypeDTO> getRemoteConnectionTypes() {
        var connectionTypeDTOs = new ArrayList<ConnectionTypeDTO>();
        Arrays.stream(ConnectionType.values())
            .filter(ct -> ct.getRemoteAccess())
            .forEach(t -> connectionTypeDTOs.add(new ConnectionTypeDTO(t)));

        return connectionTypeDTOs;
    }
}
