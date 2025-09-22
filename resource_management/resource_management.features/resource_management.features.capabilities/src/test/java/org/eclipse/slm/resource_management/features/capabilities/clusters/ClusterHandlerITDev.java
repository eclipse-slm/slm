package org.eclipse.slm.resource_management.features.capabilities.clusters;

import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.eclipse.slm.common.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.notification_service.messaging.NotificationMessageSender;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.clusters.handler.*;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.Cluster;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.ClusterCreateRequest;
import org.eclipse.slm.resource_management.features.capabilities.clusters.handler.*;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
import org.eclipse.slm.resource_management.common.resources.BasicResource;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@Import(ClusterHandlerITConfig.class)
@Testcontainers
@Disabled
public class ClusterHandlerITDev {
    //region Autowiring
    @Autowired
    ClusterHandlerITConfig config;
    @Autowired
    ClusterHandler clusterHandler;
    @SpyBean
    ClusterCreateFunctions clusterCreateFunctions;
    @Autowired
    ClusterDeleteFunctions clusterDeleteFunctions;
    @Autowired
    ClusterScaleFunctions clusterScaleFunctions;
    @Autowired
    ClusterGetFunctions clusterGetFunctions;
    @Autowired
    VaultClient vaultClient;
    @Autowired
    KeycloakAdminClient keycloakAdminClient;
    @Autowired
    ObjectMapper objectMapper;
    static ResourcesConsulClient resourcesConsulClient;
    //endregion
    @Value("${local.server.port}")
    private int localServerPort;

    //region Mocks
//    @Autowired
//    MockMvc mockMvc;

    @MockBean
    AwxJobExecutor awxJobExecutor;
    @MockBean
    NotificationMessageSender NotificationMessageSender;
    @MockBean
    AwxJobObserverInitializer awxJobObserverInitializer;
    @MockBean
    AwxJobObserver awxJobObserver;
    @MockBean
    CapabilityJpaRepository capabilityJpaRepository;
    //endregion

    //region TestContainer
    @Container
    static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:19.0.3")
            .withExposedPorts(8080)
            .withAdminUsername("admin")
            .withAdminPassword("password")
            .withRealmImportFile("realm-export.json")
            .waitingFor(new HostPortWaitStrategy());

    @Container
    static GenericContainer<?> consulDockerContainer = new GenericContainer<>(DockerImageName.parse("consul:" + ClusterHandlerITConfig.CONSUL_VERSION))
            .withExposedPorts(ClusterHandlerITConfig.CONSUL_PORT)
            .withEnv("CONSUL_LOCAL_CONFIG", "{ \"datacenter\": \"fabos\", \"domain\": \".fabos\", \"bind_addr\": \"0.0.0.0\", \"retry_join\": [\"0.0.0.0\"], \"acl\": { \"enabled\": true, \"default_policy\": \"allow\", \"tokens\": { \"master\": \""+ClusterHandlerITConfig.CONSUL_TOKEN+"\" } } }")
            .waitingFor(new HostPortWaitStrategy());


    @DynamicPropertySource
    static void consulProperties(DynamicPropertyRegistry registry) {
        registry.add("consul.port", consulDockerContainer::getFirstMappedPort);
        registry.add("consul.host", () -> ClusterHandlerITConfig.CONSUL_HOST);
        registry.add("consul.acl-token", () -> ClusterHandlerITConfig.CONSUL_TOKEN);
    }

    @Container
    static GenericContainer<?> vaultDockerContainer = new GenericContainer<>(DockerImageName.parse("vault:"+ClusterHandlerITConfig.VAULT_VERSION))
            .withExposedPorts(ClusterHandlerITConfig.VAULT_PORT)
            .withEnv("VAULT_DEV_ROOT_TOKEN_ID", ClusterHandlerITConfig.VAULT_TOKEN)
            .waitingFor(new HostPortWaitStrategy());

    @DynamicPropertySource
    static void vaultProperties(DynamicPropertyRegistry registry) {
        registry.add("vault.port", vaultDockerContainer::getFirstMappedPort);
        registry.add("vault.host", () -> ClusterHandlerITConfig.VAULT_HOST);
        registry.add("vault.token", () -> ClusterHandlerITConfig.VAULT_TOKEN);
        registry.add("vault.authentication", () -> "token");
    }
    //endregion

    @Autowired
    public ClusterHandlerITDev(
            MultiTenantKeycloakRegistration multiTenantKeycloakRegistration,
            ResourcesConsulClient resourcesConsulClient
    ) throws IOException {
        ClusterHandlerITDev.resourcesConsulClient = resourcesConsulClient;
        multiTenantKeycloakRegistration.init();
    }

//    @Before
//    public void before() {
//        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
//    }

