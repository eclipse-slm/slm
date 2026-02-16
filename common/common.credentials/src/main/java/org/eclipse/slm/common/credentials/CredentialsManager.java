package org.eclipse.slm.common.credentials;
import org.eclipse.slm.common.credentials.exceptions.CredentialNotFoundException;
import org.eclipse.slm.common.credentials.model.Credential;
import org.eclipse.slm.common.credentials.model.CredentialEntityLinkCreateDTO;
import org.eclipse.slm.common.credentials.model.CredentialReadDTO;

import java.util.List;
import java.util.UUID;

public interface CredentialsManager {

    /**
     * Retrieve all credentials for the current user authenticated by the given JWT token.
     *
     * @param userGroups The user groups list of the current user.
     * @param jwt The JWT token of the current user.
     * @return A list of CredentialReadDTOs representing all credentials for the current user.
     */
    List<CredentialReadDTO> getAllCredentialsForCurrentUser(List<String> userGroups, String jwt);

    /**
     * Retrieve a specific credential by its ID for the current user authenticated by the given JWT token.
     *
     * @param credentialId The UUID of the credential to retrieve.
     * @param jwt The JWT token of the current user.
     * @return The CredentialReadDTO representing the requested credential.
     */
    CredentialReadDTO getCredentialByIdForCurrentUser(UUID credentialId, String jwt) throws CredentialNotFoundException;


    /**
     * Retrieve all credentials linked to a specific entity for the current user authenticated by the given JWT token.
     *
     * @param entityId The ID of the entity whose credentials should be retrieved.
     * @param entityType The type of the entity whose credentials should be retrieved.
     * @param jwt The JWT token of the current user.
     * @return A list of CredentialReadDTOs representing the credentials linked to the specified entity.
     */
    List<CredentialReadDTO> getCredentialsOfEntityForCurrentUser(String entityId, String entityType, String jwt);

    /**
     * Create a new credential and link it to a specific entity.
     *
     * @param credential The Credential object to create.
     * @param credentialEntityLinks The list of entity links to associate with the credential.
     * @param fullPathOwnerGroupId The full path owner group ID for the credential.
     */
    void createCredential(Credential credential, List<CredentialEntityLinkCreateDTO> credentialEntityLinks, String fullPathOwnerGroupId);

    /**
     * Delete a credential by its ID.
     *
     * @param credentialId The UUID of the credential to delete.
     */
    void deleteCredential(UUID credentialId);

    /** Delete a credential by its ID for the user authenticated by the given access token.
     *
     * @param credentialId The UUID of the credential to delete.
     * @param accessToken The access token of the current user.
     */
    void deleteCredentialForUser(UUID credentialId, String accessToken);

    /**
     * Delete or unlink a specific credential associated with a specific entity. If the credential is linked to other entities, it will be unlinked from the
     * specified entity. If it is not linked to other entities, it will be deleted.
     * @param entityId
     * @param credentialId
     */
    void deleteOrUnlinkCredentialOfEntity(UUID entityId, UUID credentialId);

    /**
     * Delete or unlink all credentials associated with a specific entity. If a credentials is linked to other entities, it will be unlinked. If not it is
     * not linked to other entities, it will be deleted.
     *
     * @param entityId The UUID of the entity whose credentials should be deleted or unlinked.
     * @param entityType The type of the entity whose credentials should be deleted or unlinked.
     */
    void deleteOrUnlinkCredentialsOfEntity(UUID entityId, String entityType);

    /**
     * Link an existing credential to a specific entity.
     *
     * @param credentialId The UUID of the credential to link.
     * @param entityLink The entity link to create.
     */
    void linkCredentialToEntity(UUID credentialId, CredentialEntityLinkCreateDTO entityLink);

    /**
     * Delete a specific entity link of an existing credential and optionally delete the credential
     * if it becomes orphaned (only USER_GROUP links remain).
     *
     * @param credentialId The UUID of the credential to unlink.
     * @param entityType The type of the entity to unlink.
     * @param entityId The ID of the entity to unlink.
     * @param deleteIfOrphaned Whether to delete the credential when only USER_GROUP links remain.
     */
    void deleteCredentialEntityLink(UUID credentialId, String entityType, String entityId, boolean deleteIfOrphaned);

    /**
     * Add scopes to an existing credential.
     *
     * @param credentialId The UUID of the credential.
     * @param scopes The scopes to add.
     */
    void addCredentialScopes(UUID credentialId, List<String> scopes);

    /**
     * Remove scopes from an existing credential.
     *
     * @param credentialId The UUID of the credential.
     * @param scopes The scopes to remove.
     */
    void removeCredentialScopes(UUID credentialId, List<String> scopes);
}
