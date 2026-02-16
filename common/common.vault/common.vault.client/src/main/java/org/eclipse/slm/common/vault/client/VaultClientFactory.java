package org.eclipse.slm.common.vault.client;

import org.eclipse.slm.common.vault.client.auth.VaultAppRoleAuthentication;
import org.eclipse.slm.common.vault.client.auth.VaultAuthentication;
import org.eclipse.slm.common.vault.client.auth.VaultTokenAuthentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VaultClientFactory {

    private final String vaultUrl;

    private final String appRoleId;

    private final String appRoleSecretId;

    private final String token;

    private final String authentication;

    public VaultClientFactory(@Value("${vault.url}") String vaultUrl,
                              @Value("${vault.authentication}") String authentication,
                              @Value("${vault.token:}") String token,
                              @Value("${vault.app-role.role-id:}") String appRoleId,
                              @Value("${vault.app-role.secret-id:}") String appRoleSecretId) {
        this.vaultUrl = vaultUrl;
        this.authentication = authentication;
        this.token = token;
        this.appRoleId = appRoleId;
        this.appRoleSecretId = appRoleSecretId;
    }

    public VaultClient createAdminClient() {
        VaultAuthentication vaultAuthentication;
        switch (this.authentication.toLowerCase()) {
            case "token" -> {
                vaultAuthentication = new VaultTokenAuthentication(this.token);
            }
            case "approle" -> {
                vaultAuthentication = new VaultAppRoleAuthentication(this.vaultUrl, this.appRoleId, this.appRoleSecretId);
            }
            default -> {
                throw new IllegalArgumentException("Unsupported Vault authentication method: " + this.authentication);
            }
        }

        return new VaultClient(this.vaultUrl, vaultAuthentication);
    }

    public VaultClient createClient(VaultAuthentication vaultAuthentication) {
        return new VaultClient(this.vaultUrl, vaultAuthentication);
    }

    public String getVaultUrl() {
        return vaultUrl;
    }
}
