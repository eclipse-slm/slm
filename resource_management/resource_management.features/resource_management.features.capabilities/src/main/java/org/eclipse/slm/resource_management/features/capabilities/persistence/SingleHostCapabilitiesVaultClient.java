package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultClientFactory;
import org.eclipse.slm.common.vault.client.exceptions.VaultKvSecretsNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.CapabilityUtil;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class SingleHostCapabilitiesVaultClient {
    private final static Logger LOG = LoggerFactory.getLogger(SingleHostCapabilitiesVaultClient.class);

    public final static String VAULT_SECRETS_ENGINE_NAME = "capabilities";
    public final static String VAULT_POLICY_PREFIX = "capability_";

    private final VaultClient vaultAdminClient;

    @Autowired
    public SingleHostCapabilitiesVaultClient(VaultClientFactory vaultClientFactory) {
        this(vaultClientFactory.createAdminClient());
    }

    public SingleHostCapabilitiesVaultClient(VaultClient vaultClient) {
        this.vaultAdminClient = vaultClient;
    }

    public static String getCapabilityServicePolicyName(UUID capabilityServiceId) {
        return VAULT_POLICY_PREFIX + capabilityServiceId;
    }

    public static String getCapabilityServicePolicyRule(UUID capabilityServiceId) {
        return "path \""+ VAULT_SECRETS_ENGINE_NAME +"/data/" + capabilityServiceId + "\" { capabilities = [\"list\", \"read\"] }";
    }

    public static String getSecretPathForSingleHostCapabilityServiceSecrets(UUID capabilityServiceId) {
        return capabilityServiceId.toString();
    }

    public Map<String, String> getSingleHostCapabilityServiceSecrets(UUID capabilityServiceId) {
        try {
            return vaultAdminClient.kv(SingleHostCapabilitiesVaultClient.VAULT_SECRETS_ENGINE_NAME)
                    .getSecretsOfPathOrThrow(getSecretPathForSingleHostCapabilityServiceSecrets(capabilityServiceId))
                    .getData();
        } catch (VaultKvSecretsNotFoundException e) {
            return Map.of();
        }
    }

    public void addSingleHostCapabilityServiceSecrets(CapabilityService capabilityService, Map<String, String> configParameters, String fullPathOwnerGroupId) {
        var secretsConfigParameter = CapabilityUtil.getSecretConfigParameter(capabilityService.getCapability(), configParameters);
        var secretsPath = getSecretPathForSingleHostCapabilityServiceSecrets(capabilityService.getServiceId());
        this.vaultAdminClient.kv(VAULT_SECRETS_ENGINE_NAME).addSecretsToKvEngine(secretsPath, secretsConfigParameter);

        var capabilityPolicyName = SingleHostCapabilitiesVaultClient.getCapabilityServicePolicyName(capabilityService.getServiceId());
        var capabilityPolicyRule = SingleHostCapabilitiesVaultClient.getCapabilityServicePolicyRule(capabilityService.getServiceId());
        this.vaultAdminClient.acl().createOrUpdatePolicy(capabilityPolicyName, capabilityPolicyRule);
        this.vaultAdminClient.acl().addPolicyToGroup(fullPathOwnerGroupId, capabilityPolicyName);
    }

    public void deleteSingleHostCapabilityServiceSecrets(
        UUID capabilityServiceId
    ) {
        var secretsPath = getSecretPathForSingleHostCapabilityServiceSecrets(capabilityServiceId);
        vaultAdminClient.kv(SingleHostCapabilitiesVaultClient.VAULT_SECRETS_ENGINE_NAME).deleteSecretFromKvEngine(secretsPath);
        this.vaultAdminClient.acl().deletePolicy(SingleHostCapabilitiesVaultClient.getCapabilityServicePolicyName(capabilityServiceId));
    }
}
