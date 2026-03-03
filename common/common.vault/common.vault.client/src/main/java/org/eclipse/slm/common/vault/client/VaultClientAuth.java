package org.eclipse.slm.common.vault.client;

import org.eclipse.slm.common.restclient.feign.FeignResponseException;
import org.eclipse.slm.common.vault.client.auth.VaultAuthentication;
import org.eclipse.slm.common.vault.client.exceptions.VaultRuntimeException;
import org.eclipse.slm.common.vault.model.auth.AuthMethod;
import org.eclipse.slm.common.vault.model.auth.JwtGroupAliasCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VaultClientAuth extends  AbstractVaultClient {
    private final Logger LOG = LoggerFactory.getLogger(VaultClientAuth.class);

    protected VaultClientAuth(String vaultUrl, VaultAuthentication vaultAuthentication) throws VaultRuntimeException {
        super(vaultUrl, vaultAuthentication);
    }

    public void createJwtAuth(String oidcDiscoveryUrl, List<String> boundAudiences) {
        try {
            this.vaultApiClientSys.createMountAuthPath("jwt", Map.of("type", "jwt"));
            this.vaultApiClientAuth.createAuthPathConfig("jwt", Map.of(
                    "oidc_discovery_url", oidcDiscoveryUrl
            ));

            Map<String, Object> roleConfig = new HashMap<>();
            roleConfig.put("name", "default");
            roleConfig.put("role_type", "jwt");
            roleConfig.put("token_ttl", 3600);
            roleConfig.put("token_max_ttl", 3600);
            roleConfig.put("user_claim", "sub");

            roleConfig.put("bound_audiences", boundAudiences);

            Map<String, String> claimMappings = new HashMap<>();
            claimMappings.put("preferred_username", "username");
            claimMappings.put("email", "email");
            roleConfig.put("claim_mappings", claimMappings);

            List<String> allowedRedirectUris = Collections.singletonList("*");
            roleConfig.put("allowed_redirect_uris", allowedRedirectUris);

            roleConfig.put("groups_claim", "groups");
            this.vaultApiClientAuth.createDefaultRoleForAuthPath("jwt", roleConfig);
        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Error creating JWT Auth Method. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }
    }

    public AuthMethod getJwtAuthMethod() {
        try {
            var response = this.vaultApiClientSys.getAuthMethods();
            return response.getData().get("jwt/");
        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Error getting Auth Methods. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }
    }

    public void addJwtGroupAlias(String keycloakRole, String mountAccessor, String canonicalIdGroup) {
        try {
            var requestBody = new JwtGroupAliasCreateRequest(keycloakRole, mountAccessor, "auth/jwt/", "jwt", canonicalIdGroup);
            this.vaultApiClientIdentity.createJwtGroupAlias(requestBody);

        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Error creating JWT Group Alias. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }
    }

    public void removeJwtGroupAlias(String groupAliasId) {
        try {
            this.vaultApiClientIdentity.deleteJwtGroupAliasById(groupAliasId);
        } catch (FeignResponseException e) {
            throw new VaultRuntimeException("Error deleting JWT Group Alias. Status code: " + e.getStatusCode() + ", message: " + e.getMessage(), e);
        }
    }

}
