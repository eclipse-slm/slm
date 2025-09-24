package org.eclipse.slm.resource_management.common.adapters;

import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.common.vault.client.VaultCredentialType;
import org.eclipse.slm.common.vault.model.KvPath;
import org.eclipse.slm.resource_management.common.remote_access.*;
import org.eclipse.slm.common.vault.model.exceptions.CertificateAuthorityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ResourcesVaultClient {

    private final Logger LOG = LoggerFactory.getLogger(ResourcesVaultClient.class);

    public final static String VAULT_POLICY_PREFIX = "policy_resource_";

    public final static String VAULT_SLM_ROOT_PKI_NAME = "pki_root_slm";

    private final VaultClient vaultClient;

    public static String getIntermediatePkiNameOfResource(UUID resourceId) {
        return "resource-" + resourceId;
    }

    public ResourcesVaultClient(VaultClient vaultClient) {
        this.vaultClient = vaultClient;
    }

    public void initResourceKV(UUID resourceId) {
        var vaultCredential = new VaultCredential();

        // Add read access policy for secrets
        var resourceSecretsReadPolicyName = "policy_resource_" + resourceId;
        this.vaultClient.addPolicy(
                vaultCredential,
                resourceSecretsReadPolicyName,
                "path \"resources/data/"+ resourceId + "/*\" { capabilities = [\"list\", \"read\"] }"
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

    public Credential getCredentialOfRemoteAccessService(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID resourceId,
            UUID remoteAccessId,
            CredentialClass credentialClass
    ) {
        var kvPath = resourceId + "/remoteAccess/" + remoteAccessId + "/" + credentialClass;
        var resourceVaultPath = new KvPath("resources", kvPath);
        Map<String, String> kvContent = vaultClient.getKvContent(
                new VaultCredential(VaultCredentialType.KEYCLOAK_TOKEN, jwtAuthenticationToken.getToken().getTokenValue()),
                resourceVaultPath
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

    public RemoteAccessConsulService getRemoteAccessServiceByNodeService(
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

        return new RemoteAccessConsulService(
                connectionType,
                credential
        );
    }

    public Map<String, String> getSecretsForResource(VaultCredential vaultCredential, UUID resourceId, String path) {
        var kvPathSegment = resourceId + "/" + path;
        KvPath resourceVaultPath = new KvPath("resources", kvPathSegment);

        var content = this.vaultClient.getKvContent(vaultCredential, resourceVaultPath);

        return content;
    }

    public void addSecretsForResource(VaultCredential vaultCredential, UUID resourceId, String path, Map<String, String> secretsOfResource) {
        // Add secrets for resource
        var kvPathSegment = resourceId + "/" + path;
        KvPath resourceVaultPath = new KvPath("resources", kvPathSegment);
        this.vaultClient.addSecretToKvEngine(
                vaultCredential,
                resourceVaultPath.getSecretEngine(),
                resourceVaultPath.getPath(),
                secretsOfResource
        );
    }

    public void addSecretsForConnectionService(UUID resourceId, RemoteAccessConsulService remoteAccessConsulService) {
        var vaultCredential = new VaultCredential();

        var secretsOfResource = new HashMap<String, String>();
        var credential = (CredentialUsernamePassword) remoteAccessConsulService.getCredential();
        secretsOfResource.put("username", credential.getUsername());
        secretsOfResource.put("password", credential.getPassword());

        var credentialClassName = remoteAccessConsulService.getCredential().getClass().getSimpleName();
        var serviceId = String.valueOf(remoteAccessConsulService.getId());
        var kvPath = resourceId + "/remoteAccess/" + serviceId + "/" + credentialClassName;
        var resourceVaultPath = new KvPath("resources", kvPath);
        this.vaultClient.addSecretToKvEngine(
                vaultCredential,
                resourceVaultPath.getSecretEngine(),
                resourceVaultPath.getPath(),
                secretsOfResource
        );
    }

    public void removeSecretsForResource(VaultCredential vaultCredential, UUID resourceId) {
        var secretsEngine = "resources";

        List<String> secrets = vaultClient.listAllSecretsRecursive(vaultCredential, secretsEngine, resourceId.toString());
        for (var secret : secrets) {
            vaultClient.removeSecretFromKvEngine(vaultCredential, "resources", secret);
        }
        this.vaultClient.removePolicy(vaultCredential, "policy_resource_" + resourceId);

        var resourceSecretsReadGroupName = "group_resource_" + resourceId;
        this.vaultClient.removeGroup(vaultCredential, resourceSecretsReadGroupName);
    }

    public void removeSecretsOfRemoteAccessService(VaultCredential vaultCredential, UUID resourceId, UUID remoteAccessId) {
        var kvPath = resourceId + "/remoteAccess/" + remoteAccessId + "/CredentialUsernamePassword";
        var resourceVaultPath = new KvPath("resources", kvPath);

        this.vaultClient.removeSecretFromKvEngine(vaultCredential, resourceVaultPath.getSecretEngine(), resourceVaultPath.getPath());
    }

    public void createIntermediateCertificateAuthority(UUID resourceId, String resourceIp, String resourceHostname) {
        var credential = new VaultCredential();
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
            this.vaultClient.addIntermediateCA(credential, pkiName, commonName, issuerName, ResourcesVaultClient.VAULT_SLM_ROOT_PKI_NAME);
            this.vaultClient.createIntermediateRole(credential, pkiName, domains, roleName);
        } catch (Exception e) {
            LOG.error("Could not create Intermediate Certificate for Resource \"{}\"", resourceId, e);
        }
    }

    public void removeIntermediateCertificateAuthority(UUID resourceId) {
        var credential = new VaultCredential();
        try {
            var pkiName = ResourcesVaultClient.getIntermediatePkiNameOfResource(resourceId);
            this.vaultClient.disableIntermediateCA(credential, pkiName);
        }catch (CertificateAuthorityException e) {
            LOG.info("Could not delete Intermediate Certificate for resource '{}'", resourceId);
        }
    }

}