    @BeforeAll
    public static void beforeAll() throws IOException, URISyntaxException {
        ClusterHandlerITConfig.runPrepareRequest(
                keycloak.getFirstMappedPort(),
                consulDockerContainer.getFirstMappedPort(),
                vaultDockerContainer.getFirstMappedPort()
        );

        ClusterHandlerITConfig.updateKeycloakClientConfigFile(
                keycloak.getAuthServerUrl()
        );

        ClusterHandlerITConfig.setKeycloakServiceAccountRoles(
                keycloak.getAuthServerUrl(),
                "admin",
                "password"
        );
    }

    @Nested
    @Order(10)
    public class preTests {
        @Test
        public void testInjectedConsulInstances() {
            assertNotNull(clusterHandler);
            assertNotNull(clusterCreateFunctions);
            assertNotNull(clusterDeleteFunctions);
            assertNotNull(clusterScaleFunctions);
            assertNotNull(clusterGetFunctions);
        }
    }

    @Nested
    @Order(20)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class testClusterCrudMethods {

        private MultiHostCapabilityService capService =
                ClusterHandlerITConfigDockerSwarmCapability.getCapabilityService();
        ClusterCreateRequest clusterCreateRequest = ClusterHandlerITConfigDockerSwarmCapability.getClusterCreateRequest();


        @BeforeAll
        public static void beforeAll() throws ConsulLoginFailedException, URISyntaxException {
            for(BasicResource basicResource : ClusterHandlerITConfigDockerSwarmCapability.clusterMembers) {
                resourcesConsulClient.addResource(basicResource);
            }
        }



        private static AccessTokenResponse getKeycloakAccessToken() throws URISyntaxException {
            String keyloakAuthServer = keycloak.getAuthServerUrl();

            URI authorizationURI = new URIBuilder(keyloakAuthServer + "/realms/fabos/protocol/openid-connect/token").build();
            WebClient webclient = WebClient.builder().build();
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.put("grant_type", Collections.singletonList("password"));
            formData.put("client_id", Collections.singletonList("ui"));
            formData.put("username", Collections.singletonList("fabos"));
            formData.put("password", Collections.singletonList("password"));

            AccessTokenResponse accessTokenResponse = webclient.post()
                    .uri(authorizationURI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(AccessTokenResponse.class)
                    .block();

            return accessTokenResponse;
        }

        @Test
        @Order(10)
        public void testGetClusterExpectNoResults() {
            List<Cluster> clusters = clusterHandler.getClusters(config.consulCredential);

            assertEquals(0, clusters.size());
        }

        @Test
        @Order(20)
        public void testCreateCluster() throws Exception {
            AccessTokenResponse accessToken = getKeycloakAccessToken();
//            String body = objectMapper.writeValueAsString(clusterCreateRequest);

//            MvcResult mvcResult = mockMvc.perform(post("/resources/clusters")
//                            .with(csrf())
//                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getToken())
//                            .header("Realm", "fabos")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(body)
//                    )
//                    .andDo(MockMvcResultHandlers.print())
//                    .andReturn();
//
//            System.out.println( mvcResult.getResponse().getContentAsString() );

            WebClient webclient = WebClient.builder()
                    .defaultHeaders(h -> h.setBearerAuth(accessToken.getToken()))
                    .build();



            URI clusterCreateURI = new URIBuilder("http://localhost:"+localServerPort+"/resources/clusters").build();

            String result = webclient.post()
                    .uri(clusterCreateURI)
                    .body(BodyInserters.fromValue(clusterCreateRequest))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();


//            KeycloakPrincipal keycloakPrincipal = getKeycloakPrincipal();

//            awxJobObserver.jobGoal = JobGoal.CREATE;
//            ClusterJob clusterCreateJob = new ClusterJob(
//                    capService,
//                    awxJobObserver,
//                    null,
//                    keycloakPrincipal,
//                    clusterCreateRequest
//            );
//
//            //region Spies / Mocks
//            doReturn(clusterCreateJob).when(clusterCreateFunctions).startInstallAction(
//                    capService,
//                    keycloakPrincipal,
//                    clusterCreateRequest
//            );
//
//            Mockito.when(capabilityJpaRepository.findById(Mockito.any()))
//                    .thenReturn(Optional.of(ClusterHandlerITConfigDockerSwarmCapability.get()));
//            //endregion
//
//            clusterHandler.create(
//                    capService,
//                    keycloakPrincipal,
//                    clusterCreateRequest
//            );
//
//            clusterCreateFunctions.onJobStateFinished(
//                    awxJobObserver,
//                    JobFinalState.SUCCESSFUL
//            );
//
//            //region Assert
//            List<Cluster> clusters = clusterHandler.getClusters(config.consulCredential);
//
//            assertEquals(1, clusters.size());
//            //endregion
        }
    }
}
