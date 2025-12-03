package org.eclipse.slm.common.aas.clients.auth;

public class ApiKeyAuthRequestInterceptor extends AuthRequestInterceptor {
    private final String apiKey;

    public ApiKeyAuthRequestInterceptor(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String getAuthorizationHeaderValue() {
        return apiKey;
    }
}

