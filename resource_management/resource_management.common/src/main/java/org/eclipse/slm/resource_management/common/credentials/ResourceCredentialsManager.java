package org.eclipse.slm.resource_management.common.credentials;

import org.eclipse.slm.common.credentials.exceptions.CredentialRuntimeException;
import org.eclipse.slm.common.credentials.model.Credential;
import org.eclipse.slm.common.credentials.model.CredentialData;
import org.eclipse.slm.common.credentials.model.CredentialEntityLinkCreateDTO;
import org.eclipse.slm.common.restclient.feign.FeignResponseException;
import org.eclipse.slm.platform_management.service.api.credentials.CredentialCreateRequest;
import org.eclipse.slm.platform_management.service.client.PlatformManagementClient;
import org.eclipse.slm.platform_management.service.client.PlatformManagementClientFactory;
import org.eclipse.slm.platform_management.service.client.PlatformManagementCredentialsClient;
import org.springframework.security.authorization.ObservationAuthorizationManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ResourceCredentialsManager {

    private final PlatformManagementClient platformManagementAdminClient;

    private final PlatformManagementClientFactory platformManagementClientFactory;

    public ResourceCredentialsManager(PlatformManagementClientFactory platformManagementClientFactory) {
        this.platformManagementClientFactory = platformManagementClientFactory;
        this.platformManagementAdminClient = platformManagementClientFactory.createAdminClient();
    }

    public ResourceCredentialReadDTO getCredentialByIdForUser(UUID credentialId, String userAccessToken) {
        var platformManagementUserClient = platformManagementClientFactory.create(userAccessToken);

        try {
            var response = platformManagementUserClient.credentials().getCredentialById(credentialId);
            var credentialReadDTO = response.getBody();
                var resourceCredentialReadDTO = new ResourceCredentialReadDTO(credentialReadDTO.getId(), credentialReadDTO.getName(),
                        credentialReadDTO.getScopesRaw(), credentialReadDTO.getData());

            return resourceCredentialReadDTO;
        } catch (FeignResponseException e) {
            throw new CredentialRuntimeException("Error retrieving credential '" + credentialId + "': " + e.getMessage(), e);
        }
    }

    public List<ResourceCredentialReadDTO> getCredentialsOfResource(UUID resourceId) {
        try {
            var response = platformManagementAdminClient.credentials().getCredentialsOfEntity(resourceId.toString(), ResourceCredentialEntityType.RESOURCE.toString());
            var resourceCredentialReadDTOs = new ArrayList<ResourceCredentialReadDTO>();
            for (var credentialReadDTO : response.getBody()) {
                var resourceCredentialReadDTO = new ResourceCredentialReadDTO(credentialReadDTO.getId(), credentialReadDTO.getName(),
                        credentialReadDTO.getScopesRaw(), credentialReadDTO.getData());
                resourceCredentialReadDTOs.add(resourceCredentialReadDTO);
            }

            return resourceCredentialReadDTOs;
        } catch (FeignResponseException e) {
            throw new CredentialRuntimeException("Error retrieving credentials for resource '" + resourceId + "': " + e.getMessage(), e);
        }
    }

    public List<ResourceCredentialReadDTO> getCredentialsOfResourceForUser(UUID resourceId, String userAccessToken) {
        var platformManagementUserClient = platformManagementClientFactory.create(userAccessToken);
        try {
            var response = platformManagementUserClient.credentials().getCredentialsOfEntity(resourceId.toString(), ResourceCredentialEntityType.RESOURCE.toString());
            var resourceCredentialReadDTOs = new ArrayList<ResourceCredentialReadDTO>();
            for (var credentialReadDTO : response.getBody()) {
                var resourceCredentialReadDTO = new ResourceCredentialReadDTO(credentialReadDTO.getId(), credentialReadDTO.getName(),
                        credentialReadDTO.getScopesRaw(), credentialReadDTO.getData());
                resourceCredentialReadDTOs.add(resourceCredentialReadDTO);
            }

            return resourceCredentialReadDTOs;
        } catch (FeignResponseException e) {
            throw new CredentialRuntimeException("Error retrieving credentials for resource '" + resourceId + "': " + e.getMessage(), e);
        }
    }

    public CredentialData getCredentialDataByIdForImpersonatedUser(UUID credentialId, String impersonatedUserId) {
        try {
            var response = this.platformManagementAdminClient.credentials().getCredentialDataById(credentialId, "/users/" + impersonatedUserId);
            var credentialData = response.getBody();

            return credentialData;
        } catch (FeignResponseException e) {
            throw new CredentialRuntimeException("Error retrieving credential data for credential '" + credentialId + "': " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CredentialRuntimeException("Unexpected error retrieving credential data for credential '" + credentialId + "': " + e.getMessage(), e);
        }
    }

    public void createOrUpdateCredential(Credential credential, List<CredentialEntityLinkCreateDTO> credentialEntityLinks, String fullPathOwnerGroupId) {
        var credentialCreateRequest = new CredentialCreateRequest(credentialEntityLinks, credential, fullPathOwnerGroupId);
        var response = this.platformManagementAdminClient.credentials().createOrUpdateCredential(credential.getId(), credentialCreateRequest);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error creating credential: " + response.getStatusCode() + " - " + response.getBody());
        }
    }

    public void deleteOrUnlinkCredentialForResource(UUID credentialId, String userAccessToken) {
        var platformManagementUserClient = platformManagementClientFactory.create(userAccessToken);

        platformManagementUserClient.credentials().deleteCredential(credentialId);
    }

    public void addEntityLinksToCredential(UUID credentialId, List<CredentialEntityLinkCreateDTO> credentialEntityLinks, String userAccessToken) {
        var platformManagementUserClient = platformManagementClientFactory.create(userAccessToken);
        for (var credentialEntityLink : credentialEntityLinks) {
            platformManagementUserClient.credentials().linkCredentialToEntity(credentialId, credentialEntityLink);
        }

    }

    public void addCredentialScopes(UUID credentialId, List<String> scopes, String userAccessToken) {
        var platformManagementUserClient = platformManagementClientFactory.create(userAccessToken);
        platformManagementUserClient.credentials().addCredentialScopes(credentialId, scopes);
    }

    public void removeCredentialScopes(UUID credentialId, List<String> scopes, String userAccessToken) {
        var platformManagementUserClient = platformManagementClientFactory.create(userAccessToken);
        platformManagementUserClient.credentials().removeCredentialScopes(credentialId, scopes);
    }

    public void deleteCredentialEntityLink(UUID credentialId,
                                           ResourceCredentialEntityType entityType,
                                           UUID entityId,
                                           boolean deleteIfOrphaned,
                                           String userAccessToken) {
        var platformManagementUserClient = platformManagementClientFactory.create(userAccessToken);
        platformManagementUserClient.credentials().deleteCredentialEntityLink(
                credentialId,
                entityType.toString(),
                entityId.toString(),
                deleteIfOrphaned);
    }
}
