package org.eclipse.slm.common.restclient.feign.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public abstract class AuthRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header(this.getAuthorizationHeaderKey(), this.getAuthorizationHeaderValue());
    }

    public String getAuthorizationHeaderKey() {
        return "Authorization";
    }

    public abstract String getAuthorizationHeaderValue();

}
