package org.eclipse.slm.resource_management.features.capabilities;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.model.DeploymentType;
import org.assertj.core.api.Assertions;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.ClusterMemberType;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityFilter;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityType;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.VirtualizationCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CapabilitiesRestController.class)
@ContextConfiguration(
    classes = {
            CapabilitiesRestController.class,
            RestTemplate.class
    }
)
@ComponentScan(basePackages = {
    "org.eclipse.slm.common.consul.client",
})
@AutoConfigureMockMvc(addFilters = false)
@WithMockJwtAuth(
    claims = @OpenIdClaims(
            iss = "https://localhost/auth/realms/vfk",
            preferredUsername = "testUser123"
    )
)
@ActiveProfiles("test")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class CapabilityRestControllerTest {

    //region Variables
    public final static String BASE_PATH = "/resources/capabilities";

    @Autowired
    private CapabilitiesRestController controller;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static DeploymentCapability dockerDeploymentCapability;
    private static DeploymentCapability dockerSwarmDeploymentCapability;
    private static VirtualizationCapability kvmQemuVirtualizationCapability;

    @MockBean
    private CapabilitiesService capabilitiesManager;
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
    public static void beforeAll() {
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
                .put(ActionType.INSTALL, new AwxAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "install.yml"));
        dockerDeploymentCapability.getActions()
                .put(ActionType.UNINSTALL, new AwxAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "uninstall.yml"));
        dockerDeploymentCapability.getActions()
                .put(ActionType.DEPLOY, new AwxAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "deploy.yml"));
        dockerDeploymentCapability.getActions()
                .put(ActionType.UNDEPLOY, new AwxAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "undeploy.yml"));
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
        dockerSwarmDeploymentCapability.getActions().put(ActionType.INSTALL,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "install.yml"));
        dockerSwarmDeploymentCapability.getActions().put(ActionType.UNINSTALL,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "uninstall.yml"));
        dockerSwarmDeploymentCapability.getActions().put(ActionType.DEPLOY,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "deploy.yml"));
        dockerSwarmDeploymentCapability.getActions().put(ActionType.UNDEPLOY,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "undeploy.yml"));
        dockerSwarmDeploymentCapability.getActions().put(ActionType.SCALE_UP,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "scaleup.yml"));
        dockerSwarmDeploymentCapability.getActions().put(ActionType.SCALE_DOWN,
                new AwxAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "scaledown.yml"));

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
                .put(ActionType.INSTALL, new AwxAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "install.yml"));
        dockerDeploymentCapability.getActions()
                .put(ActionType.UNINSTALL, new AwxAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "uninstall.yml"));
        dockerDeploymentCapability.getActions()
                .put(ActionType.CREATE_VM, new AwxAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "create_vm.yml"));
        dockerDeploymentCapability.getActions()
                .put(ActionType.DELETE_VM, new AwxAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "delete_vm.yml"));
        //endregion
    }

    @Nested
    @Order(10)
    @DisplayName("Pretests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
                            get(BASE_PATH))
                    .andExpect(status().isOk());
        }
    }


    @Nested
    @Order(20)
    @DisplayName("Deployment Capabilities")
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
                                post(BASE_PATH)
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
                                delete(BASE_PATH + "/" + dockerDeploymentCapability.getId())
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
                                post(BASE_PATH)
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
                                delete(BASE_PATH + "/" + dockerSwarmDeploymentCapability.getId())
                        )
                        .andExpect(status().isOk())
                        .andDo(MockMvcResultHandlers.print());
            }
        }
    }


    @Nested
    @Order(30)
    @DisplayName("Virtualization Capabilities")
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
                                post(BASE_PATH)
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
                                delete(BASE_PATH + "/" + kvmQemuVirtualizationCapability.getId())
                        )
                        .andExpect(status().isOk())
                        .andDo(MockMvcResultHandlers.print());
            }
        }
    }
}
