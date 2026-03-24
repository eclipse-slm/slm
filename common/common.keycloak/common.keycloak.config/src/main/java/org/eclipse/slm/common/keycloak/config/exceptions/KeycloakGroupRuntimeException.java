package org.eclipse.slm.common.keycloak.config.exceptions;

public class KeycloakGroupRuntimeException extends Exception {

    public KeycloakGroupRuntimeException(String message) {
        super(message);
    }

    public KeycloakGroupRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
