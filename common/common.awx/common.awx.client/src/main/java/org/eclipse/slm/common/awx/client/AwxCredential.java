package org.eclipse.slm.common.awx.client;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class AwxCredential {
    String username;
    String password;
    JwtAuthenticationToken jwtAuthenticationToken;

    public AwxCredential(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public AwxCredential(JwtAuthenticationToken jwtAuthenticationToken) {
        this.jwtAuthenticationToken = jwtAuthenticationToken;
    }
}
