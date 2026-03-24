package org.eclipse.slm.common.restserver.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * Custom annotation to check if the user has the 'slm-admin' role.
 * This can be used on methods or classes to enforce this security requirement.
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasRole('slm-admin')")
public @interface AuthorizedAsSlmAdmin {
}