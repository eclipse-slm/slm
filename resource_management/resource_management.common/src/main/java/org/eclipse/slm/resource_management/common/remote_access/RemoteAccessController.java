package org.eclipse.slm.resource_management.common.remote_access;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resources")
@Tag(name = "Resources")
public class RemoteAccessController {

    private final static Logger LOG = LoggerFactory.getLogger(RemoteAccessController.class);

    private final RemoteAccessManager remoteAccessManager;

    @Autowired
    public RemoteAccessController(RemoteAccessManager remoteAccessManager) {
        this.remoteAccessManager = remoteAccessManager;
    }

    @RequestMapping(value = "/{resourceId}/remote-access/{remoteAccessId}", method = RequestMethod.GET)
    @Operation(summary = "Get remote access of resource by id")
    public @ResponseBody ResponseEntity<RemoteAccessDTO> getRemoteAccessOfResourceById(
            @PathVariable(name = "resourceId")      UUID resourceId,
            @PathVariable(name = "remoteAccessId")  UUID remoteAccessId) {

        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var remoteAccessDto = remoteAccessManager.getRemoteAccessService(resourceId, remoteAccessId, jwtAuthenticationToken);

        return ResponseEntity.ok(remoteAccessDto);
    }

    @RequestMapping(value = "/{resourceId}/remote-access", method = RequestMethod.PUT)
    @Operation(summary = "Set remote access of resource with id")
    public @ResponseBody ResponseEntity<RemoteAccessDTO> setRemoteAccessOfResource(
        @PathVariable(name = "resourceId")  UUID resourceId,
        @RequestParam(name = "resourceUsername", required = false)                              String resourceUsername,
        @RequestParam(name = "resourcePassword", required = false)                              String resourcePassword,
        @RequestParam(name = "resourceConnectionType", required = false)                        ConnectionType connectionType,
        @RequestParam(name = "resourceConnectionPort", required = false, defaultValue = "0")    int connectionPort) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        var ownerUserId = jwtAuthenticationToken.getToken().getSubject();
        var remoteAccessDto = this.remoteAccessManager.addUsernamePasswordRemoteAccessService(ownerUserId, resourceId, connectionType, connectionPort, resourceUsername, resourcePassword);

        return ResponseEntity.ok(remoteAccessDto);
    }

    @RequestMapping(value = "/{resourceId}/remote-access/{remoteAccessId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete remote access of resource by id")
    public @ResponseBody ResponseEntity<RemoteAccessDTO> deleteRemoteAccessOfResourceById(
            @PathVariable(name = "resourceId")      UUID resourceId,
            @PathVariable(name = "remoteAccessId")  UUID remoteAccessId) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        remoteAccessManager.deleteRemoteAccess(resourceId, remoteAccessId);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/connection-types", method = RequestMethod.GET)
    @Operation(summary="Get available remote connection types")
    public List<ConnectionTypeDTO> getRemoteConnectionTypes() {
        return ConnectionTypeUtils.getRemoteConnectionTypeDTOs();
    }
}
