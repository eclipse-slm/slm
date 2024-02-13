package org.eclipse.slm.common.keycloak.config;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.OIDCHttpFacade;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationEntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * The multi tenant keycloak config resolver resolves a keycloak deployment
 * in a configurable manner based on security context, header, and/or path.
 *
 * @author des
 */
@Component
@EnableConfigurationProperties({MultiTenantKeycloakApplicationProperties.class, KeycloakSpringBootProperties.class})
//@PropertySource("application.yml")
public class MultiTenantKeycloakConfigResolver implements KeycloakConfigResolver {

    private static final Logger LOG = LoggerFactory.getLogger(MultiTenantKeycloakConfigResolver.class);
    private KeycloakSpringBootProperties keycloakSpringBootProperties;
    private MultiTenantKeycloakApplicationProperties multiTenantKeycloakApplicationProperties;
    private MultiTenantKeycloakRegistration multiTenantKeycloakRegistration;

    @Autowired
    protected MultiTenantKeycloakConfigResolver(KeycloakSpringBootProperties keycloakSpringBootProperties,
                                                MultiTenantKeycloakApplicationProperties multiTenantKeycloakApplicationProperties,
                                                MultiTenantKeycloakRegistration multiTenantKeycloakRegistration) {
        this.multiTenantKeycloakRegistration = multiTenantKeycloakRegistration;
        this.keycloakSpringBootProperties = keycloakSpringBootProperties;
        this.multiTenantKeycloakApplicationProperties = multiTenantKeycloakApplicationProperties;
    }

    @Override
    public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {
        LOG.trace("uri: {} method: {} relative path: {}", request.getURI(), request.getMethod(), request.getRelativePath());

        if (multiTenantKeycloakRegistration.getRealmCount()==1) {
            return multiTenantKeycloakRegistration.getFirstRealm();
        } else {
            KeycloakDeployment keycloakDeployment = resolveSecurityContext();
            if (keycloakDeployment != null) {
                return keycloakDeployment;
            }
            keycloakDeployment = resolveHeader(request);
            if (keycloakDeployment != null) {
                return keycloakDeployment;
            }
            keycloakDeployment = resolvePath(request);
            if (keycloakDeployment != null) {
                return keycloakDeployment;
            }
        }
        return multiTenantKeycloakRegistration.getFirstRealm();
    }

    private KeycloakDeployment resolveSecurityContext() {
        SecurityContext context = SecurityContextHolder.getContext();
        try {
            if (KeycloakSecurityContextHelper.isAuthenticated(context)) {
                String currentRealm = KeycloakSecurityContextHelper.getRealm(context);
                LOG.trace("logged in at realm: {}", currentRealm);
                if (multiTenantKeycloakRegistration.containsRealm(currentRealm)) {
                    return multiTenantKeycloakRegistration.getRealm(currentRealm);
                }
            }
        } catch (SecurityContextNotFoundException e) {
            LOG.trace("SecurityContextNotFoundException: {}",e.getMessage());
        }
        return null;
    }

    private KeycloakDeployment resolveHeader(HttpFacade.Request request) {
        if (multiTenantKeycloakApplicationProperties.isResolveByHeader()) {
            String realm = request.getHeader(multiTenantKeycloakApplicationProperties.getResolverHeader());
            if (realm != null && !realm.isEmpty() && multiTenantKeycloakRegistration.containsRealm(realm)) {
                LOG.trace("found realm in header: {}", realm);
                return multiTenantKeycloakRegistration.getRealm(realm);
            }
        }
        return null;
    }

    private KeycloakDeployment resolvePath(HttpFacade.Request request) {
        if (multiTenantKeycloakApplicationProperties.isResolveByPath()) {
            String path = request.getRelativePath().toLowerCase(Locale.ROOT);
            if (path.startsWith(KeycloakAuthenticationEntryPoint.DEFAULT_LOGIN_URI) ||
                    path.startsWith(multiTenantKeycloakApplicationProperties.getResolverBasePath().toLowerCase(Locale.ROOT))) {
                for (String realm : multiTenantKeycloakRegistration.getRealms()) {
                    if (path.equals(KeycloakAuthenticationEntryPoint.DEFAULT_LOGIN_URI + "/" + realm)  ||
                            path.equals(multiTenantKeycloakApplicationProperties.getResolverBasePath().toLowerCase(Locale.ROOT) + realm) ||
                            path.startsWith(multiTenantKeycloakApplicationProperties.getResolverBasePath().toLowerCase(Locale.ROOT) + realm + "/")) {
                        LOG.trace("found realm in path: {}", realm);
                        return multiTenantKeycloakRegistration.getRealm(realm);
                    }
                }
            }
        }
        return null;
    }

}
