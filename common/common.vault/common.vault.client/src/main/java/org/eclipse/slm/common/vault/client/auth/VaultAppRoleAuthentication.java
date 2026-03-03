package org.eclipse.slm.common.vault.client.auth;

import org.eclipse.slm.common.restclient.feign.auth.AuthRequestInterceptor;

public class VaultAppRoleAuthentication extends VaultAuthentication {

    private final String vaultUrl;
    private final String appRoleId;
    private final String appRoleSecretId;

    public VaultAppRoleAuthentication(String vaultUrl, String appRoleId, String appRoleSecretId) {
        super(VaultAuthenticationType.APP_ROLE);
        this.vaultUrl = vaultUrl;
        this.appRoleId = appRoleId;
        this.appRoleSecretId = appRoleSecretId;
    }

    public String getAppRoleSecretId() {
        return appRoleSecretId;
    }

    public String getAppRoleId() {
        return appRoleId;
    }

    public AuthRequestInterceptor getAuthRequestInterceptor() {
        return new VaultAppRoleAuthRequestInterceptor(this.vaultUrl, this.appRoleId, this.appRoleSecretId);
    }
}
