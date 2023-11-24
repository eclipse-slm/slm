package org.eclipse.slm.resource_management.service.rest.capabilities;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.resource_management.service.rest.handler.provider.ServiceHosterHandler;
import org.eclipse.slm.resource_management.service.rest.handler.provider.VirtualResourceProviderHandler;
import org.eclipse.slm.resource_management.model.capabilities.CapabilityType;
import org.eclipse.slm.resource_management.model.capabilities.VirtualizationCapability;
import org.eclipse.slm.resource_management.model.actions.AwxAction;
import org.eclipse.slm.resource_management.model.actions.ActionType;
import org.eclipse.slm.resource_management.model.capabilities.provider.VirtualResourceProvider;
import org.eclipse.slm.resource_management.model.cluster.ClusterMemberType;
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.model.consul.capability.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.model.consul.capability.SingleHostCapabilityService;
import org.eclipse.slm.resource_management.service.rest.Application;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        Application.class,
        CapabilityProvidersRestController.class,
},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ComponentScan(basePackages = {
        "org.eclipse.slm.common.keycloak.config"
})
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class CapabilityProvidersRestControllerTest {
    //region Variables
    public final static String BASE_PATH = "/resources/providers";
    public static String BASE_URL = "";
    @Autowired
    private static MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    CapabilityProvidersRestController capabilityProvidersRestController;
    @MockBean
    VirtualResourceProviderHandler virtualResourceProviderHandler;
    @MockBean
    ServiceHosterHandler serviceHosterHandler;

    public final static GenericContainer<?> consulDockerContainer;
    private static int CONSUL_PORT = 8500;

    @DynamicPropertySource
    static void consulProperties(DynamicPropertyRegistry registry){
        registry.add("consul.port", consulDockerContainer::getFirstMappedPort);
    }
    //endregion

    static {
        consulDockerContainer = new GenericContainer<>(DockerImageName.parse("consul:1.14"))
                .withExposedPorts(CONSUL_PORT)
                .withEnv("CONSUL_LOCAL_CONFIG", "{\"datacenter\": \"fabos\", \"domain\": \".fabos\", \"bind_addr\": \"0.0.0.0\", \"retry_join\": [\"0.0.0.0\"], \"acl\":{\"enabled\": true, \"default_policy\": \"allow\", \"tokens\":{\"master\": \"myroot\"}}}");
        consulDockerContainer.start();
    }

    @AfterAll
    public static void afterAll(){
        consulDockerContainer.stop();
    }

    @BeforeAll
    public static void beforeAll(@Value("${local.server.port:80}") int port) {
        BASE_URL = "http://localhost:" + port + BASE_PATH;
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
            Assertions.assertThat(capabilityProvidersRestController).isNotNull();
        }
    }

    @Nested
    @Order(20)
    @DisplayName("VirtualResourceProvider")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @WithMockKeycloakAuth(
        claims = @OpenIdClaims(
                iss = "https://localhost/auth/realms/vfk",
                preferredUsername = "testUser123"
    ))
    public class virtualResourceProviderEndpointTests {
        private final static String BASE_URL_EXT = "/virtual-resource-provider";

        private static String BASE_URL_TEST = BASE_URL + BASE_URL_EXT;

        private static List<VirtualResourceProvider> oneSingleHostVirtualResourceProvider = new ArrayList<>();
        private static List<VirtualResourceProvider> oneMultiHostVirtualResourceProvider = new ArrayList<>();
        private static List<VirtualResourceProvider> mixedVirtualResourceProvider = new ArrayList<>();

        @BeforeAll
        private static void beforeAll() {
            //region INIT - SingleHost VirtualizationCapability
            VirtualizationCapability vc = new VirtualizationCapability();
            vc.setName("KVM/QEMU(SH)");
            vc.setCapabilityClass("VirtualizationCapability");
            vc.setLogo("mdi-hypervisor");
            vc.setType(Arrays.asList(
                    CapabilityType.SETUP,
                    CapabilityType.VM
            ));
            var kvmQemuVirtualizationCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-vrp-kvm";
            var kvmQemuVirtualizationCapabilityBranch = "main";
            vc.getActions()
                    .put(ActionType.INSTALL, new AwxAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "install.yml"));
            vc.getActions()
                    .put(ActionType.UNINSTALL, new AwxAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "uninstall.yml"));
            vc.getActions()
                    .put(ActionType.CREATE_VM, new AwxAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "create_vm.yml"));
            vc.getActions()
                    .put(ActionType.DELETE_VM, new AwxAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "delete_vm.yml"));
            //endregion

            //region INIT - MultiHost VirtualizationCapability
            VirtualizationCapability mhvc = new VirtualizationCapability();
            mhvc.setName("KVM/QEMU(MH)");
            mhvc.setCapabilityClass("VirtualizationCapability");
            mhvc.setLogo("mdi-hypervisor");
            mhvc.setType(Arrays.asList(
                    CapabilityType.SETUP,
                    CapabilityType.VM
            ));
            kvmQemuVirtualizationCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-vrp-kvm";
            kvmQemuVirtualizationCapabilityBranch = "main";
            mhvc.getActions()
                    .put(ActionType.INSTALL, new AwxAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "install.yml"));
            mhvc.getActions()
                    .put(ActionType.UNINSTALL, new AwxAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "uninstall.yml"));
            mhvc.getActions()
                    .put(ActionType.CREATE_VM, new AwxAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "create_vm.yml"));
            mhvc.getActions()
                    .put(ActionType.DELETE_VM, new AwxAction(kvmQemuVirtualizationCapabilityRepo, kvmQemuVirtualizationCapabilityBranch, "delete_vm.yml"));

            mhvc.getClusterMemberTypes().add(new ClusterMemberType("memberTyp1", "memberTyp1", 1, false));
            mhvc.getClusterMemberTypes().add(new ClusterMemberType("memberTyp2", "memberTyp2", 1, false));
            //endregion

            //region INIT - oneSingleHostVirtualResourceProvider
            VirtualResourceProvider vrp = new VirtualResourceProvider(
                    new SingleHostCapabilityService(
                            vc,
                            UUID.randomUUID()
                    )
            );

            oneSingleHostVirtualResourceProvider.add(vrp);
            //endregion

            //region INIT - oneMultiHostVirtualResourceProvider
            Map<UUID, String> memberMapping = Map.of(
                    UUID.randomUUID(), "memberTyp1",
                    UUID.randomUUID(), "memberTyp1",
                    UUID.randomUUID(), "memberTyp2",
                    UUID.randomUUID(), "memberTyp2"
            );

            oneMultiHostVirtualResourceProvider.add(
                    new VirtualResourceProvider(
                        new MultiHostCapabilityService(mhvc,memberMapping,UUID.randomUUID(), CapabilityServiceStatus.INSTALL, false)
                    )
            );
            //endregion

            //region - mixedVirtualResourceProvider
            mixedVirtualResourceProvider.add(oneSingleHostVirtualResourceProvider.get(0));
            mixedVirtualResourceProvider.add(oneMultiHostVirtualResourceProvider.get(0));
            //endregion
        }

        @Test
        @Order(10)
        @DisplayName("Check availability of '" + BASE_URL_EXT + "'")
        public void testAvailabilityOfBaseEndpoint() throws Exception {
            mockMvc.perform(get(BASE_URL_TEST))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(20)
        @DisplayName("Get list containing one virtual resource provider representing a single host capability service (GET '" + BASE_URL_EXT + "')")
        public void getListOfVirtualResourceProviderContainingOneSingleHostCapabilityService() throws Exception {
            when(virtualResourceProviderHandler.getVirtualResourceProviders(Mockito.any()))
                .thenReturn(oneSingleHostVirtualResourceProvider);

            MvcResult result = mockMvc.perform(get(BASE_URL_TEST))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(oneSingleHostVirtualResourceProvider.size())))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
        }

        @Test
        @Order(30)
        @DisplayName("Get list containing one virtual resource provider representing a multi host capability service (GET '" + BASE_URL_EXT + "')")
        public void getListOfVirtualResourceProviderContainingOneMultiHostCapabilityService() throws Exception {
            when(virtualResourceProviderHandler.getVirtualResourceProviders(Mockito.any()))
                    .thenReturn(oneMultiHostVirtualResourceProvider);

            MvcResult result = mockMvc.perform(get(BASE_URL_TEST))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(oneMultiHostVirtualResourceProvider.size())))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
        }

        @Test
        @Order(40)
        @DisplayName("Get mixed list (single/multi host) of virtual resource provider (GET '" + BASE_URL_EXT + "')")
        public void getListOfVirtualResourceProvider() throws Exception {
            when(virtualResourceProviderHandler.getVirtualResourceProviders(Mockito.any()))
                    .thenReturn(mixedVirtualResourceProvider);

            MvcResult result = mockMvc.perform(get(BASE_URL_TEST))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(mixedVirtualResourceProvider.size())))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
        }
    }


    @Nested
    @Order(30)
    @DisplayName("ServiceHosters")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @WithMockKeycloakAuth(
            claims = @OpenIdClaims(
                    iss = "https://localhost/auth/realms/vfk",
                    preferredUsername = "testUser123"
            ))
    public class serviceHosterEndpointTests {
        private final static String BASE_URL_EXT = "/service-hoster";

        private static String BASE_URL_TEST = BASE_URL + BASE_URL_EXT;

        @Test
        @Order(10)
        @DisplayName("Check availability of '" + BASE_URL_EXT + "'")
        public void testAvailabilityOfBaseEndpoint() throws Exception {
            mockMvc.perform(get(BASE_URL_TEST))
                    .andExpect(status().isOk());
        }
    }

}
