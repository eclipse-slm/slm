package org.eclipse.slm.resource_management.features.capabilities.clusters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.slm.resource_management.common.location.LocationJpaRepository;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@TestConfiguration
public class ClusterHandlerITConfig {

    //region Consul Properties:
    ConsulCredential consulCredential = new ConsulCredential();
    public static String CONSUL_VERSION = "1.14";
    public static String CONSUL_HOST = "localhost";
    public static int CONSUL_PORT = 8500;
    public static String CONSUL_TOKEN = "myroot";
    //endregion

    //region Vault Properties
    VaultCredential vaultCredential = new VaultCredential();
    public static String VAULT_VERSION = "1.11.11";
    public static String VAULT_HOST = "localhost";
    public static int VAULT_PORT = 8200;
    public static String VAULT_TOKEN = "myroot";
    //endregion

    //region Mocks
    @MockBean
    LocationJpaRepository locationJpaRepository;
    //endregion


    public static void runPrepareRequest(
            Integer keycloakPort,
            Integer consulPort,
            Integer vaultPort
    ) {
        String host = "host.docker.internal";
        setupConsulJwtAuth(host, keycloakPort, consulPort);

        setupVaultJwtAuth(host, keycloakPort, vaultPort);
    }

    private static void setupVaultJwtAuth(String host, Integer keycloakPort, Integer vaultPort) {
        RestTemplate restTemplate = new RestTemplate();
        String mount_auth_path = "jwt";

        String vaultBaseUrl = "http://"+VAULT_HOST+":"+vaultPort+"/v1";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(VAULT_TOKEN);

        //region Create mount auth path:
        Map<String, String> body = Map.of("type", "jwt");

        restTemplate.exchange(
                vaultBaseUrl+"/sys/auth/"+mount_auth_path,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Void.class,
                new HashMap<>()
        );
        //endregion

        //region Set Config for auth path
        body = Map.of(
                "oidc_discovery_url", "http://"+host+":"+keycloakPort+"/realms/fabos",
                "oidc_discovery_ca_pem", "",
                "oidc_client_id", "",
                "bound_issuer", ""
        );

        restTemplate.exchange(
                vaultBaseUrl + "/auth/" + mount_auth_path + "/config",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Void.class,
                new HashMap<>()
        );
        //endregion

        //region Create default role for auth path
        Map<Object, Object> bodyRole = new HashMap<>() {{
            put("name", "default");
            put("role_type","jwt");
            put("token_ttl","3600");
            put("token_max_ttl","3600");
            put("user_claim","sub");
            put("bound_audiences", new String[] {"ui","resource-management","service-management"});
            put("claim_mappings", new HashMap<>() {{
                put("preferred_username","username");
                put("email","email");
            }});
            put("allowed_redirect_uris", new String[] {"*"});
            put("groups_claim","/realm_access/roles");
        }};

        restTemplate.exchange(
                vaultBaseUrl + "/auth/" + mount_auth_path + "/role/default",
                HttpMethod.POST,
                new HttpEntity<>(bodyRole, headers),
                Void.class,
                new HashMap<>()
        );
        //endregion
    }

    public static void setupConsulJwtAuth(String host, Integer keycloakPort, Integer consulPort) {
        RestTemplate restTemplate = new RestTemplate();
        String consulBaseUrl = "http://"+CONSUL_HOST+":"+consulPort+"/v1";

        Map<Object, Object> body = new HashMap<>() {{
            put("Name", "keycloak");
            put("Type", "jwt");
            put("Description", "FabOS Keycloak Instance");
            put("Config", new HashMap<>() {{
                put("OIDCDiscoveryURL", "http://"+host+":"+keycloakPort+"/realms/fabos");
                put("OIDCDiscoveryCACert", "");
                put("ClaimMappings", new HashMap<>() {{
                    put("family_name", "last_name");
                    put("given_name", "first_name");
                }});
                put("ListClaimMappings", new HashMap<>() {{
                    put("groups", "groups");
                    put("/realm_access/roles", "resources");
                }});
            }});
        }};
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(ClusterHandlerITConfig.CONSUL_TOKEN);
        HttpEntity<?> entity = new HttpEntity<>(body, headers);


        restTemplate.exchange(
                consulBaseUrl+"/acl/auth-method",
                HttpMethod.PUT,
                entity,
                Void.class,
                new HashMap<>()
        );
    }

