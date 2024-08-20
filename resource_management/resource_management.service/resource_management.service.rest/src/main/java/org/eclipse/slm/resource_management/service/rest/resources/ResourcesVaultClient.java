package org.eclipse.slm.resource_management.service.rest.resources;

import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.common.vault.client.VaultCredentialType;
import org.eclipse.slm.common.vault.model.KvPath;
import org.eclipse.slm.resource_management.model.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ResourcesVaultClient {

    private final Logger LOG = LoggerFactory.getLogger(ResourcesVaultClient.class);

    private final VaultClient vaultClient;

    public ResourcesVaultClient(VaultClient vaultClient) {
        this.vaultClient = vaultClient;
    }

    @Deprecated
    public BasicResource getSecretsOfResources(BasicResource basicResource, VaultCredential vaultCredential) {
//        if (basicResource.getCredentialsType() != null) {
//            if (basicResource.getCredentialsType().equals("vault_kv")) {
//                var kvPath = new KvPath("resources", basicResource.getId().toString());
//                Map<String, String> kvContent = vaultClient.getKvContent(
//                        vaultCredential,
//                        kvPath
//                );
//
//                basicResource.setUsername(kvContent.get("username"));
//                basicResource.setPassword(kvContent.get("password"));
//            }
//        }
//
//        return basicResource;
        return null;
    }

    public Credential getCredentialOfRemoteAccessService(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID serviceId,
            CredentialClass credentialClass
    ) {
        var kvPath = new KvPath("resources", serviceId+"/"+credentialClass);
        Map<String, String> kvContent = vaultClient.getKvContent(
                new VaultCredential(VaultCredentialType.KEYCLOAK_TOKEN, jwtAuthenticationToken.getToken().getTokenValue()),
                kvPath
        );

        Credential credential = null;

        if(credentialClass.name().equals(CredentialUsernamePassword.class.getSimpleName())) {
            credential = new CredentialUsernamePassword(
                    kvContent.get("username"),
                    kvContent.get("password")
            );
        }

        return credential;
    }

    public RemoteAccessService getRemoteAccessServiceByNodeService(
            VaultCredential vaultCredential,
            NodeService nodeService
    ) {
        Map<String, String> meta = nodeService.getMeta();
        String credentialClass = meta.get("credentialClass");


        var kvPath = new KvPath("resources", nodeService.getID()+"/"+credentialClass);
        Map<String, String> kvContent = vaultClient.getKvContent(
                vaultCredential,
                kvPath
        );

        ConnectionType connectionType = ConnectionType.valueOf(
                meta.get("resourceConnectionType")
        );

        Credential credential = null;

        if(credentialClass.equals(CredentialUsernamePassword.class.getSimpleName())) {
            credential = new CredentialUsernamePassword(
                    kvContent.get("username"),
                    kvContent.get("password")
            );
        }

        return new RemoteAccessService(
                connectionType,
                credential
        );
    }

    public void addSecretsForResource(VaultCredential vaultCredential,String resourceId, Map<String, String> secretsOfResource) {
        // Add secrets for resource
        KvPath resourceVaultPath = new KvPath("resources", resourceId);
        this.vaultClient.addSecretToKvEngine(
                vaultCredential,
                resourceVaultPath.getSecretEngine(),
                resourceVaultPath.getPath(),
                secretsOfResource
        );

        // Add read access policy for secrets
        var resourceSecretsReadPolicyName = "policy_resource_" + resourceId;
        this.vaultClient.addPolicy(
                vaultCredential,
                resourceSecretsReadPolicyName,
                "path \"resources/data/"+ resourceId + "\" { capabilities = [\"list\", \"read\"] }"
        );

        // Add group with link to new read access policy
        var resourceSecretsReadGroupName = "group_resource_" + resourceId;
        this.vaultClient.addGroup(
                vaultCredential,
                resourceSecretsReadGroupName,
                "external",
                Arrays.asList(resourceSecretsReadPolicyName)
        );
        var canonicalIdReadGroup = this.vaultClient.getGroupId(vaultCredential, resourceSecretsReadGroupName);

        // Add group alias to link Keycloak role with read access group
        var keycloakRole = "resource_" + resourceId;
        var mountAccessor = this.vaultClient.getJwtMountAccessor(vaultCredential);
        if(!mountAccessor.equals(""))
            this.vaultClient.addJwtGroupAlias(
                    vaultCredential,
                    keycloakRole,
                    mountAccessor,
                    canonicalIdReadGroup
            );
        else
            LOG.warn("Keycloak mount accessor not available!");
    }

    public void addSecretsForConnectionService(
            JwtAuthenticationToken jwtAuthenticationToken,
            RemoteAccessService remoteAccessService
    ) {
        Map<String, String> secretsOfResource = new HashMap<>();
        CredentialUsernamePassword credential = (CredentialUsernamePassword) remoteAccessService.getCredential();
        secretsOfResource.put("username", credential.getUsername());
        secretsOfResource.put("password", credential.getPassword());

        String credentialClassName = remoteAccessService.getCredential().getClass().getSimpleName();
        String serviceId = String.valueOf(remoteAccessService.getId());
        String kvPath = serviceId+"/"+credentialClassName;

        KvPath resourceVaultPath = new KvPath(
                "resources",
                kvPath
        );
        this.vaultClient.addSecretToKvEngine(
                new VaultCredential(),
                resourceVaultPath.getSecretEngine(),
                resourceVaultPath.getPath(),
                secretsOfResource
        );

        var resourceSecretsReadPolicyName = "policy_service_" + remoteAccessService.getId();
        this.vaultClient.addPolicy(
                new VaultCredential(),
                resourceSecretsReadPolicyName,
                "path \"resources/data/"+ serviceId + "/*\" { capabilities = [\"list\", \"read\"] }"
        );

        // Add group with link to new read access policy
        var resourceSecretsReadGroupName = "group_service_" + serviceId;
        this.vaultClient.addGroup(
                new VaultCredential(),
                resourceSecretsReadGroupName,
                "external",
                Arrays.asList(resourceSecretsReadPolicyName)
        );
        var canonicalIdReadGroup = this.vaultClient.getGroupId(new VaultCredential(), resourceSecretsReadGroupName);

        // Add group alias to link Keycloak role with read access group
        var keycloakRole = "service_" + serviceId;
        var mountAccessor = this.vaultClient.getJwtMountAccessor(new VaultCredential());
        if(!mountAccessor.equals(""))
            this.vaultClient.addJwtGroupAlias(
                    new VaultCredential(),
                    keycloakRole,
                    mountAccessor,
                    canonicalIdReadGroup
            );
        else
            LOG.warn("Keycloak mount accessor not available!");
    }

    public void removeSecretsForResource(VaultCredential vaultCredential, BasicResource resource) {
        var secretsEngine = "resources";
        var resourceId = resource.getId().toString();

        this.vaultClient.removeSecretFromKvEngine(vaultCredential, secretsEngine, resourceId);

        this.vaultClient.removePolicy(vaultCredential, "policy_resource_" + resourceId);

        var resourceSecretsReadGroupName = "group_resource_" + resourceId;
        this.vaultClient.removeGroup(vaultCredential, resourceSecretsReadGroupName);
    }

    public void removeSecretsOfRemoteAccessService(VaultCredential vaultCredential, UUID serviceId) {
        String secretsEngine = "resources";
        String secretsPath = String.valueOf(serviceId+"/CredentialUsernamePassword");
        String policyName = "policy_service_" + serviceId;
        String policyGroupName = "group_service_" + serviceId;


        this.vaultClient.removeSecretFromKvEngine(vaultCredential, secretsEngine, secretsPath);
        this.vaultClient.removePolicy(vaultCredential, policyName);
        this.vaultClient.removeGroup(vaultCredential, policyGroupName);
    }
}
