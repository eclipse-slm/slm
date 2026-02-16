package org.eclipse.slm.common.vault.client.auth;

import org.eclipse.slm.common.restclient.feign.FeignClientFactory;
import org.eclipse.slm.common.restclient.feign.auth.AuthRequestInterceptor;
import org.eclipse.slm.common.vault.client.apiclients.VaultApiClientAuth;
import org.eclipse.slm.common.vault.model.auth.AppRoleLoginRequest;

public class VaultAppRoleAuthRequestInterceptor extends AuthRequestInterceptor {

    private final String vaultUrl;
    private final String appRoleId;
    private final String appRoleSecretId;

    public VaultAppRoleAuthRequestInterceptor(String vaultUrl, String appRoleId, String appRoleSecretId) {
        if (!vaultUrl.endsWith("/v1")) {
            vaultUrl += "/v1";
        }
        this.vaultUrl = vaultUrl;
        this.appRoleId = appRoleId;
        this.appRoleSecretId = appRoleSecretId;
    }

    @Override
    public String getAuthorizationHeaderValue() {
        var vaultApiClientAuth = FeignClientFactory.createClient(VaultApiClientAuth.class, vaultUrl);
        var loginRequest = new AppRoleLoginRequest(this.appRoleId, this.appRoleSecretId);
        var authResponse = vaultApiClientAuth.loginWithAppRole(loginRequest);

        return "Bearer " + authResponse.getAuth().getClientToken();
    }

}
