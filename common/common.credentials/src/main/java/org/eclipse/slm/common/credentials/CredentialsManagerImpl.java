package org.eclipse.slm.common.credentials;

import org.eclipse.slm.common.credentials.exceptions.CredentialNotFoundException;
import org.eclipse.slm.common.credentials.exceptions.CredentialPermissionDeniedException;
import org.eclipse.slm.common.credentials.model.*;
import org.eclipse.slm.common.credentials.persistence.CredentialEntityLink;
import org.eclipse.slm.common.credentials.persistence.CredentialLinkJpaRepository;
import org.eclipse.slm.common.credentials.persistence.VaultCredentialRepository;
import org.eclipse.slm.common.vault.client.VaultClientFactory;
import org.eclipse.slm.common.vault.client.auth.VaultJwtAuthentication;
import org.eclipse.slm.common.vault.client.exceptions.VaultPermissionDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
public class CredentialsManagerImpl implements CredentialsManager {

    private final static Logger LOG = LoggerFactory.getLogger(CredentialsManagerImpl.class);

    public static final String USER_GROUP_ENTITY_TYPE = "USER_GROUP";

    private final VaultClientFactory vaultClientFactory;

    private final CredentialLinkJpaRepository credentialLinkJpaRepository;

    public CredentialsManagerImpl(VaultClientFactory vaultClientFactory,
                                  CredentialLinkJpaRepository credentialLinkJpaRepository) {
        this.vaultClientFactory = vaultClientFactory;
        this.credentialLinkJpaRepository = credentialLinkJpaRepository;
    }

    @Override
    public List<CredentialReadDTO> getAllCredentialsForUser(List<String> userGroups, String jwtAccessToken) {
        var userCredentials = new ArrayList<CredentialReadDTO>();
        for (var userGroup : userGroups) {
            var entityCredentialLinks = this.credentialLinkJpaRepository.findByEntityIdAndEntityType(userGroup, USER_GROUP_ENTITY_TYPE);

            for (var link : entityCredentialLinks) {
                var credentialId = link.getCredentialId();
                try {
                    var credentialReadDTO = this.getCredentialByIdForUser(credentialId, jwtAccessToken);
                    userCredentials.add(credentialReadDTO);
                } catch (CredentialNotFoundException | CredentialPermissionDeniedException e) {
                    // Skip credentials that cannot be accessed
                }
            }
        }
        return userCredentials;
    }

    @Override
    public CredentialReadDTO getCredentialByIdForUser(UUID credentialId, String jwtAccessToken)
            throws CredentialNotFoundException, CredentialPermissionDeniedException {
        try {
            var vaultAuthentication = new VaultJwtAuthentication(vaultClientFactory.getVaultUrl(), jwtAccessToken);
            var vaultCredentialRepository = new VaultCredentialRepository(vaultClientFactory.createClient(vaultAuthentication));
            var credential = vaultCredentialRepository.findCredential(credentialId).orElseThrow(() -> new CredentialNotFoundException(credentialId));

            var entityLinks = this.credentialLinkJpaRepository.findByCredentialId(credentialId);
            var entityLinksDTO = CredentialMapper.INSTANCE.toReadDTO(entityLinks);
            var credentialReadDTO = CredentialMapper.INSTANCE.toReadDTO(credential, entityLinksDTO);

            return credentialReadDTO;
        } catch (VaultPermissionDeniedException e) {
            throw new CredentialPermissionDeniedException("Access denied for credential with id '" + credentialId + "' or credential was not found", e);
        }
    }

    @Override
    public CredentialData getCredentialDataById(UUID credentialId, String impersonatedGroupId) throws CredentialNotFoundException, CredentialPermissionDeniedException {
        try {
            var vaultCredentialRepository = new VaultCredentialRepository(vaultClientFactory.createAdminClient());
            var credential = vaultCredentialRepository.findCredential(credentialId).orElseThrow(() -> new CredentialNotFoundException(credentialId));

            if (!vaultCredentialRepository.hasGroupReadAccessToCredential(credentialId, impersonatedGroupId)) {
                throw new CredentialPermissionDeniedException("Access denied for credential with id '" + credentialId + "' or credential was not found");
            }

            return credential.getData();
        } catch (VaultPermissionDeniedException e) {
            throw new CredentialPermissionDeniedException("Access denied for credential with id '" + credentialId + "' or credential was not found", e);
        }
    }

