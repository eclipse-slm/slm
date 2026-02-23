package org.eclipse.slm.common.credentials.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.credentials.exceptions.CredentialNotFoundException;
import org.eclipse.slm.common.credentials.exceptions.CredentialRuntimeException;
import org.eclipse.slm.common.credentials.model.*;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.exceptions.VaultGroupNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of CredentialRepository that uses Vault as the backend storage.
 */
public class VaultCredentialRepository implements CredentialRepository {

    private final Logger LOG = LoggerFactory.getLogger(VaultCredentialRepository.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final VaultClient vaultClient;

    public final static String VAULT_SECRET_ENGINE_NAME = "credentials";
    public static final String VAULT_CREDENTIAL_POLICY_PREFIX = "credential_";

    public static final String VAULT_METADATA_CREDENTIAL_ID_KEY = "credentialId";
    public static final String VAULT_METADATA_CREDENTIAL_NAME_KEY = "credentialName";
    public static final String VAULT_METADATA_CREDENTIAL_DATA_TYPE_KEY = "credentialDataType";
    public static final String VAULT_METADATA_CREDENTIAL_SCOPES_KEY = "credentialScopes";

    public static final String VAULT_SECRET_DATA_KEY_USERNAME = "username";
    public static final String VAULT_SECRET_DATA_KEY_PASSWORD = "password";
    public static final String VAULT_SECRET_DATA_KEY_PRIVATE_KEY = "privateKey";
    public static final String VAULT_SECRET_DATA_KEY_PUBLIC_KEY = "publicKey";

    /// Constructor for CredentialsVaultClient
    /// @param vaultClient The VaultClient to use for Vault operations
    public VaultCredentialRepository(VaultClient vaultClient) {
        this.vaultClient = vaultClient;
    }

    //region CredentialRepository
    @Override
    public void saveCredential(Credential credential, String fullPathOwnerGroupId) {
        try {
            var kvPath = credential.getId().toString();
            var secretData = new HashMap<String, String>();
            var metadata = new HashMap<String, String>();
            metadata.put(VaultCredentialRepository.VAULT_METADATA_CREDENTIAL_ID_KEY, credential.getId().toString());
            if (credential.getName() != null && !credential.getName().isBlank()) {
                metadata.put(VaultCredentialRepository.VAULT_METADATA_CREDENTIAL_NAME_KEY, credential.getName());
            }
            metadata.put(VaultCredentialRepository.VAULT_METADATA_CREDENTIAL_DATA_TYPE_KEY, credential.getData().getCredentialDataType().toString());
            metadata.put(VaultCredentialRepository.VAULT_METADATA_CREDENTIAL_SCOPES_KEY, this.objectMapper.writeValueAsString(credential.getScopesRaw()));

            switch (credential.getData().getCredentialDataType()) {
                case USERNAME_PASSWORD -> {
                    var credDataUP = (CredentialDataUsernamePassword) credential.getData();
                    secretData.put(VaultCredentialRepository.VAULT_SECRET_DATA_KEY_USERNAME, credDataUP.getUsername());
                    secretData.put(VaultCredentialRepository.VAULT_SECRET_DATA_KEY_PASSWORD, credDataUP.getPassword());
                }
                case KEY_PAIR -> {
                    var credDataKP = (CredentialDataKeyPair) credential.getData();
                    secretData.put(VaultCredentialRepository.VAULT_SECRET_DATA_KEY_PRIVATE_KEY, credDataKP.getPrivateKey());
                    secretData.put(VaultCredentialRepository.VAULT_SECRET_DATA_KEY_PUBLIC_KEY, credDataKP.getPublicKey());
                }
                default -> throw new IllegalArgumentException("Unknown credential type: " + credential.getData().getCredentialDataType());
            }

            this.vaultClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).addSecretsToKvEngine(kvPath, secretData);
            this.vaultClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).addMetadataToKvEngine(kvPath, metadata);

            var credentialPolicyName = VaultCredentialRepository.getCredentialPolicyNameById(credential.getId());
            var credentialPolicyRule = "path \"" + VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME + "/data/" + credential.getId() + "\" { capabilities = [\"read\", \"list\"] }";
            this.vaultClient.acl().createOrUpdatePolicy(credentialPolicyName, credentialPolicyRule);
            this.vaultClient.acl().addPolicyToGroup(fullPathOwnerGroupId, credentialPolicyName);
        } catch (Exception e) {
            throw new CredentialRuntimeException("Failed to store credential with id '" + credential.getId() + "' in Vault.", e);
        }
    }

    @Override
    public Optional<Credential> findCredential(UUID credentialId) {

        var kvPath = credentialId.toString();
        var optionalKevSecrets = this.vaultClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).getSecretsOfPath(kvPath);
        if (optionalKevSecrets.isEmpty()) {
            throw new CredentialNotFoundException(credentialId);
        }
        var kvSecrets = optionalKevSecrets.get();
        var metadata = kvSecrets.getMetadata().getCustomMetadata();
        if (!metadata.containsKey(VaultCredentialRepository.VAULT_METADATA_CREDENTIAL_SCOPES_KEY)
                || !metadata.containsKey(VaultCredentialRepository.VAULT_METADATA_CREDENTIAL_DATA_TYPE_KEY)) {
            throw new CredentialRuntimeException("Credential metadata is missing required keys.");
        }
        var credentialName = metadata.getOrDefault(VaultCredentialRepository.VAULT_METADATA_CREDENTIAL_NAME_KEY, "");
        var credentialType = CredentialDataType.valueOf(metadata.get(VaultCredentialRepository.VAULT_METADATA_CREDENTIAL_DATA_TYPE_KEY));
        List<String> credentialScopes;
        try {
            credentialScopes = this.objectMapper.readValue(
                    metadata.get(VaultCredentialRepository.VAULT_METADATA_CREDENTIAL_SCOPES_KEY),
                    new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new CredentialRuntimeException("Failed to parse credential scopes from metadata.", e);
        }

        CredentialData credentialData;
        switch (credentialType) {
            case USERNAME_PASSWORD -> {
                var data = kvSecrets.getData();
                if (!data.containsKey(VaultCredentialRepository.VAULT_SECRET_DATA_KEY_USERNAME)
                        || !data.containsKey(VaultCredentialRepository.VAULT_SECRET_DATA_KEY_PASSWORD)) {
                    throw new CredentialRuntimeException("Credential data is missing required keys for USERNAME_PASSWORD type.");
                }
                var username = data.get(VaultCredentialRepository.VAULT_SECRET_DATA_KEY_USERNAME);
                var password = data.get(VaultCredentialRepository.VAULT_SECRET_DATA_KEY_PASSWORD);
                credentialData = new CredentialDataUsernamePassword(username, password);

            }
            case KEY_PAIR -> {
                var data = kvSecrets.getData();
                if (!data.containsKey(VaultCredentialRepository.VAULT_SECRET_DATA_KEY_PRIVATE_KEY)
                        || !data.containsKey(VaultCredentialRepository.VAULT_SECRET_DATA_KEY_PUBLIC_KEY)) {
                    throw new CredentialRuntimeException("Credential data is missing required keys for KEY_PAIR type.");
                }
                var publicKey = data.get(VaultCredentialRepository.VAULT_SECRET_DATA_KEY_PUBLIC_KEY);
                var privateKey = data.get(VaultCredentialRepository.VAULT_SECRET_DATA_KEY_PRIVATE_KEY);
                credentialData = new CredentialDataKeyPair(privateKey, publicKey);
            }
            default -> throw new IllegalArgumentException("Unknown credential type: " + credentialType);
        }

        var credential = new Credential(credentialId, credentialName, credentialScopes, credentialData);
        return Optional.of(credential);
    }

    @Override
    public Credential findCredentialOrThrow(UUID credentialId) {
        return findCredential(credentialId).orElseThrow(() -> new CredentialNotFoundException(credentialId));
    }

    @Override
    public void deleteCredential(UUID credentialId) {
        var kvPath = getCredentialKvPath(credentialId);
        this.vaultClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).deleteSecretFromKvEngine(kvPath);
        this.vaultClient.acl().deletePolicy(getCredentialPolicyNameById(credentialId));
    }

    @Override
    public void updateCredentialScopes(UUID credentialId, List<String> scopes) {
        try {
            var kvPath = credentialId.toString();
            var optionalKevSecrets = this.vaultClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME)
                    .getSecretsOfPath(kvPath);
            if (optionalKevSecrets.isEmpty()) {
                throw new CredentialNotFoundException(credentialId);
            }
            var existingMetadata = new HashMap<>(optionalKevSecrets.get().getMetadata().getCustomMetadata());
            existingMetadata.put(VaultCredentialRepository.VAULT_METADATA_CREDENTIAL_SCOPES_KEY,
                    this.objectMapper.writeValueAsString(scopes));
            this.vaultClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME)
                    .addMetadataToKvEngine(kvPath, existingMetadata);
        } catch (Exception e) {
            throw new CredentialRuntimeException("Failed to update credential scopes for id '" + credentialId + "'.", e);
        }
    }

    @Override
    public boolean hasGroupReadAccessToCredential(UUID credentialId, String groupId) {
        try {
            var vaultGroup = this.vaultClient.acl().getGroupByName(groupId);
            if (vaultGroup == null || vaultGroup.getPolicies() == null) {
                return false;
            }

            for (var policyName : vaultGroup.getPolicies()) {
                if (policyName.equals(VaultCredentialRepository.getCredentialPolicyNameById(credentialId))) {
                    return true;
                }
            }
        } catch (VaultGroupNotFoundException e) {
            LOG.debug("Group with id '{}' not found in Vault. Assuming no access to credential '{}'.", groupId, credentialId);
            return  false;
        }
        
        return false;
    }
    //endregion CredentialRepository

    public static String getCredentialPolicyNameById(UUID credentialId) {
        return VAULT_CREDENTIAL_POLICY_PREFIX + credentialId;
    }

    private String getCredentialKvPath(UUID credentialId) {
        return credentialId.toString();
    }
}
