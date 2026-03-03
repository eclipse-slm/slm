package org.eclipse.slm.common.vault.client.auth;

import org.eclipse.slm.common.restclient.feign.FeignClientFactory;
import org.eclipse.slm.common.restclient.feign.auth.AuthRequestInterceptor;
import org.eclipse.slm.common.vault.client.apiclients.VaultApiClientAuth;
import org.eclipse.slm.common.vault.model.auth.JwtLoginRequest;

public class VaultJwtAuthRequestInterceptor extends AuthRequestInterceptor {

    private final String vaultUrl;
    private final String jwt;

    public VaultJwtAuthRequestInterceptor(String vaultUrl, String jwt) {
        if (!vaultUrl.endsWith("/v1")) {
            vaultUrl += "/v1";
        }
        this.vaultUrl = vaultUrl;
        this.jwt = jwt;
    }

    @Override
    public String getAuthorizationHeaderValue() {
        var vaultApiClientAuth = FeignClientFactory.createClient(VaultApiClientAuth.class, vaultUrl);
        var loginRequest = new JwtLoginRequest(jwt);
        var authResponse = vaultApiClientAuth.loginWithJwt(loginRequest);

        return "Bearer " + authResponse.getAuth().getClientToken();
    }

}
