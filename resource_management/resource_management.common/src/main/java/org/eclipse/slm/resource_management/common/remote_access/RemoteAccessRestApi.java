package org.eclipse.slm.resource_management.common.remote_access;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

public interface RemoteAccessRestApi {

    @RequestMapping(value = "/{resourceId}/remote-access/{remoteAccessId}", method = RequestMethod.GET)
    @Operation(summary = "Get remote access of resource by id")
    ResponseEntity<RemoteAccessDTOReadFull> getRemoteAccessOfResourceById(
            @Parameter(description = "Unique resource identifier", required = true)
            @PathVariable(name = "resourceId") UUID resourceId,
            @Parameter(description = "Unique remote access identifier", required = true)
            @PathVariable(name = "remoteAccessId") UUID remoteAccessId);

    @RequestMapping(value = "/{resourceId}/remote-access", method = RequestMethod.POST)
    @Operation(summary = "Add remote access of resource with id")
    ResponseEntity<RemoteAccessDTOReadFull> addRemoteAccessForResource(
            @Parameter(description = "Unique resource identifier", required = true)
            @PathVariable(name = "resourceId") UUID resourceId,
            @Parameter(description = "Definition of the remote access to create", required = true)
            @RequestBody RemoteAccessCreateDTO remoteAccessCreateDTO);

    @RequestMapping(value = "/{resourceId}/remote-access/{remoteAccessId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete remote access of resource by id")
    ResponseEntity<Void> deleteRemoteAccessOfResourceById(
            @Parameter(description = "Unique resource identifier", required = true)
            @PathVariable(name = "resourceId") UUID resourceId,
            @Parameter(description = "Unique remote access identifier", required = true)
            @PathVariable(name = "remoteAccessId") UUID remoteAccessId,
            @Parameter(description = "If true, delete the credential if it becomes orphaned after the remote access has been deleted", example = "false")
            @RequestParam(name = "deleteCredentialIfOrphaned", required = false, defaultValue = "false") boolean deleteCredentialIfOrphaned);

    @RequestMapping(value = "/connection-types", method = RequestMethod.GET)
    @Operation(summary = "Get available remote connection types")
    List<ConnectionTypeDTO> getRemoteConnectionTypes();
}
