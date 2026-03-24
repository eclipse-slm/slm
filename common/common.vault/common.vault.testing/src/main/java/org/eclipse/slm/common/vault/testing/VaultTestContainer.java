package org.eclipse.slm.common.vault.testing;

import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultClientFactory;
import org.eclipse.slm.common.vault.client.auth.VaultJwtAuthentication;
import org.eclipse.slm.common.vault.client.auth.VaultTokenAuthentication;
import org.testcontainers.containers.GenericContainer;

public class VaultTestContainer extends GenericContainer<VaultTestContainer> {

    private static final String VAULT_IMAGE = "hashicorp/vault:1.21.2";
    private static final String VAULT_ROOT_TOKEN = "root";
    private static final int VAULT_PORT = 8200;

    public VaultTestContainer() {
        super(VaultTestContainer.VAULT_IMAGE);
        this.withEnv("VAULT_DEV_ROOT_TOKEN_ID", VaultTestContainer.VAULT_ROOT_TOKEN);
        this.withEnv("VAULT_DEV_LISTEN_ADDRESS", "0.0.0.0:" + VAULT_PORT);
        this.withExposedPorts(VaultTestContainer.VAULT_PORT);
        this.withExtraHost("localhost", "host-gateway");
    }

    public int getPort() {
        return this.getMappedPort(VaultTestContainer.VAULT_PORT);
    }

    public String getRootToken() {
        return VaultTestContainer.VAULT_ROOT_TOKEN;
    }

    public VaultClientFactory getVaultClientFactory() {
        return new VaultClientFactory(this.getVaultUrl(), "TOKEN", VaultTestContainer.VAULT_ROOT_TOKEN, "", "");
    }

    public VaultClient getVaultAdminClient() {
        var vaultUrl = this.getVaultUrl();
        var vaultAuthentication = new VaultTokenAuthentication(VaultTestContainer.VAULT_ROOT_TOKEN);
        return new VaultClient(vaultUrl, vaultAuthentication);
    }

    public VaultClient getVaultUserClient(String userJwt) {
        var vaultUrl = this.getVaultUrl();
        var vaultAuthentication = new VaultJwtAuthentication(vaultUrl, userJwt);
        return new VaultClient(vaultUrl, vaultAuthentication);
    }

    public String getVaultUrl() {
        return "http://" + this.getHost() + ":" + this.getPort() + "/v1";
    }
}
