package org.eclipse.slm.platform_management.service.api.credentials;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.common.credentials.model.CredentialData;
import org.eclipse.slm.common.credentials.model.CredentialEntityLinkCreateDTO;
import org.eclipse.slm.common.credentials.model.CredentialReadDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

public interface CredentialManagementRestApi {

    @RequestMapping(value = "/{credentialId}", method = RequestMethod.GET)
    @Operation(summary = "Get credential by id")
    @ResponseBody ResponseEntity<CredentialReadDTO> getCredentialById(
            @PathVariable(name = "credentialId") UUID credentialId);

    @RequestMapping(value = "/{credentialId}/data", method = RequestMethod.GET)
    @Operation(summary = "Get credential data by id")
    @ResponseBody ResponseEntity<CredentialData> getCredentialDataById(
            @PathVariable(name = "credentialId") UUID credentialId,
            @RequestParam(name = "impersonatedGroupId") String impersonatedGroupId
            );

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @Operation(summary = "Create credential")
    @ResponseBody ResponseEntity<Void> createCredential(
            @RequestBody CredentialCreateRequest credentialCreateRequest);

    @RequestMapping(value = "/{credentialId}", method = RequestMethod.PUT)
    @Operation(summary = "Create or update credential")
    @ResponseBody ResponseEntity<Void> createOrUpdateCredential(
            @PathVariable(name = "credentialId")      UUID credentialId,
            @RequestBody CredentialCreateRequest credentialCreateRequest);

    @RequestMapping(value = "/{credentialId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete credential by id")
    @ResponseBody ResponseEntity<Void> deleteCredential(
            @PathVariable(name = "credentialId")      UUID credentialId);

    @RequestMapping(value = "/entities/{entityId}", method = RequestMethod.GET)
    @Operation(summary = "Get credentials of entity")
    @ResponseBody ResponseEntity<List<CredentialReadDTO>> getCredentialsOfEntity(
            @PathVariable(name = "entityId") String entityId,
            @RequestParam(value = "entityType") String entityType);

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get credentials of user")
    @ResponseBody ResponseEntity<List<CredentialReadDTO>> getCredentialsOfUser();

    @RequestMapping(value = "/{credentialId}/links", method = RequestMethod.POST)
    @Operation(summary = "Link existing credential to entity")
    @ResponseBody ResponseEntity<Void> linkCredentialToEntity(
            @PathVariable(name = "credentialId") UUID credentialId,
            @RequestBody CredentialEntityLinkCreateDTO entityLink);

    @RequestMapping(value = "/{credentialId}/links", method = RequestMethod.DELETE)
    @Operation(summary = "Delete credential entity link")
    @ResponseBody ResponseEntity<Void> deleteCredentialEntityLink(
            @PathVariable(name = "credentialId") UUID credentialId,
            @RequestParam(value = "entityType") String entityType,
            @RequestParam(value = "entityId") String entityId,
            @RequestParam(value = "deleteIfOrphaned", required = false, defaultValue = "false") boolean deleteIfOrphaned);

    @RequestMapping(value = "/{credentialId}/scopes", method = RequestMethod.POST)
    @Operation(summary = "Add scopes to credential")
    @ResponseBody ResponseEntity<Void> addCredentialScopes(
            @PathVariable(name = "credentialId") UUID credentialId,
            @RequestBody List<String> scopes);

    @RequestMapping(value = "/{credentialId}/scopes", method = RequestMethod.DELETE)
    @Operation(summary = "Remove scopes from credential")
    @ResponseBody ResponseEntity<Void> removeCredentialScopes(
            @PathVariable(name = "credentialId") UUID credentialId,
            @RequestBody List<String> scopes);

}
