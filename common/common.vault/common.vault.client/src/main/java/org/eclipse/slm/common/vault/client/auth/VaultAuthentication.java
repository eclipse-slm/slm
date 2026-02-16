package org.eclipse.slm.common.vault.client.auth;

import org.eclipse.slm.common.restclient.feign.auth.AuthRequestInterceptor;

public abstract class VaultAuthentication {

    private final VaultAuthenticationType authenticationType;

    public VaultAuthentication(VaultAuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public VaultAuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public abstract AuthRequestInterceptor getAuthRequestInterceptor();
}