    @Override
    public List<CredentialReadDTO> getCredentialsOfEntityForUser(String entityId, String entityType, String jwt) {
        var entityCredentialLinks = this.credentialLinkJpaRepository.findByEntityIdAndEntityType(entityId.toString(), entityType);
        var entityCredentials = new ArrayList<CredentialReadDTO>();
        for (var link : entityCredentialLinks) {
            var credentialId = link.getCredentialId();
            try {
                var credentialReadDTO = this.getCredentialByIdForUser(credentialId, jwt);
                entityCredentials.add(credentialReadDTO);
            } catch (CredentialNotFoundException | CredentialPermissionDeniedException e) {
                // Skip credentials that cannot be accessed
            }
        }

        return entityCredentials;
    }

    @Override
    public void createCredential(Credential credential, List<CredentialEntityLinkCreateDTO> linkedEntities, String fullPathOwnerGroupId) {
        // Store credential in Vault
        var vaultCredentialRepository = new VaultCredentialRepository(vaultClientFactory.createAdminClient());
        vaultCredentialRepository.saveCredential(credential, fullPathOwnerGroupId);
        // Add implicit link to user entity if not already present
        var optionalUserEntityLink = linkedEntities.stream().filter(linkedEntity -> linkedEntity.getEntityType().equals(USER_GROUP_ENTITY_TYPE)).findFirst();
        linkedEntities = new ArrayList<>(linkedEntities); // Make sure list is mutable
        if (optionalUserEntityLink.isEmpty()) {
            var credentialUserLink = new CredentialEntityLinkCreateDTO(USER_GROUP_ENTITY_TYPE, fullPathOwnerGroupId);
            linkedEntities.add(credentialUserLink);
        }
        // Store entity links
        for (var linkedEntity : linkedEntities) {
            var credentialEntityLink = new CredentialEntityLink(null, credential.getId(), linkedEntity.getEntityType(), linkedEntity.getEntityId().toString());
            this.credentialLinkJpaRepository.save(credentialEntityLink);
        }
        LOG.info("Created credential with id '{}' for user group '{}' and linked it to entities: {}",
                credential.getId(), fullPathOwnerGroupId, linkedEntities);
    }

    @Override
    public void deleteCredential(UUID credentialId) {
        // Find all links for the credential
        var credentialEntityLinks = this.credentialLinkJpaRepository.findByCredentialId(credentialId);
        this.credentialLinkJpaRepository.deleteAll(credentialEntityLinks);
        // Delete credential from Vault
        var vaultCredentialRepository = new VaultCredentialRepository(vaultClientFactory.createAdminClient());
        vaultCredentialRepository.deleteCredential(credentialId);
        LOG.info("Deleted credential with id '{}'", credentialId);
    }

    @Override
    public void deleteCredentialForUser(UUID credentialId, String jwtAccessToken) {
        // Check if user has access to the credential
        this.getCredentialByIdForUser(credentialId, jwtAccessToken);
        // If no exception was thrown, the user has access and we can delete the credential
        this.deleteCredential(credentialId);
    }

    @Override
    public void deleteOrUnlinkCredentialOfEntity(UUID entityId, UUID credentialId) {
        var entityCredentialLinks = this.credentialLinkJpaRepository.findByCredentialId(credentialId);
        if (entityCredentialLinks.size() == 1) {
            // Only linked to this entity, delete credential
            this.deleteCredential(credentialId);
            LOG.info("Deleted credential with id '{}'", credentialId);
        } else {
            // Delete the links between the credential and the entity
            var linksToDelete = this.credentialLinkJpaRepository.findByEntityIdAndCredentialId(entityId.toString(), credentialId);
            this.credentialLinkJpaRepository.deleteAll(linksToDelete);
            LOG.info("Unlinked credential with id '{}' from entity '{}' of type '{}'", credentialId, entityId, linksToDelete.get(0).getEntityType());
        }
    }

    @Override
    public void deleteOrUnlinkCredentialsOfEntity(UUID entityId, String entityType) {
        var entityCredentialLinks = this.credentialLinkJpaRepository.findByEntityIdAndEntityType(entityId.toString(), entityType);
        for (var link : entityCredentialLinks) {
            var credentialId = link.getCredentialId();
            var allLinksForCredential = this.credentialLinkJpaRepository.findByCredentialId(credentialId);
            if (allLinksForCredential.size() <= 1) {
                // Only linked to this entity, delete credential
                this.deleteCredential(credentialId);
            }
            // Delete the link between the credential and the entity
            this.credentialLinkJpaRepository.delete(link);
            LOG.info("Unlinked credential with id '{}' from entity '{}' of type '{}'", credentialId, entityId, link.getEntityType());
        }
    }

