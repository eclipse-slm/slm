package org.eclipse.slm.resource_management.features.capabilities;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.ClusterMemberType;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityType;
import org.eclipse.slm.resource_management.features.capabilities.model.SingleHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.VirtualizationCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
import org.eclipse.slm.resource_management.features.capabilities.providers.ProvidersRestController;
import org.eclipse.slm.resource_management.features.capabilities.providers.ServiceHosterHandler;
import org.eclipse.slm.resource_management.features.capabilities.providers.VirtualResourceProviderHandler;
import org.eclipse.slm.resource_management.features.providers.VirtualResourceProvider;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProvidersRestController.class)
@ContextConfiguration(
    classes = {
            ProvidersRestController.class
    }
)
@AutoConfigureMockMvc
@WithMockJwtAuth
@ActiveProfiles("test")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class CapabilityProvidersRestControllerTest {

    public final static String BASE_PATH = "/resources/providers";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProvidersRestController providersRestController;

    @MockBean
    private VirtualResourceProviderHandler virtualResourceProviderHandler;

    @MockBean
    private CapabilityJpaRepository capabilityJpaRepository;

    @MockBean
    private ServiceHosterHandler serviceHosterHandler;

    private static int CONSUL_PORT = 8500;

//    @Container
//    private final GenericContainer<?> consulDockerContainer = new GenericContainer<>(DockerImageName.parse("consul:1.14"))
//            .withExposedPorts(CONSUL_PORT)
//            .withEnv("CONSUL_LOCAL_CONFIG", "{\"datacenter\": \"fabos\", \"domain\": \".fabos\", \"bind_addr\": \"0.0.0.0\", \"retry_join\": [\"0.0.0.0\"], \"acl\":{\"enabled\": true, \"default_policy\": \"allow\", \"tokens\":{\"master\": \"myroot\"}}}");

    @Nested
    @Order(10)
    @DisplayName("Pretests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class doPreTests {
        @Test
        @Order(10)
        @DisplayName("Application Context loads")
        public void contextLoads() {
            Assertions.assertThat(providersRestController).isNotNull();
        }
    }

    @Nested
    @Order(20)
    @DisplayName("VirtualResourceProvider")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class virtualResourceProviderEndpointTests {
        private final static String BASE_URL_EXT = "/virtual-resource-provider";

        private static String BASE_URL_TEST = BASE_PATH + BASE_URL_EXT;

        private static List<VirtualResourceProvider> oneSingleHostVirtualResourceProvider = new ArrayList<>();
        private static List<VirtualResourceProvider> oneMultiHostVirtualResourceProvider = new ArrayList<>();
        private static List<VirtualResourceProvider> mixedVirtualResourceProvider = new ArrayList<>();

        @BeforeAll
        public static void beforeAll() {
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
            mockMvc.perform(get(BASE_PATH + "/virtual-resource-provider"))
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
    public class serviceHosterEndpointTests {
        private final static String BASE_URL_EXT = "/service-hoster";

        private static String BASE_URL_TEST = BASE_PATH + BASE_URL_EXT;

        @Test
        @Order(10)
        @DisplayName("Check availability of '" + BASE_URL_EXT + "'")
        public void testAvailabilityOfBaseEndpoint() throws Exception {
            mockMvc.perform(get(BASE_URL_TEST))
                    .andExpect(status().isOk());
        }
    }

}
