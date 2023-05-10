package org.eclipse.slm.common.awx.client;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;

public class AwxCredential {
    String username;
    String password;
    KeycloakPrincipal keycloakPrincipal;

    public AwxCredential(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public AwxCredential(KeycloakPrincipal keycloakPrincipal) {
        this.keycloakPrincipal = keycloakPrincipal;
    }
}
