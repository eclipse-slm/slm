package org.eclipse.slm.common.keycloak.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.representations.idm.ClientRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
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
@EnableConfigurationProperties({MultiTenantKeycloakApplicationProperties.class, KeycloakSpringBootProperties.class})
//@PropertySource("classpath:application.yml")
public class MultiTenantKeycloakRegistration {

    private static final Logger LOG = LoggerFactory.getLogger(MultiTenantKeycloakRegistration.class);

    private static final int INTERVAL = 2000;
    private static final int WAIT_TIME_KEYCLOAK = 24000;
    private static final int MAX_RETRY_LOAD_CLIENT = 10;
    private static final String CONFIG_FILE_ENDING = "-keycloak.json";
    private static final String CREDENTIAL_INITIAL_ACCESS_TOKEN = "initial-access-token";
    private static final String CREDENTIAL_REGISTRATION_ACCESS_TOKEN = "registration-access-token";
    private static final String CREDENTIAL_SECRET = "secret";

    private final Object lock = new Object();

    private static final Map<String, KeycloakDeployment> cache = new HashMap<>();

    private final List<String> realms = new ArrayList<>();
    private Map<String, File> adapterConfigFiles;

    @Value("${server.port}")
    private int port;

    private KeycloakSpringBootProperties keycloakSpringBootProperties;
    private MultiTenantKeycloakApplicationProperties multiTenantKeycloakProperties;
    private ApplicationContext context;

    private Map<String, RealmResource> realmResourceMap = new HashMap<>();

    /**
     * Instantiates a new Multi tenant keycloak registration.
     *
     * @param keycloakSpringBootProperties  the keycloak spring boot properties
     * @param multiTenantKeycloakProperties the multi tenant keycloak properties
     */
    @Autowired
    public MultiTenantKeycloakRegistration(
            KeycloakSpringBootProperties keycloakSpringBootProperties,
            MultiTenantKeycloakApplicationProperties multiTenantKeycloakProperties,
            ApplicationContext context
    ) {
        this.keycloakSpringBootProperties = keycloakSpringBootProperties;
        this.multiTenantKeycloakProperties = multiTenantKeycloakProperties;
        this.context = context;
    }


    /**
     * Initialize client configurations for multiple keycloak realms (can be provided by different keycloak instances),
     * by searching for multiple configuration files with CONFIG_FILE_ENDING extension in a directory.
     * If no configuration file is found, a new file is created based on the defined Keycloak properties.
     */
    @PostConstruct
    public void init() {
        //multiTenantKeycloakProperties = new MultiTenantKeycloakProperties(keycloakSpringBootProperties);
        Map<String, AdapterConfig> adapterConfigs = new HashMap<>();
        // search in config folder for keycloak client configurations
        String keycloakConfigDirectory;
        if(multiTenantKeycloakProperties.getConfigPath().startsWith("/") || multiTenantKeycloakProperties.getConfigPath().startsWith(":", 1)) {
            // absolute path
            keycloakConfigDirectory = multiTenantKeycloakProperties.getConfigPath();
        } else {
            // relative path
            keycloakConfigDirectory = PersistentPropertiesUtil.getExecutionPath(this) + multiTenantKeycloakProperties.getConfigPath();
        }
        adapterConfigFiles = PersistentPropertiesUtil.findFiles(keycloakConfigDirectory, "regex:.*" + CONFIG_FILE_ENDING);
        LOG.info("Found realm configuration files: {}", adapterConfigFiles.keySet());
        // read keycloak client configurations from files
        for (Map.Entry<String, File> entry : adapterConfigFiles.entrySet()) {
            AdapterConfig adapterConfig = PersistentPropertiesUtil.loadFromFile(entry.getValue(), AdapterConfig.class);
            adapterConfig.setDisableTrustManager(true);
            // TODO: Use trust store to verify certificates of Keycloak servers
            // adapterConfig.setTruststore("classpath:truststore.p12");
            // adapterConfig.setTruststorePassword("password");
            String realm = adapterConfig.getRealm().toLowerCase(Locale.ROOT);
            adapterConfigs.put(realm, adapterConfig);

            LOG.info("Wait for keycloak instance and initialize realm configuration: {}", realm);
            var keycloakDeployment = KeycloakDeploymentBuilder.build(adapterConfig);
            if (keycloakDeployment != null)
            {
                cache.put(adapterConfig.getRealm().toLowerCase(Locale.ROOT), keycloakDeployment);
                realms.add(realm);
                LOG.info("Client configuration initialized for realm '{}'", realm);

                Keycloak keycloak = KeycloakBuilder.builder()
                        .serverUrl(adapterConfig.getAuthServerUrl())
                        .realm(realm)
                        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                        .clientId(adapterConfig.getResource())
                        .clientSecret((String) adapterConfig.getCredentials().get("secret"))
                        .build();

                this.realmResourceMap.put(realm, keycloak.realm(realm) );
            }
            else {
                LOG.error("Failed to initialize client configuration for realm '{}', therefore application is stopped", realm);
                System.exit(SpringApplication.exit(context, () -> 666));
            }
        }
    }

