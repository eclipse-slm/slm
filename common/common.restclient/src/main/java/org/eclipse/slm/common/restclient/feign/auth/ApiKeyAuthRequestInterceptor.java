package org.eclipse.slm.common.restclient.feign.auth;

public class ApiKeyAuthRequestInterceptor extends AuthRequestInterceptor {
    private final String apiKey;

    private String apiKeyAuthHeaderKey = "Authorization";

    public ApiKeyAuthRequestInterceptor(String apiKey) {
        this.apiKey = apiKey;
    }

    public ApiKeyAuthRequestInterceptor(String apiKey, String apiKeyAuthHeaderKey) {
        this.apiKey = apiKey;
        this.apiKeyAuthHeaderKey = apiKeyAuthHeaderKey;
    }

    @Override
    public String getAuthorizationHeaderKey() {
        return this.apiKeyAuthHeaderKey;
    }

    @Override
    public String getAuthorizationHeaderValue() {
        return apiKey;
    }
}

