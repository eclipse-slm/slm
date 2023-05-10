package org.eclipse.slm.common.keycloak.config;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A helper class to to work with a keycloak based security context.
 *
 * @author des
 */
public class KeycloakSecurityContextHelper {

    private KeycloakSecurityContextHelper(){}

    /**
     * Is authenticated boolean.
     *
     * @param securityContext the security context
     * @return the boolean
     */
    public static boolean isAuthenticated(SecurityContext securityContext) {
        Authentication authentication = getAuthentication(securityContext);
        return authentication.isAuthenticated();
    }

    /**
     * Has role boolean.
     *
     * @param securityContext the security context
     * @param type            the type
     * @return the boolean
     */
    public static boolean hasRole(SecurityContext securityContext, SecurityGrants.ROLE type) {
        Authentication authentication = getAuthentication(securityContext);
        return hasRole(authentication, type);
    }

    /**
     * Has role boolean.
     *
     * @param authentication the authentication
     * @param type           the type
     * @return the boolean
     */
    public static boolean hasRole(Authentication authentication, SecurityGrants.ROLE type) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(type.name());
        return authentication.getAuthorities().contains(grantedAuthority);
    }

    /**
     * Gets role types.
     *
     * @param securityContext the security context
     * @return the role types
     */
    public static Set<SecurityGrants.ROLE> getRoleTypes(SecurityContext securityContext) {
        Authentication authentication = getAuthentication(securityContext);
        return getRoleTypes(authentication);
    }

    /**
     * Gets role types.
     *
     * @param authentication the authentication
     * @return the role types
     */
    public static Set<SecurityGrants.ROLE> getRoleTypes(Authentication authentication) {
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) authentication.getAuthorities();
        return authorities.stream().map(x -> SecurityGrants.findByType(x.getAuthority())).collect(Collectors.toSet());
    }

    /**
     * Gets realm.
     *
     * @param securityContext the security context
     * @return the realm
     */
    public static String getRealm(SecurityContext securityContext) {
        Authentication authentication = getAuthentication(securityContext);
        if(authentication instanceof KeycloakAuthenticationToken) {
            return getRealm((KeycloakAuthenticationToken) authentication);
        } else {
            throw new SecurityContextNotFoundException("SecurityContext Keycloak Authentication Not Found Exception");
        }
    }

    /**
     * Gets realm.
     *
     * @param authentication the authentication
     * @return the realm
     */
    public static String getRealm(KeycloakAuthenticationToken authentication) {
        OidcKeycloakAccount oidcKeycloakAccount = authentication.getAccount();
        KeycloakSecurityContext keycloakSecurityContext = oidcKeycloakAccount.getKeycloakSecurityContext();
        return keycloakSecurityContext.getRealm();
    }

    /**
     * Gets access token.
     *
     * @param securityContext the security context
     * @return the access token
     */
    public static AccessToken getAccessToken(SecurityContext securityContext) {
        Authentication authentication = getAuthentication(securityContext);
        return getAccessToken(authentication);
    }

    /**
     * Gets access token.
     *
     * @param authentication the authentication
     * @return the access token
     */
    public static AccessToken getAccessToken(Authentication authentication) {
        KeycloakPrincipal<KeycloakSecurityContext> principal = (KeycloakPrincipal) authentication.getPrincipal();
        return principal.getKeycloakSecurityContext().getToken();
    }

    /**
     * Extracts an authentication from given security context.
     * This can be created e.g. with SecurityContextHolder.getContext().
     * SecurityContextNotFoundException is thrown if no authentication has been extracted.
     *
     * @param securityContext the security context
     * @return the authentication
     */
    private static Authentication getAuthentication(SecurityContext securityContext) {
        if(securityContext==null) {
            throw new SecurityContextNotFoundException("SecurityContext Not Found Exception");
        }
        final Authentication authentication = securityContext.getAuthentication();
        if(authentication == null) {
            throw new SecurityContextNotFoundException("SecurityContext Authentication Not Found Exception");
        }
        return authentication;
    }

}
