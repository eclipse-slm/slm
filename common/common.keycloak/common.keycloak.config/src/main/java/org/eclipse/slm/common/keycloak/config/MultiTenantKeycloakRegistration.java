package org.eclipse.slm.common.keycloak.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.slm.common.consul.client.ConsulClient;
import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.keycloak.config.jwt.IssuerProperties;
import org.eclipse.slm.common.keycloak.config.jwt.MisconfigurationException;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * The multi-tenant keycloak registration resolves all keycloak configurations within a directory based on
 * the search pattern '*-keycloak.json' or creates one based on the application properties if missing.
 * If enabled, the resolved keycloak configuration is registered or updated with the configured keycloak instance.
 * Otherwise, the keycloak configurations are cached only for the MultiTenantKeycloakConfigResolver.
 *
 * @author des
 */
@Component
public class MultiTenantKeycloakRegistration {

    private static final Logger LOG = LoggerFactory.getLogger(MultiTenantKeycloakRegistration.class);

    private static final String CONFIG_FILE_ENDING = "-keycloak.json";

    private static final Map<String, KeycloakOidcConfig> keycloakOidcConfigs = new HashMap<>();

    @Value("${keycloak.tenants.config-path}")
    private String keycloakConfigPath;

    private final List<String> realms = new ArrayList<>();

    private Map<String, File> oidcConfigFiles;

    private Map<String, Keycloak> keycloakClientMap = new HashMap<>();

    private Optional<ConsulClient> consulClient;

    @Value("${spring.cloud.consul.config.prefix:config}")
    private String consulKVPrefix;

    @Value("${spring.application.name}")
    private String applicationName;

    private List<IssuerProperties> issuers = new ArrayList<>();

    /**
     * Instantiates a new Multi tenant keycloak registration.
     *
     * @param consulClientFactory
     */
    @Autowired
    public MultiTenantKeycloakRegistration(ConsulClientFactory consulClientFactory) {
        if (consulClientFactory == null) {
            this.consulClient = Optional.empty();
        }
        else {
            this.consulClient = Optional.of(consulClientFactory.createAdminClient());
        }
    }

    /**
     * Initialize client configurations for multiple keycloak realms (can be provided by different keycloak instances),
     * by searching for multiple configuration files with CONFIG_FILE_ENDING extension in a directory.
     * If no configuration file is found, a new file is created based on the defined Keycloak properties.
     */
    @PostConstruct
    public void init() throws IOException {
        if (keycloakConfigPath.startsWith("consul:")) {
            // Load Keycloak config from Consul KV
            var kvPathPrefix = this.consulKVPrefix + "/" + this.applicationName;
            var kvSubPath = keycloakConfigPath.replace("consul:", "");
            var kvPath = kvPathPrefix + "/" + kvSubPath;
            this.loadKeycloakConfigFromConsul(kvPath);
        }
        else {
            // Search in config folder for keycloak client configurations
            String keycloakConfigDirectory = "";
            if(keycloakConfigPath.startsWith("/") || keycloakConfigPath.startsWith(":", 1)) {
                // Absolute directory path
                keycloakConfigDirectory = keycloakConfigPath;
            } else {
                // Relative directory path
                keycloakConfigDirectory = PersistentPropertiesUtil.getExecutionPath(this) + keycloakConfigPath;
            }
            this.loadKeycloakConfigFromDirectory(keycloakConfigDirectory);
        }
    }

    private void loadKeycloakConfigFromDirectory(String keycloakConfigDirectory) {
        oidcConfigFiles = PersistentPropertiesUtil.findFiles(keycloakConfigDirectory, "regex:.*" + CONFIG_FILE_ENDING);
        LOG.info("Found realm configuration files: {}", oidcConfigFiles.keySet());
        // Read keycloak client configurations from files
        for (Map.Entry<String, File> entry : oidcConfigFiles.entrySet()) {
            var keycloakOidcConfig = PersistentPropertiesUtil.loadFromFile(entry.getValue(), KeycloakOidcConfig.class);
            this.processOidcConfig(keycloakOidcConfig);
        }
    }

