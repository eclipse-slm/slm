package org.eclipse.slm.common.keycloak.config;

import com.ecwid.consul.v1.ConsulClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
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
@EnableConfigurationProperties({MultiTenantKeycloakApplicationProperties.class})
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

    private MultiTenantKeycloakApplicationProperties multiTenantKeycloakProperties;
    private ApplicationContext context;

    private Map<String, RealmResource> realmResourceMap = new HashMap<>();

    private final Optional<ConsulClient> consulClient;

    @Value("${consul.acl-token}")
    private String consulAclToken;

    @Value("${spring.cloud.consul.config.prefix:config}")
    private String consulKVPrefix;

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * Instantiates a new Multi tenant keycloak registration.
     *
     * @param multiTenantKeycloakProperties the multi tenant keycloak properties
     * @param consulClient
     */
    @Autowired
    public MultiTenantKeycloakRegistration(
            MultiTenantKeycloakApplicationProperties multiTenantKeycloakProperties,
            ApplicationContext context,
            @Nullable ConsulClient consulClient) {
        this.multiTenantKeycloakProperties = multiTenantKeycloakProperties;
        this.context = context;
        if (consulClient == null) {
            this.consulClient = null;
        }
        else {
            this.consulClient = Optional.of(consulClient);
        }
    }

    /**
     * Initialize client configurations for multiple keycloak realms (can be provided by different keycloak instances),
     * by searching for multiple configuration files with CONFIG_FILE_ENDING extension in a directory.
     * If no configuration file is found, a new file is created based on the defined Keycloak properties.
     */
    @PostConstruct
    public void init() throws IOException {
        if (multiTenantKeycloakProperties.getConfigPath().startsWith("consul:")) {
            // Load Keycloak config from Consul KV
            var kvPathPrefix = this.consulKVPrefix + "/" + this.applicationName;
            var kvSubPath = multiTenantKeycloakProperties.getConfigPath().replace("consul:", "");
            var kvPath = kvPathPrefix + "/" + kvSubPath;
            this.loadKeycloakConfigFromConsul(kvPath);
        }
        else {
            // Search in config folder for keycloak client configurations
            String keycloakConfigDirectory = "";
            if(multiTenantKeycloakProperties.getConfigPath().startsWith("/") || multiTenantKeycloakProperties.getConfigPath().startsWith(":", 1)) {
                // Absolute directory path
                keycloakConfigDirectory = multiTenantKeycloakProperties.getConfigPath();
            } else {
                // Relative directory path
                keycloakConfigDirectory = PersistentPropertiesUtil.getExecutionPath(this) + multiTenantKeycloakProperties.getConfigPath();
            }
            this.loadKeycloakConfigFromDirectory(keycloakConfigDirectory);
        }
    }

    private void loadKeycloakConfigFromDirectory(String keycloakConfigDirectory) {
        adapterConfigFiles = PersistentPropertiesUtil.findFiles(keycloakConfigDirectory, "regex:.*" + CONFIG_FILE_ENDING);
        LOG.info("Found realm configuration files: {}", adapterConfigFiles.keySet());
        // Read keycloak client configurations from files
        for (Map.Entry<String, File> entry : adapterConfigFiles.entrySet()) {
            AdapterConfig adapterConfig = PersistentPropertiesUtil.loadFromFile(entry.getValue(), AdapterConfig.class);
            this.processAdapterConfig(adapterConfig);
        }
    }

    private void loadKeycloakConfigFromConsul(String keyValuePath) throws IOException {
        if (this.consulClient.isPresent()) {
            var response = this.consulClient.get().getKVValue(keyValuePath, this.consulAclToken);
            var value = response.getValue().getDecodedValue();
            value = value.replace("'", "\"");

            var mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            var reader = mapper.readerFor(AdapterConfig.class);
            var adapterConfig = reader.readValue(value, AdapterConfig.class);
            this.processAdapterConfig(adapterConfig);
        }
        else {
            LOG.error("Load of Keycloak config from Consul configured, but no Consul client available");
        }
    }

    private void processAdapterConfig(AdapterConfig adapterConfig) {
        adapterConfig.setDisableTrustManager(true);
        String realm = adapterConfig.getRealm().toLowerCase(Locale.ROOT);

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
