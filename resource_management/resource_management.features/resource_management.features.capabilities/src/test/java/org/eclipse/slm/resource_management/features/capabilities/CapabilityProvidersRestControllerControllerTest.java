package org.eclipse.slm.resource_management.features.capabilities;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
import org.eclipse.slm.resource_management.features.capabilities.providers.ProvidersRestController;
import org.eclipse.slm.resource_management.features.capabilities.providers.ServiceHosterHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class CapabilityProvidersRestControllerControllerTest {

    public final static String BASE_PATH = "/resources/providers";

    private static final String TEST_USER_ID = UUID.randomUUID().toString();
    private static final String TEST_GROUP_ID = "/users/" + TEST_USER_ID;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProvidersRestController providersRestController;

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
