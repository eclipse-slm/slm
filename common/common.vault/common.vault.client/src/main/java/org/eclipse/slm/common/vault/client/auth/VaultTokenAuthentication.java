package org.eclipse.slm.common.vault.client.auth;

import org.eclipse.slm.common.restclient.feign.auth.AuthRequestInterceptor;
import org.eclipse.slm.common.restclient.feign.auth.BearerTokenAuthRequestInterceptor;

public class VaultTokenAuthentication extends VaultAuthentication {

    private final String token;

    public VaultTokenAuthentication(String token) {
        super(VaultAuthenticationType.TOKEN);
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public AuthRequestInterceptor getAuthRequestInterceptor() {
        return new BearerTokenAuthRequestInterceptor(this.token);
    }
}
