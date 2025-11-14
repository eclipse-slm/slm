package org.eclipse.slm.common.aas.clients.auth;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class JwtAuthenticationTokenAuthRequestInterceptor implements AuthRequestInterceptor {

    private final JwtAuthenticationToken jwtAuthenticationToken;

    public JwtAuthenticationTokenAuthRequestInterceptor(JwtAuthenticationToken jwtAuthenticationToken) {
        this.jwtAuthenticationToken = jwtAuthenticationToken;
    }

    @Override
    public String getAuthorizationHeaderValue() {
        return "Bearer " + jwtAuthenticationToken.getToken().getTokenValue();
    }
}