    public static void updateKeycloakClientConfigFile(
            String keycloakAuthServerUrl
    ) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File keycloakClientConfigFile = new File("src/test/resources/keycloak/fabos-test-keycloak.json");

        JsonNode keycloakClientConfig = objectMapper.readTree(keycloakClientConfigFile);
        ((ObjectNode) keycloakClientConfig).put("auth-server-url", keycloakAuthServerUrl);

        objectMapper.writeValue(keycloakClientConfigFile, keycloakClientConfig);
    }

    public static void setKeycloakServiceAccountRoles(
            String keycloakAuthServerUrl,
            String adminUser,
            String adminPassword
    ) throws URISyntaxException {
        String targetRealm = "fabos";
        String clientId = "resource-management";
        String clientIdRealmManagement = "realm-management";
        String roleName = "realm-admin";

        URI authorizationURI = new URIBuilder(keycloakAuthServerUrl + "realms/master/protocol/openid-connect/token").build();
        WebClient webclient = WebClient.builder().build();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.put("grant_type", Collections.singletonList("password"));
        formData.put("client_id", Collections.singletonList("admin-cli"));
        formData.put("username", Collections.singletonList(adminUser));
        formData.put("password", Collections.singletonList(adminPassword));

        AccessTokenResponse adminAccessToken = webclient.post()
                .uri(authorizationURI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(AccessTokenResponse.class)
                .block();

        webclient = WebClient.builder()
                .defaultHeaders(h -> h.setBearerAuth(adminAccessToken.getToken()))
                .build();

        URI clientURI = new URIBuilder(keycloakAuthServerUrl + "admin/realms/"+targetRealm+"/clients?clientId="+clientId).build();

        ClientRepresentation[] keycloakClient = webclient.get()
                .uri(clientURI)
                .retrieve()
                .bodyToMono(ClientRepresentation[].class)
                .block();

        URI serviceAccountUserURI = new URIBuilder(keycloakAuthServerUrl + "admin/realms/"+targetRealm+"/clients/"+keycloakClient[0].getId()+"/service-account-user").build();

        UserRepresentation serviceAccountUser = webclient.get()
                .uri(serviceAccountUserURI)
                .retrieve()
                .bodyToMono(UserRepresentation.class)
                .block();


        URI clientRealmManagementURI = new URIBuilder(keycloakAuthServerUrl + "admin/realms/"+targetRealm+"/clients?clientId="+clientIdRealmManagement).build();

        ClientRepresentation[] keycloakClientRealmManagement = webclient.get()
                .uri(clientRealmManagementURI)
                .retrieve()
                .bodyToMono(ClientRepresentation[].class)
                .block();

        URI clientRoleURI = new URIBuilder(keycloakAuthServerUrl + "admin/realms/"+targetRealm+"/clients/"+keycloakClientRealmManagement[0].getId()+"/roles?search="+roleName).build();

        RoleRepresentation[] clientRole = webclient.get()
                .uri(clientRoleURI)
                .retrieve()
                .bodyToMono(RoleRepresentation[].class)
                .block();

        URI userRoleMappingURI = new URIBuilder(keycloakAuthServerUrl + "admin/realms/"+targetRealm+"/users/"+serviceAccountUser.getId()+"/role-mappings/clients/"+keycloakClientRealmManagement[0].getId()).build();

        List<Map<String, String>> body = Arrays.asList(new HashMap<>(){{
            put("id",   clientRole[0].getId());
            put("name", clientRole[0].getName());
        }});

        String mappingResponse = webclient.post()
                .uri(userRoleMappingURI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
