package org.eclipse.slm.common.keycloak.config;

import org.springframework.security.core.AuthenticationException;

/**
 * The type Security context not found exception.
 *
 * @author des
 */
public class SecurityContextNotFoundException extends AuthenticationException {

    /**
     * Instantiates a new Security context not found exception.
     *
     * @param msg the msg
     * @param t   the t
     */
    public SecurityContextNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Instantiates a new Security context not found exception.
     *
     * @param msg the msg
     */
    public SecurityContextNotFoundException(String msg) {
        super(msg);
    }

}
