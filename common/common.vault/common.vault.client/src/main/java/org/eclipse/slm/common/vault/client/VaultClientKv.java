package org.eclipse.slm.common.vault.client;

import org.eclipse.slm.common.restclient.feign.FeignResponseException;
import org.eclipse.slm.common.vault.client.auth.VaultAuthentication;
import org.eclipse.slm.common.vault.client.exceptions.VaultKvSecretsNotFoundException;
import org.eclipse.slm.common.vault.client.exceptions.VaultRuntimeException;
import org.eclipse.slm.common.vault.model.kv.KvMetadataCreateRequest;
import org.eclipse.slm.common.vault.model.mounts.SecretsEngine;
import org.eclipse.slm.common.vault.model.mounts.KvSecretsEngineOptions;
import org.eclipse.slm.common.vault.model.kv.KvSecretsCreateRequest;
import org.eclipse.slm.common.vault.model.kv.KvSecrets;
import org.eclipse.slm.common.vault.model.kv.KvSubkeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VaultClientKv extends AbstractVaultClient {
    private final Logger LOG = LoggerFactory.getLogger(VaultClientKv.class);

    private final String secretsEngineName;

    protected VaultClientKv(String vaultUrl, VaultAuthentication vaultAuthentication, String secretsEngineName) throws VaultRuntimeException {
        super(vaultUrl, vaultAuthentication);
        this.secretsEngineName = secretsEngineName;
    }

    public void createKvSecretEngine() {
        try {
            var mountsResponse = this.vaultApiClientSys.getMounts();
            if (mountsResponse != null && mountsResponse.getData() != null
                    && mountsResponse.getData().containsKey(this.secretsEngineName + "/")) {
                LOG.debug("KV Secret Engine '{}' already exists. Skipping creation.", this.secretsEngineName);
                return;
            }

            var secretsEngine = new SecretsEngine("kv", "Key-Value Secret Engine", null, new KvSecretsEngineOptions("2"));
            this.vaultApiClientSys.createSecretEngine(this.secretsEngineName, secretsEngine);
        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Error creating KV Secret Engine at path '" + this.secretsEngineName + "'. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }
    }

    public KvSubkeys getSecretKeysOfPath(String kvPath) {
        try {
            var response = this.vaultApiClientKv.getSecretSubkeys(this.secretsEngineName, kvPath, null, null);

            return response.getData();
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new VaultKvSecretsNotFoundException(kvPath);
            }
            throw new VaultRuntimeException("Error getting keys of path '" + kvPath + "' in secrets engine '" + this.secretsEngineName + "'. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }
    }

    public Optional<KvSecrets> getSecretsOfPath(String kvPath) {
        try {
            return Optional.of(this.getSecretsOfPathOrThrow(kvPath));
        } catch (VaultKvSecretsNotFoundException e) {
            return Optional.empty();
        }
    }

    public KvSecrets getSecretsOfPathOrThrow(String kvPath) throws VaultKvSecretsNotFoundException {
        try {
            var response = this.vaultApiClientKv.getSecretsOfPath(this.secretsEngineName, kvPath);
            return response.getData();
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new VaultKvSecretsNotFoundException(kvPath);
            }
            throw new VaultRuntimeException("Error getting secrets of path '" + kvPath + "' in secrets engine '" + this.secretsEngineName + "'. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }
    }

    public void addSecretsToKvEngine(String kvPath, Map<String, String> secrets) {
        try {
            this.vaultApiClientKv.addSecretToKvEngine(this.secretsEngineName, kvPath, new KvSecretsCreateRequest(secrets));
        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Error adding secrets to kvPath '" + kvPath + "' in secrets engine '" + this.secretsEngineName + "'. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }
    }

    public void addMetadataToKvEngine(String kvPath, Map<String, String> metadata) {
        try {
            this.vaultApiClientKv.addMetadataToKvEngine(this.secretsEngineName, kvPath, new KvMetadataCreateRequest(metadata));
        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Error adding metadata to kvPath '" + kvPath + "' in secrets engine '" + this.secretsEngineName + "'. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }
    }

    public void deleteSecretFromKvEngine(String kvPath) {
        try {
            this.vaultApiClientKv.deleteSecretData(this.secretsEngineName, kvPath);
            this.vaultApiClientKv.deleteSecretMetadata(this.secretsEngineName, kvPath);
        } catch (FeignResponseException e) {
            if (e.getStatusCode() == 404) {
                // Secret does not exist, nothing to delete
                return;
            }
            throw new VaultRuntimeException("Error deleting secret at kvPath '" + kvPath + "' in secrets engine '" + this.secretsEngineName + "'. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }
    }
}
