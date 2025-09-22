package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.common.vault.model.KvPath;
import org.eclipse.slm.resource_management.features.capabilities.CapabilityUtil;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Component
public class SingleHostCapabilitiesVaultClient {
    private final static Logger LOG = LoggerFactory.getLogger(SingleHostCapabilitiesVaultClient.class);
    public final static String VAULT_ENGINE_NAME = "resources";
    public final static String VAULT_POLICY_PREFIX = "policy_resource_";
    public final static String VAULT_GROUP_PREFIX = "group_resource_";
    public final static String VAULT_ROLE_PREFIX = "resource_";
    private final VaultClient vaultClient;
    private final CapabilityUtil capabilityUtil;


    @Autowired
    public SingleHostCapabilitiesVaultClient(
            VaultClient vaultClient,
            CapabilityUtil capabilityUtil
    ) {
        this.vaultClient = vaultClient;
        this.capabilityUtil = capabilityUtil;
    }

    public Map<String, String> getSingleHostCapabilityServiceSecrets(
            VaultCredential vaultCredential,
            UUID resourceId,
            CapabilityService capabilityService
    ) {
        return vaultClient.getKvContent(
                vaultCredential,
                new KvPath(
                        this.VAULT_ENGINE_NAME,
                        getSecretPathForSingleHostCapabilityServiceSecrets(resourceId, capabilityService)
                )
        );
    }

    public void addSingleHostCapabilityServiceSecrets(
            VaultCredential vaultCredential,
            CapabilityService capabilityService,
            UUID resourceId,
            Map<String, String> configParameters
    ) {
        var secretConfigParameter = this.capabilityUtil.getSecretConfigParameter(capabilityService.getCapability(), configParameters);

        this.vaultClient.addSecretToKvEngine(
                vaultCredential,
                VAULT_ENGINE_NAME,
                getSecretPathForSingleHostCapabilityServiceSecrets(resourceId, capabilityService),
                secretConfigParameter
        );
    }

    public String deleteSingleHostCapabilityServiceSecrets(
        VaultCredential vaultCredential,
        UUID resourceId,
        CapabilityService capabilityService
    ) {
        vaultClient.removeSecretFromKvEngine(
                vaultCredential,
                this.VAULT_ENGINE_NAME,
                getSecretPathForSingleHostCapabilityServiceSecrets(resourceId, capabilityService)
        );

        return removeReadVaultPolicy(capabilityService);
    }

    private String getSecretPathForSingleHostCapabilityServiceSecrets(UUID resourceId, CapabilityService capabilityService) {
        return resourceId + "/" + "capabilities/" + capabilityService.getId();
    }

    private String getReadPolicyName(CapabilityService capabilityService) {
        return VAULT_POLICY_PREFIX + capabilityService.getId();
    }

    private String getGroupName(CapabilityService capabilityService) {
        return VAULT_GROUP_PREFIX + capabilityService.getId();
    }

    private String getRoleName(CapabilityService capabilityService) {
        return VAULT_ROLE_PREFIX + capabilityService.getId();
    }

    private String getPolicyRule(CapabilityService capabilityService) {
        UUID id = capabilityService.getId();
        return "path \""+VAULT_ENGINE_NAME+"/data/" + id + "\" { capabilities = [\"list\", \"read\"] }\npath \""+VAULT_ENGINE_NAME+"/data/" + id + "/*\" { capabilities = [\"list\", \"read\"] }";
    }

    private String createReadVaultPolicy(CapabilityService capabilityService) {
        UUID capabilityServiceId = capabilityService.getId();

        // add policy for resource
        var resourceSecretsReadPolicyName = getReadPolicyName(capabilityService);
        this.vaultClient.addPolicy(
                new VaultCredential(),
                resourceSecretsReadPolicyName,
                getPolicyRule(capabilityService)
        );

        // add group with link to new read access policy
        var resourceSecretsReadGroupName = getGroupName(capabilityService);
        this.vaultClient.addGroup(
                new VaultCredential(),
                resourceSecretsReadGroupName,
                "external",
                Arrays.asList(resourceSecretsReadPolicyName)
        );
        var canonicalIdReadGroup = this.vaultClient.getGroupId(new VaultCredential(), resourceSecretsReadGroupName);

        // Add group alias to link Keycloak role with read access group
        var keycloakRole = getRoleName(capabilityService);
        var mountAccessor = this.vaultClient.getJwtMountAccessor(new VaultCredential());
        if (!mountAccessor.equals(""))
            this.vaultClient.addJwtGroupAlias(
                    new VaultCredential(),
                    keycloakRole,
                    mountAccessor,
                    canonicalIdReadGroup
            );
        else {
            LOG.warn("Keycloak mount accessor not available!");
            return "";
        }

        return keycloakRole;
    }

    private String removeReadVaultPolicy(CapabilityService capabilityService) {
        this.vaultClient.removePolicy(
                new VaultCredential(),
                getReadPolicyName(capabilityService)
        );

        this.vaultClient.removeGroup(
                new VaultCredential(),
                getGroupName(capabilityService)
        );

        return getRoleName(capabilityService);
    }
}
