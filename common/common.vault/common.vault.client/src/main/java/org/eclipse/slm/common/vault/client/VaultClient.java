package org.eclipse.slm.common.vault.client;

import org.eclipse.slm.common.vault.client.auth.VaultAuthentication;
import org.eclipse.slm.common.vault.client.exceptions.VaultRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaultClient {
    private final Logger LOG = LoggerFactory.getLogger(VaultClient.class);

    private final VaultClientAuth vaultClientAuth;
    private final VaultClientAcl vaultClientAcl;
    private VaultClientKv vaultClientKv;
    private final VaultClientPki vaultClientPki;

    private final String vaultUrl;
    private final VaultAuthentication vaultAuthentication;

    public VaultClient(String vaultUrl, VaultAuthentication vaultAuthentication) throws VaultRuntimeException {
        this.vaultUrl = vaultUrl;
        this.vaultAuthentication = vaultAuthentication;

        vaultClientAuth = new VaultClientAuth(this.vaultUrl, this.vaultAuthentication);
        vaultClientAcl = new VaultClientAcl(this.vaultUrl, this.vaultAuthentication);
        vaultClientPki = new VaultClientPki(this.vaultUrl, this.vaultAuthentication);
    }

    public VaultClientAuth auth() {
        return vaultClientAuth;
    }

    public VaultClientAcl acl() {
        return vaultClientAcl;
    }

    public VaultClientKv kv(String secretsEngineName) {
        return new VaultClientKv(this.vaultUrl, this.vaultAuthentication, secretsEngineName);
    }

    public VaultClientPki pki() {
        return vaultClientPki;
    }
}