    @Override
    public void linkCredentialToEntity(UUID credentialId, CredentialEntityLinkCreateDTO entityLink) {
        var existingLinks = this.credentialLinkJpaRepository.findByEntityIdAndEntityTypeAndCredentialId(
                entityLink.getEntityId(),
                entityLink.getEntityType(),
                credentialId);
        if (!existingLinks.isEmpty()) {
            LOG.info("Credential '{}' already linked to entity '{}' of type '{}'", credentialId, entityLink.getEntityId(), entityLink.getEntityType());
            return;
        }
        var credentialEntityLink = new CredentialEntityLink(null, credentialId, entityLink.getEntityType(), entityLink.getEntityId());
        this.credentialLinkJpaRepository.save(credentialEntityLink);
        LOG.info("Linked credential '{}' to entity '{}' of type '{}'", credentialId, entityLink.getEntityId(), entityLink.getEntityType());
    }

    @Override
    public void deleteCredentialEntityLink(UUID credentialId, String entityType, String entityId, boolean deleteIfOrphaned) {
        // Get credential to check if it exists and to have a copy before deletion
        var vaultCredentialRepository = new VaultCredentialRepository(vaultClientFactory.createAdminClient());
        var credential = vaultCredentialRepository.findCredential(credentialId)
                .orElseThrow(() -> new CredentialNotFoundException(credentialId));
        // Delete the link between the credential and the entity
        var links = this.credentialLinkJpaRepository.findByEntityIdAndEntityTypeAndCredentialId(entityId, entityType, credentialId);
        if (links.isEmpty()) {
            LOG.info("No credential link found for credential '{}' and entity '{}' of type '{}'", credentialId, entityId, entityType);
        } else {
            this.credentialLinkJpaRepository.deleteAll(links);
            LOG.info("Deleted credential link for credential '{}' and entity '{}' of type '{}'", credentialId, entityId, entityType);
        }

        if (!deleteIfOrphaned) {
            return;
        }

        // Check if credential is orphaned (no remaining links or only linked to user groups)
        var remainingLinks = this.credentialLinkJpaRepository.findByCredentialId(credentialId);
        var isOrphanedByLinks = remainingLinks.stream()
                .allMatch(link -> USER_GROUP_ENTITY_TYPE.equals(link.getEntityType()));
        // Check if credential has no scopes
        var isOrphanedByScopes = credential.getScopesRaw().isEmpty();
        // Delete credential if orphaned
        if (isOrphanedByLinks && isOrphanedByScopes) {
            this.deleteCredential(credentialId);
            LOG.info("Deleted orphaned credential with id '{}'", credentialId);
        }
    }

    @Override
    public void addCredentialScopes(UUID credentialId, List<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return;
        }
        var vaultCredentialRepository = new VaultCredentialRepository(vaultClientFactory.createAdminClient());
        var credential = vaultCredentialRepository.findCredential(credentialId)
                .orElseThrow(() -> new CredentialNotFoundException(credentialId));
        var updatedScopes = new LinkedHashSet<>(credential.getScopesRaw());
        updatedScopes.addAll(scopes);
        vaultCredentialRepository.updateCredentialScopes(
                credentialId,
                new ArrayList<>(updatedScopes));
        LOG.info("Added scopes {} to credential '{}'", scopes, credentialId);
    }

    @Override
    public void removeCredentialScopes(UUID credentialId, List<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return;
        }
        var vaultCredentialRepository = new VaultCredentialRepository(vaultClientFactory.createAdminClient());
        var credential = vaultCredentialRepository.findCredential(credentialId)
                .orElseThrow(() -> new CredentialNotFoundException(credentialId));
        var updatedScopes = new LinkedHashSet<>(credential.getScopesRaw());
        updatedScopes.removeAll(scopes);
        vaultCredentialRepository.updateCredentialScopes(credentialId, new ArrayList<>(updatedScopes));
        LOG.info("Removed scopes {} from credential '{}'", scopes, credentialId);
    }
}
