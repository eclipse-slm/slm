package org.eclipse.slm.common.restclient.feign.auth;

public class BearerTokenAuthRequestInterceptor extends AuthRequestInterceptor {

    private final String bearerToken;

    public BearerTokenAuthRequestInterceptor(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public String getAuthorizationHeaderValue() {
        return "Bearer " + bearerToken;
    }
}
