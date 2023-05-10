package org.eclipse.slm.common.keycloak.config.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class KeycloakGroupNotFoundException extends Exception {

        public KeycloakGroupNotFoundException(String keycloakGroupname)
        {
            super("Keycloak group with name '" + keycloakGroupname + "' not found");
        }

}
