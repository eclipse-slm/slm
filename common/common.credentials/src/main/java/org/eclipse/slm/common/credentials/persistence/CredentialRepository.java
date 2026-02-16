package org.eclipse.slm.common.credentials.persistence;

import org.eclipse.slm.common.credentials.model.Credential;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing Credential entities.
 */
public interface CredentialRepository {

    /**
     * Save a Credential entity and give the defined owner group access.
     * @param credential            The Credential entity to save
     * @param fullPathOwnerGroupId  The full path owner group ID to grant access
     */
    void saveCredential(Credential credential, String fullPathOwnerGroupId);

    /**
     * Find a Credential by its UUID.
     * @param id The UUID of the Credential
     * @return An Optional containing the Credential if found, otherwise empty
     */
    Optional<Credential> findCredential(UUID id);

    /**
     * Find a Credential by its UUID or throw an exception if not found.
     * @param id The UUID of the Credential
     * @return The Credential entity
     */
    Credential findCredentialOrThrow(UUID id);

    /**
     * Delete a Credential by its UUID.
     * @param id The UUID of the Credential to delete
     */
    void deleteCredential(UUID id);

    /**
     * Update the scopes of a Credential.
     * @param credentialId The UUID of the Credential
     * @param scopes The list of scopes to set
     */
    void updateCredentialScopes(UUID credentialId, java.util.List<String> scopes);

}