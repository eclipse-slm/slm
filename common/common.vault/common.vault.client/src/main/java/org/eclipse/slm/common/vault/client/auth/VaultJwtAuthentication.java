package org.eclipse.slm.common.vault.client.auth;

import org.eclipse.slm.common.restclient.feign.auth.AuthRequestInterceptor;

public class VaultJwtAuthentication extends VaultAuthentication {

    private final String vaultUrl;
    private final String jwt;

    public VaultJwtAuthentication(String vaultUrl, String jwt) {
        super(VaultAuthenticationType.JWT);
        this.vaultUrl = vaultUrl;
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    public AuthRequestInterceptor getAuthRequestInterceptor() {
        return new VaultJwtAuthRequestInterceptor(this.vaultUrl, this.jwt);
    }
}