    /**
     * Wait until keycloak instance is up and running by periodically checking the reachability of the URL.
     *
     * @param url the keycloak url
     */
    private void waitForKeycloakConnectivity(String url) {
        try {
            boolean isReachable = ConnectivityCheckUtil.isUrlAvailable(url);
            if (!isReachable) {
                synchronized (lock) {
                    while (!isReachable) {
                        isReachable = ConnectivityCheckUtil.isUrlAvailable(url);
                        lock.wait(INTERVAL);
                    }
                }
            }
        } catch (InterruptedException e) {
            LOG.warn("Wait time to check keycloak connectivity interrupted", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

    private void setServiceRedirectUris(ClientRepresentation client) {
        if (client.getRedirectUris() == null) {
            client.setRedirectUris(new ArrayList<>());
        }
        String[] s;
        if (multiTenantKeycloakProperties.getRedirectUris() != null && multiTenantKeycloakProperties.getRedirectUris().length > 0) {
            s = multiTenantKeycloakProperties.getRedirectUris();
        } else {
            s = new String[]{String.format("http://%s:%s/*", ConnectivityCheckUtil.getIpAddress(), port),
                    String.format("http://localhost:%s/*", port),
                    String.format("http://127.0.0.1:%s/*", port)};
        }
        Arrays.asList(s).forEach(serviceUri -> {
                    if (!client.getRedirectUris().contains(serviceUri)) {
                        client.getRedirectUris().add(serviceUri);
                    }
                }
        );
    }

    private void setServiceWebOrigins(ClientRepresentation client) {
        if (client.getWebOrigins() == null) {
            client.setWebOrigins(new ArrayList<>());
        }
        String[] s;
        if (multiTenantKeycloakProperties.getWebOrigins() != null && multiTenantKeycloakProperties.getWebOrigins().length > 0) {
            s = multiTenantKeycloakProperties.getWebOrigins();
        } else {
            s = new String[]{String.format("http://%s:%s", ConnectivityCheckUtil.getIpAddress(), port),
                    String.format("http://localhost:%s", port),
                    String.format("http://127.0.0.1:%s", port)};
        }
        Arrays.asList(s).forEach(serviceUri -> {
                    if (!client.getWebOrigins().contains(serviceUri)) {
                        client.getWebOrigins().add(serviceUri);
                    }
                }
        );
    }

    private void saveAdapterConfig(AdapterConfig adapterConfig) {
        File configFile = adapterConfigFiles.get(adapterConfig.getRealm().toLowerCase(Locale.ROOT) + CONFIG_FILE_ENDING);
        PersistentPropertiesUtil.saveToFile(configFile, adapterConfig);
    }

    private void updateRegistrationToken(AdapterConfig adapterConfig, ClientRepresentation clientRepresentation) {
        String registrationAccessToken = clientRepresentation.getRegistrationAccessToken();
        adapterConfig.getCredentials().put(CREDENTIAL_REGISTRATION_ACCESS_TOKEN, registrationAccessToken);
        saveAdapterConfig(adapterConfig);
    }

    /**
     * Get first realm keycloak deployment.
     *
     * @return the keycloak deployment
     */
    public KeycloakDeployment getFirstRealm() {
        Optional<Map.Entry<String, KeycloakDeployment>> optional = cache.entrySet().stream().findFirst();
        if (optional.isPresent()) {
            return optional.get().getValue();
        }
        return null;
    }

    /**
     * Get realm keycloak deployment.
     *
     * @param realm the realm
     * @return the keycloak deployment
     */
    public KeycloakDeployment getRealm(String realm) {
        return cache.get(realm.toLowerCase(Locale.ROOT));
    }

    /**
     * Contains realm boolean.
     *
     * @param realm the realm
     * @return the boolean
     */
    public boolean containsRealm(String realm) {
        return cache.containsKey(realm.toLowerCase(Locale.ROOT));
    }

    /**
     * Get realm count int.
     *
     * @return the int
     */
    public int getRealmCount() {
        return cache.size();
    }

    /**
     * Get realms list.
     *
     * @return the list
     */
    public List<String> getRealms() {
        return realms;
    }

    public boolean isRealmConfigured(String authUrl)
    {
        for (var keycloakDeployment : cache.values())
        {
            if (keycloakDeployment.getAuthUrl().build().toString().contains(authUrl))
            {
                return true;
            }
        }

        return false;
    }

    public RealmResource getRealmResource(String realmName) {
        RealmResource realm = realmResourceMap.get(realmName);
        return realmResourceMap.get(realmName);
    }
}
