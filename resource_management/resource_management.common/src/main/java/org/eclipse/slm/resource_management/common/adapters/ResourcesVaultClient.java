package org.eclipse.slm.resource_management.common.adapters;

import org.eclipse.slm.common.vault.client.*;
import org.eclipse.slm.common.vault.client.exceptions.VaultRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ResourcesVaultClient {

    private final Logger LOG = LoggerFactory.getLogger(ResourcesVaultClient.class);

    public final static String VAULT_SECRET_ENGINE_NAME = "resources";
    public final static String VAULT_RESOURCE_POLICY_PREFIX = "resource_";
    public final static String VAULT_SLM_ROOT_PKI_NAME = "pki_root_slm";

    private final VaultClient vaultAdminClient;

    public static String getIntermediatePkiNameOfResource(UUID resourceId) {
        return "resource-" + resourceId;
    }
    public static String getResourcePolicyName(UUID resourceId) {return VAULT_RESOURCE_POLICY_PREFIX + resourceId;}
    public static String getResourceKvPath(UUID resourceId, String path) {return resourceId + "/" + path;}

    @Autowired
    public ResourcesVaultClient(VaultClientFactory vaultClientFactory) {
        this.vaultAdminClient = vaultClientFactory.createAdminClient();
    }

    public ResourcesVaultClient(VaultClient vaultAdminClient) {
        this.vaultAdminClient = vaultAdminClient;
    }

    public void initResourceKV(UUID resourceId, String fullPathOwnerGroupId) {
        // Add policy for secrets ...
        var resourcePolicyName = ResourcesVaultClient.getResourcePolicyName(resourceId);
        var resourcePolicyRule = "path \"" + VAULT_SECRET_ENGINE_NAME + "/data/"+ resourceId + "/*\" { capabilities = [\"list\", \"read\"] }";
        this.vaultAdminClient.acl().createOrUpdatePolicy(resourcePolicyName, resourcePolicyRule);
        // ... and assign to owner group
        this.vaultAdminClient.acl().addPolicyToGroup(fullPathOwnerGroupId, resourcePolicyName);
    }

    public Map<String, String> getSecretsForResource(UUID resourceId, String path) {
        var resourceKvSecrets = this.vaultAdminClient.kv(ResourcesVaultClient.VAULT_SECRET_ENGINE_NAME)
                .getSecretsOfPathOrThrow(ResourcesVaultClient.getResourceKvPath(resourceId, path));

        return resourceKvSecrets.getData();
    }

    public void addSecretsForResource(UUID resourceId, String path, Map<String, String> secretsOfResource) {
        this.vaultAdminClient.kv(ResourcesVaultClient.VAULT_SECRET_ENGINE_NAME)
                .addSecretsToKvEngine(ResourcesVaultClient.getResourceKvPath(resourceId, path), secretsOfResource);
    }

    public void removeSecretsForResource(UUID resourceId) {
        var secretKeys = vaultAdminClient.kv(ResourcesVaultClient.VAULT_SECRET_ENGINE_NAME).getSecretKeysOfPath(resourceId.toString());
        for (var secretKey : secretKeys.getData().keySet()) {
            vaultAdminClient.kv(ResourcesVaultClient.VAULT_SECRET_ENGINE_NAME).deleteSecretFromKvEngine(secretKey);
        }
        this.vaultAdminClient.acl().deletePolicy(ResourcesVaultClient.getResourcePolicyName(resourceId));
    }

    public void createIntermediateCertificateAuthority(UUID resourceId, String resourceIp, String resourceHostname) {
        try {
            var domains = new ArrayList<String>();

            if (resourceHostname != null) {
                domains.add(resourceHostname);
            }
            if ((resourceIp != null)) {
                domains.add(resourceIp);
            }

            var pkiName = ResourcesVaultClient.getIntermediatePkiNameOfResource(resourceId);
            var commonName = "Resource '" + resourceId + "' Intermediate CA";
            var issuerName = "resource-" + resourceId + "-intermediate-ca";
            var roleName = "resource";
            this.vaultAdminClient.pki().createIntermediateCA(pkiName, commonName, issuerName, ResourcesVaultClient.VAULT_SLM_ROOT_PKI_NAME);
            this.vaultAdminClient.pki().createIntermediateRole(pkiName, domains, roleName);
        } catch (Exception e) {
            throw new VaultRuntimeException("Could not create Intermediate Certificate Authority for resource '" + resourceId + "'", e);
        }
    }

    public void removeIntermediateCertificateAuthority(UUID resourceId) {
        try {
            var pkiName = ResourcesVaultClient.getIntermediatePkiNameOfResource(resourceId);
            this.vaultAdminClient.pki().deleteIntermediateCA(pkiName);
        } catch (Exception e) {
            throw new VaultRuntimeException("Could not delete Intermediate Certificate for resource '" + resourceId + "'", e);
        }
    }

}
