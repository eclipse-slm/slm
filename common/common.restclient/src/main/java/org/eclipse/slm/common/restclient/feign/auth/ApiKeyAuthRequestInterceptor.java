package org.eclipse.slm.common.restclient.feign.auth;

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

