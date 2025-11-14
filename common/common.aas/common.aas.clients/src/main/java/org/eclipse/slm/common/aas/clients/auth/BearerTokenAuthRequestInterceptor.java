package org.eclipse.slm.common.aas.clients.auth;

public class BearerTokenAuthRequestInterceptor implements AuthRequestInterceptor {

    private final String bearerToken;

    public BearerTokenAuthRequestInterceptor(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public String getAuthorizationHeaderValue() {
        return "Bearer " + bearerToken;
    }
}
