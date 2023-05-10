package org.eclipse.slm.resource_management.service.rest.capabilities;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.model.capabilities.CapabilityFilter;
import org.eclipse.slm.resource_management.model.capabilities.CapabilityType;
import org.eclipse.slm.resource_management.model.capabilities.DeploymentCapability;
import org.eclipse.slm.resource_management.model.capabilities.VirtualizationCapability;
import org.eclipse.slm.resource_management.model.capabilities.actions.AwxCapabilityAction;
import org.eclipse.slm.resource_management.model.capabilities.actions.CapabilityActionType;
import org.eclipse.slm.resource_management.model.cluster.ClusterMemberType;
import org.eclipse.slm.resource_management.service.rest.Application;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesManager;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesRestController;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        Application.class,
        ResourcesRestController.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ComponentScan(basePackages = {
        "org.eclipse.slm.common.consul.client",
        "org.eclipse.slm.common.vault.client",
        "org.eclipse.slm.common.awx.client",
        "org.eclipse.slm.notification_service.service.client",
        "org.eclipse.slm.common.keycloak.config",
        "org.eclipse.slm.resource_management",
        "org.eclipse.slm.resource_management.service.rest"
})
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class CapabilityRestControllerTest {

    //region Variables
    public final static String BASE_PATH = "/resources/capabilities";
    public static String BASE_URL = "";
    @MockBean
    CapabilitiesManager capabilitiesManager;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private ResourcesRestController controller;
    @Autowired
    private ResourcesManager resourcesManager;
    @Autowired
    ObjectMapper objectMapper;
    private static MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private static DeploymentCapability dockerDeploymentCapability;
    private static DeploymentCapability dockerSwarmDeploymentCapability;
    private static VirtualizationCapability kvmQemuVirtualizationCapability;
    //endregion

    //region Functions
    private String getMultiHostDeploymentCapabilityDTOAsJsonString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(dockerSwarmDeploymentCapability);
    }

    private String getSingleHostDeploymentCapabilityDTOAsJsonString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(dockerDeploymentCapability);
    }

    private String getSingleHostVirtualizationCapabilityDTOAsJsonString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(kvmQemuVirtualizationCapability);
    }
    //endregion

    @BeforeAll
    public static void beforeAll(@Value("${local.server.port:80}") int port) {
        BASE_URL = "http://localhost:"+port+BASE_PATH;

        //region Create Java Object of Docker DeploymentCapability:
        dockerDeploymentCapability = new DeploymentCapability();
        dockerDeploymentCapability.setName("Docker");
        dockerDeploymentCapability.setLogo("mdi-docker");
        dockerDeploymentCapability.setType(Arrays.asList(
                CapabilityType.SETUP,
                CapabilityType.DEPLOY
        ));
        dockerDeploymentCapability.setCapabilityClass("DeploymentCapability");

        dockerDeploymentCapability.setSupportedDeploymentTypes(Arrays.asList(
                DeploymentType.DOCKER_CONTAINER,
                DeploymentType.DOCKER_COMPOSE
        ));

        // Set AWX Capability Actions
        var dockerDeploymentCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-dc-docker.git";
        var dockerDeploymentCapabilityBranch = "1.0.0";
        dockerDeploymentCapability.getActions()
                .put(CapabilityActionType.INSTALL, new AwxCapabilityAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "install.yml"));
        dockerDeploymentCapability.getActions()
                .put(CapabilityActionType.UNINSTALL, new AwxCapabilityAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "uninstall.yml"));
        dockerDeploymentCapability.getActions()
                .put(CapabilityActionType.DEPLOY, new AwxCapabilityAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "deploy.yml"));
        dockerDeploymentCapability.getActions()
                .put(CapabilityActionType.UNDEPLOY, new AwxCapabilityAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "undeploy.yml"));
        //endregion

        //region Create Java Object of Docker Swarm DeploymentCapability:
        dockerSwarmDeploymentCapability = new DeploymentCapability();
        dockerSwarmDeploymentCapability.setName("Docker-Swarm");
        dockerSwarmDeploymentCapability.setLogo("mdi-docker-swarm");
        dockerSwarmDeploymentCapability.setType(Arrays.asList(
                CapabilityType.SETUP,
                CapabilityType.DEPLOY,
                CapabilityType.SCALE
        ));
        dockerSwarmDeploymentCapability.setCapabilityClass("DeploymentCapability");

        dockerSwarmDeploymentCapability.setSupportedDeploymentTypes(Arrays.asList(
                DeploymentType.DOCKER_CONTAINER,
                DeploymentType.DOCKER_COMPOSE
        ));

        // Set AWX Capability Actions
        var dockerSwarmDeploymentCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-dc-docker-swarm.git";
        var dockerSwarmDeploymentCapabilityBranch = "1.0.0";
        dockerSwarmDeploymentCapability.getActions().put(CapabilityActionType.INSTALL,
                new AwxCapabilityAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "install.yml"));
        dockerSwarmDeploymentCapability.getActions().put(CapabilityActionType.UNINSTALL,
                new AwxCapabilityAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "uninstall.yml"));
        dockerSwarmDeploymentCapability.getActions().put(CapabilityActionType.DEPLOY,
                new AwxCapabilityAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "deploy.yml"));
        dockerSwarmDeploymentCapability.getActions().put(CapabilityActionType.UNDEPLOY,
                new AwxCapabilityAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "undeploy.yml"));
        dockerSwarmDeploymentCapability.getActions().put(CapabilityActionType.SCALE_UP,
                new AwxCapabilityAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "scaleup.yml"));
        dockerSwarmDeploymentCapability.getActions().put(CapabilityActionType.SCALE_DOWN,
                new AwxCapabilityAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "scaledown.yml"));

        // Set Cluster Member Types:
        dockerSwarmDeploymentCapability.setClusterMemberTypes(Arrays.asList(
                new ClusterMemberType("Manager", "docker_manager", 3, false),
                new ClusterMemberType("Worker","docker_worker", 1, true)
        ));
        //endregion

        //region Create Java Object of KVM/QEMU VirtualizationCapability
        kvmQemuVirtualizationCapability = new VirtualizationCapability();
        kvmQemuVirtualizationCapability.setName("KVM/QEMU");
        kvmQemuVirtualizationCapability.setLogo("mdi-kvm");
        kvmQemuVirtualizationCapability.setType(Arrays.asList(
                CapabilityType.SETUP,
                CapabilityType.VM
        ));
        kvmQemuVirtualizationCapability.setCapabilityClass("VirtualizationCapability");

        // Set AWX Capability Actions
        var kvmQemuVirtualizationCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-vrp-kvm";
        var kvmQemuVirtualizationCapabilityBranch = "main";
        dockerDeploymentCapability.getActions()
                .put(CapabilityActionType.INSTALL, new AwxCapabilityAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "install.yml"));
        dockerDeploymentCapability.getActions()
                .put(CapabilityActionType.UNINSTALL, new AwxCapabilityAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "uninstall.yml"));
        dockerDeploymentCapability.getActions()
                .put(CapabilityActionType.CREATE_VM, new AwxCapabilityAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "create_vm.yml"));
        dockerDeploymentCapability.getActions()
                .put(CapabilityActionType.DELETE_VM, new AwxCapabilityAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "delete_vm.yml"));
        //endregion
    }

    @BeforeEach
    public void beforeEach() throws JsonProcessingException {
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Nested
    @Order(10)
    @DisplayName("Pretests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @WithMockKeycloakAuth(
        claims = @OpenIdClaims(
                iss = "https://localhost/auth/realms/vfk",
                preferredUsername = "testUser123"
    ))
    public class doPreTests {
        @Test
        @Order(10)
        @DisplayName("Application Context loads")
        public void contextLoads() {
            Assertions.assertThat(controller).isNotNull();
        }

        @Test
        @Order(20)
        @DisplayName("Check availability of BASE_PATH (GET " + BASE_PATH + ")")
        public void checkBaseEndpoint() throws Exception {
            mockMvc.perform(
                            get(BASE_URL))
                    .andExpect(status().isOk());
        }
    }


    @Nested
    @Order(20)
    @DisplayName("Deployment Capabilities")
    @WithMockKeycloakAuth (
        claims = @OpenIdClaims(
            iss = "https://localhost/auth/realms/vfk",
            preferredUsername = "testUser123"
    ))
    @TestClassOrder(ClassOrderer.OrderAnnotation.class)
    public class testDeploymentCapabilities {

        @Nested
        @Order(10)
        @DisplayName("Single Host Deployment Capability")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        public class testSingleHostDeploymentCapability {
            @Test
            @Order(10)
            @DisplayName("Get Capabilities and expect empty list (GET " + BASE_PATH + ")")
            public void getCapabilitiesEmptyList(@Value("${:0}") int expectedSize) throws Exception {
                mockMvc.perform(
                                get(BASE_PATH)
                                        .with(csrf())
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$", hasSize(expectedSize)))
                        .andDo(MockMvcResultHandlers.print())
                        .andReturn();
            }

            @Test
            @Order(20)
            @DisplayName("Create Single Host Deployment Capability (POST " + BASE_PATH + ")")
            public void createSingleHostDeploymentCapability() throws Exception {
                mockMvc.perform(
                                post(BASE_URL)
                                        .content( getSingleHostDeploymentCapabilityDTOAsJsonString() )
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isCreated())
                        .andDo(MockMvcResultHandlers.print());
            }

            @Test
            @Order(30)
            @DisplayName("Get Capabilities and expect list with size == 1 (GET " + BASE_PATH + ")")
            public void getCapabilitiesAndExpectSingleHostDeploymentCapability(@Value("${:1}") int expectedSize) throws Exception {
                Mockito
                        .when(capabilitiesManager.getCapabilities(any(Optional.of(new CapabilityFilter.Builder().build()).getClass())))
                        .thenReturn(Arrays.asList(dockerDeploymentCapability));

                mockMvc.perform(get(BASE_PATH).with(csrf()))
                        .andExpect(status().isOk())
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$", hasSize(expectedSize)))
                        .andReturn();
            }

            @Test
            @Order(40)
            @DisplayName("Delete Single Host Deployment Capability (DELETE " + BASE_PATH + "/{capabilityId})")
            public void deleteSingleHostDeploymentCapability() throws Exception {
                Mockito
                        .when(capabilitiesManager.deleteCapability(dockerDeploymentCapability.getId()))
                        .thenReturn(true);

                mockMvc.perform(
                                delete(BASE_URL + "/" + dockerDeploymentCapability.getId())
                        )
                        .andExpect(status().isOk())
                        .andDo(MockMvcResultHandlers.print());
            }
        }

        @Nested
        @Order(20)
        @DisplayName("Multi Host Deployment Capability")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        public class testMultiHostDeploymentCapability {
            @Test
            @Order(10)
            @DisplayName("Get Capabilities and expect empty list (GET " + BASE_PATH + ")")
            public void getCapabilitiesEmptyList(@Value("${:0}") int expectedSize) throws Exception {
                mockMvc.perform(
                                get(BASE_PATH)
                                        .with(csrf())
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$", hasSize(expectedSize)))
                        .andDo(MockMvcResultHandlers.print())
                        .andReturn();
            }

            @Test
            @Order(20)
            @DisplayName("Create Multi Host Deployment Capability (POST " + BASE_PATH + ")")
            public void createClusterCapability() throws Exception {
                mockMvc.perform(
                                post(BASE_URL)
                                        .content( getMultiHostDeploymentCapabilityDTOAsJsonString() )
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isCreated())
                        .andDo(MockMvcResultHandlers.print());
            }

            @Test
            @Order(30)
            @DisplayName("Get Capabilities and expect list with size == 1 (GET " + BASE_PATH + ")")
            public void getCapabilitiesAndExpectMultiHostDeploymentCapability(@Value("${:1}") int expectedSize) throws Exception {
                Mockito
                        .when(capabilitiesManager.getCapabilities(any(Optional.of(new CapabilityFilter.Builder().build()).getClass())))
                        .thenReturn(Arrays.asList(dockerSwarmDeploymentCapability));

                mockMvc.perform(get(BASE_PATH).with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$", hasSize(expectedSize)))
                        .andDo(MockMvcResultHandlers.print())
                        .andReturn();
            }

            @Test
            @Order(40)
            @DisplayName("Delete Multi Host Deployment Capability (DELETE " + BASE_PATH + "/{capabilityId})")
            public void deleteMultiHostDeploymentCapability() throws Exception {
                Mockito
                        .when(capabilitiesManager.deleteCapability(dockerSwarmDeploymentCapability.getId()))
                        .thenReturn(true);

                mockMvc.perform(
                                delete(BASE_URL + "/" + dockerSwarmDeploymentCapability.getId())
                        )
                        .andExpect(status().isOk())
                        .andDo(MockMvcResultHandlers.print());
            }
        }
    }


    @Nested
    @Order(30)
    @DisplayName("Virtualization Capabilities")
    @WithMockKeycloakAuth (
        claims = @OpenIdClaims(
            iss = "https://localhost/auth/realms/vfk",
            preferredUsername = "testUser123"
    ))
    @TestClassOrder(ClassOrderer.OrderAnnotation.class)
    public class testVirtualizationCapabilities {
        @Nested
        @Order(10)
        @DisplayName("Single Host Virtualization Capability")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        public class testSingleHostVirtualizationCapability {
            @Test
            @Order(10)
            @DisplayName("Get Capabilities and expect empty list (GET " + BASE_PATH + ")")
            public void getCapabilitiesEmptyList(@Value("${:0}") int expectedSize) throws Exception {
                mockMvc.perform(
                                get(BASE_PATH)
                                        .with(csrf())
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$", hasSize(expectedSize)))
                        .andDo(MockMvcResultHandlers.print())
                        .andReturn();
            }

            @Test
            @Order(20)
            @DisplayName("Create Single Host Virtualization Capability (POST " + BASE_PATH + ")")
            public void createSingleHostVirtualizationCapability() throws Exception {
                mockMvc.perform(
                                post(BASE_URL)
                                        .content( getSingleHostVirtualizationCapabilityDTOAsJsonString() )
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isCreated())
                        .andDo(MockMvcResultHandlers.print());
            }

            @Test
            @Order(30)
            @DisplayName("Get Capabilities and expect list with size == 1 (GET " + BASE_PATH + ")")
            public void getCapabilitiesAndExpectSingleHostVirtualizationCapability(@Value("${:1}") int expectedSize) throws Exception {
                Mockito
                        .when(capabilitiesManager.getCapabilities(any(Optional.of(new CapabilityFilter.Builder().build()).getClass())))
                        .thenReturn(Arrays.asList(kvmQemuVirtualizationCapability));

                mockMvc.perform(get(BASE_PATH).with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$", hasSize(expectedSize)))
                        .andDo(MockMvcResultHandlers.print())
                        .andReturn();
            }

            @Test
            @Order(40)
            @DisplayName("Delete Single Host Virtualization Capability (DELETE " + BASE_PATH + "/{capabilityId})")
            public void deleteSingleHostVirtualizationCapability() throws Exception {
                Mockito
                        .when(capabilitiesManager.deleteCapability(kvmQemuVirtualizationCapability.getId()))
                        .thenReturn(true);

                mockMvc.perform(
                                delete(BASE_URL + "/" + kvmQemuVirtualizationCapability.getId())
                        )
                        .andExpect(status().isOk())
                        .andDo(MockMvcResultHandlers.print());
            }
        }
    }
}