    private void loadKeycloakConfigFromConsul(String keyValuePath) throws IOException {
        if (this.consulClient.isPresent()) {
            var response = this.consulClient.get().kv().readKey(keyValuePath, false, false, false, null);
            var value = response.get(0).getDecodedValue();
            value = value.replace("'", "\"");

            var mapper = new ObjectMapper()
                    .registerModule(new KotlinModule.Builder()
                            .nullIsSameAsDefault(true)
                            .build());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            var reader = mapper.readerFor(KeycloakOidcConfig.class);
            var keycloakOidcConfig = reader.readValue(value, KeycloakOidcConfig.class);
            this.processOidcConfig(keycloakOidcConfig);
        }
        else {
            LOG.error("Load of Keycloak config from Consul configured, but no Consul client available");
        }
    }

    private void processOidcConfig(KeycloakOidcConfig keycloakOidcConfig) {
        var realm = keycloakOidcConfig.getRealm().toLowerCase(Locale.ROOT);

        keycloakOidcConfigs.put(realm, keycloakOidcConfig);
        realms.add(realm);
        LOG.info("Client configuration initialized for realm '{}'", realm);

        try {
            Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakOidcConfig.getAuthServerUrl())
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(keycloakOidcConfig.getResource())
                .clientSecret(keycloakOidcConfig.getCredentials().getSecret())
                .build();
            this.keycloakClientMap.put(realm, keycloak);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing Keycloak client for realm '" + realm + "'", e);
        }

        var issuerProperties = new IssuerProperties();
        try {
            issuerProperties.setUri(new URL(keycloakOidcConfig.getAuthServerUrlIncludingRealm()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        this.issuers.add(issuerProperties);
    }

    /**
     * Get first Keycloak OIDC config.
     *
     * @return the keycloak ODIC config
     */
    public KeycloakOidcConfig getFirstOidcConfig() {
        var optionalKeycloakOidcConfig = keycloakOidcConfigs.entrySet().stream().findFirst();
        if (optionalKeycloakOidcConfig.isPresent()) {
            return optionalKeycloakOidcConfig.get().getValue();
        }
        return null;
    }

    /**
     * Get realms list.
     *
     * @return the list
     */
    public List<String> getRealms() {
        return realms;
    }

    public RealmResource getRealmResource(String realmName) {
        Keycloak keycloak = keycloakClientMap.get(realmName);
        if (keycloak == null) {
            throw new IllegalStateException("No Keycloak client for realm: " + realmName);
        }
        return keycloak.realm(realmName);
    }

    public List<IssuerProperties> getIssuers() {
        return issuers;
    }

    public void setIssuers(List<IssuerProperties> issuers) {
        this.issuers = issuers;
    }

    public IssuerProperties getIssuerProperties(URL issuerUri) throws MisconfigurationException {
        final var issuerProperties = issuers.stream().filter(iss -> issuerUri.equals(iss.getUri())).toList();
        if (issuerProperties.isEmpty()) {
            throw new MisconfigurationException("Missing authorities mapping properties for %s".formatted(issuerUri.toString()));
        }
        if (issuerProperties.size() > 1) {
            throw new MisconfigurationException("Too many authorities mapping properties for %s".formatted(issuerUri.toString()));
        }
        return issuerProperties.get(0);
    }

    public String getDefaultRealm() {
        var optionalKeycloakOidcConfig = keycloakOidcConfigs.entrySet().stream().findFirst();
        if (optionalKeycloakOidcConfig.isPresent()) {
            return optionalKeycloakOidcConfig.get().getValue().getRealm();
        }
        return null;
    }

    @PreDestroy
    public void closeKeycloakClients() {
        for (Keycloak keycloak : keycloakClientMap.values()) {
            try {
                keycloak.close();
            } catch (Exception e) {
                LOG.warn("Error closing Keycloak client", e);
            }
        }
    }
}
