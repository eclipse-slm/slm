package org.eclipse.slm.common.aas.clients.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public abstract class AuthRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("Authorization", this.getAuthorizationHeaderValue());
    }

    public abstract String getAuthorizationHeaderValue();

}
