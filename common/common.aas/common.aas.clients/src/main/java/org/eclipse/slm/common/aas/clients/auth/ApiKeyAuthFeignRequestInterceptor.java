package org.eclipse.slm.common.aas.clients.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class ApiKeyAuthFeignRequestInterceptor implements RequestInterceptor {
    private final String apiKey;

    public ApiKeyAuthFeignRequestInterceptor(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", apiKey);
    }
}

