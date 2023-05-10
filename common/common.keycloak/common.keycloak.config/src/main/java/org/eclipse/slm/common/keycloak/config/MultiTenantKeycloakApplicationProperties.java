package org.eclipse.slm.common.keycloak.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationEntryPoint;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * The type Multi tenant keycloak properties.
 *
 * @author des
 */
@ConfigurationProperties("keycloak.config.tenants")
public class MultiTenantKeycloakApplicationProperties {

    /**
     * Enable path based resolution for keycloak deployment.
     */
    private boolean resolveByPath = true;

    /**
     * Enable header based resolution for keycloak deployment.
     */
    private boolean resolveByHeader = true;

    /**
     * Base path for path based resolution for keycloak deployment.
     */
    private String resolverBasePath = KeycloakAuthenticationEntryPoint.DEFAULT_LOGIN_URI;

    /**
     * Header for header based resolution for keycloak deployment.
     */
    private String resolverHeader = "Realm";

    /**
     * Lookup path for keycloak deployment configurations, cloud be an absolute or relative path.
     */
    private String configPath = "config/";

    /**
     * Enable automatic client registration for keycloak deployment at start up.
     */
    private boolean clientRegistrationEnable = false;

    /**
     * Enable automatic client update for keycloak deployment at start up.
     */
    private boolean clientUpdateEnable = false;

    /**
     * Set redirect uris for client registration.
     */
    private String[] redirectUris;

    /**
     * Set web origins for client registration.
     */
    private String[] webOrigins;

    /**
     * Instantiates a new Multi tenant keycloak properties.
     */
    public MultiTenantKeycloakApplicationProperties(){}

    /**
     * Instantiates a new Multi tenant keycloak properties.
     *
     * @param keycloakSpringBootProperties the keycloak spring boot properties
     */
    public MultiTenantKeycloakApplicationProperties(KeycloakSpringBootProperties keycloakSpringBootProperties){
        Map map = (Map) keycloakSpringBootProperties.getConfig().get("tenants");
        setResolveByPath(Boolean.parseBoolean((String) map.get("resolve-by-path")));
        setResolveByHeader(Boolean.parseBoolean((String) map.get("resolve-by-header")));
        setResolverBasePath((String) map.get("resolver-base-path"));
        setResolverHeader((String) map.get("resolver-header"));
        setConfigPath((String) map.get("config-path"));
        setClientRegistrationEnable(Boolean.parseBoolean((String) map.get("client-registration-enable")));
        setClientUpdateEnable(Boolean.parseBoolean((String) map.get("client-update-enable")));
        setRedirectUris((String[]) map.get("redirect-uris"));
        setWebOrigins((String[]) map.get("web-origins"));
    }

    /**
     * Is resolve by path boolean.
     *
     * @return the boolean
     */
    public boolean isResolveByPath() {
        return resolveByPath;
    }

    /**
     * Sets resolve by path.
     *
     * @param resolveByPath the resolve by path
     */
    public void setResolveByPath(boolean resolveByPath) {
        this.resolveByPath = resolveByPath;
    }

    /**
     * Is resolve by header boolean.
     *
     * @return the boolean
     */
    public boolean isResolveByHeader() {
        return resolveByHeader;
    }

    /**
     * Sets resolve by header.
     *
     * @param resolveByHeader the resolve by header
     */
    public void setResolveByHeader(boolean resolveByHeader) {
        this.resolveByHeader = resolveByHeader;
    }

    /**
     * Gets resolver base path.
     *
     * @return the resolver base path
     */
    public String getResolverBasePath() {
        return resolverBasePath;
    }

    /**
     * Sets resolver base path.
     *
     * @param resolverBasePath the resolver base path
     */
    public void setResolverBasePath(String resolverBasePath) {
        this.resolverBasePath = resolverBasePath;
    }

    /**
     * Gets resolver header.
     *
     * @return the resolver header
     */
    public String getResolverHeader() {
        return resolverHeader;
    }

    /**
     * Sets resolver header.
     *
     * @param resolverHeader the resolver header
     */
    public void setResolverHeader(String resolverHeader) {
        this.resolverHeader = resolverHeader;
    }

    /**
     * Gets config path.
     *
     * @return the config path
     */
    public String getConfigPath() {
        return configPath;
    }

    /**
     * Sets config path.
     *
     * @param configPath the config path
     */
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    /**
     * Is client registration enable boolean.
     *
     * @return the boolean
     */
    public boolean isClientRegistrationEnable() {
        return clientRegistrationEnable;
    }

    /**
     * Sets client registration enable.
     *
     * @param clientRegistrationEnable the client registration enable
     */
    public void setClientRegistrationEnable(boolean clientRegistrationEnable) {
        this.clientRegistrationEnable = clientRegistrationEnable;
    }

    /**
     * Is client update enable boolean.
     *
     * @return the boolean
     */
    public boolean isClientUpdateEnable() {
        return clientUpdateEnable;
    }

    /**
     * Sets client update enable.
     *
     * @param clientUpdateEnable the client update enable
     */
    public void setClientUpdateEnable(boolean clientUpdateEnable) {
        this.clientUpdateEnable = clientUpdateEnable;
    }

    /**
     * Get redirect uris string [ ].
     *
     * @return the string [ ]
     */
    public String[] getRedirectUris() {
        return redirectUris;
    }

    /**
     * Sets redirect uris.
     *
     * @param redirectUris the redirect uris
     */
    public void setRedirectUris(String[] redirectUris) {
        this.redirectUris = redirectUris;
    }

    /**
     * Get web origins string [ ].
     *
     * @return the string [ ]
     */
    public String[] getWebOrigins() {
        return webOrigins;
    }

    /**
     * Sets web origins.
     *
     * @param webOrigins the web origins
     */
    public void setWebOrigins(String[] webOrigins) {
        this.webOrigins = webOrigins;
    }
}
