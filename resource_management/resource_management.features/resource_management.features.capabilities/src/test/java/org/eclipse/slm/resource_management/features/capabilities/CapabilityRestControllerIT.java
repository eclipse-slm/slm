package org.eclipse.slm.resource_management.features.capabilities;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import org.eclipse.slm.resource_management.common.resources.ResourcesManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        CapabilitiesRestController.class,
        ResourcesManager.class
},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Disabled
public class CapabilityRestControllerIT {
    public final static String BASE_PATH = "/resources/capabilities";
    public static String BASE_URL = "";
    @Autowired
    CapabilitiesRestController capabilitiesRestController;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public static void beforeAll(@Value("${local.server.port:80}") int port) {
        BASE_URL = "http://localhost:" + port + BASE_PATH;
    }

    @Nested
    @Order(10)
    @DisplayName("Pretests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @WithMockJwtAuth(
            claims = @OpenIdClaims(
                    iss = "https://localhost/auth/realms/vfk",
                    preferredUsername = "testUser123"
            ))
    public class doPreTests {
        @Test
        @Order(10)
        @DisplayName("Application Context loads")
        public void contextLoads() {
            Assertions.assertThat(capabilitiesRestController).isNotNull();
        }
    }

    @Nested
    @Order(20)
    @DisplayName("CapabilitiesRestController - IT")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @WithMockJwtAuth(
            claims = @OpenIdClaims(
                    iss = "https://localhost/auth/realms/vfk",
                    preferredUsername = "testUser123"
            ))
    public class capabilitiesRestControllerIntegrationTests {
        @Test
        @Order(10)
        public void getCapabilitiesOfResourceIfNoCapabilityInstalled() throws Exception {
            var responseContent = mockMvc.perform(
                            get(BASE_URL)
                    )
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            return;
        }
    }
}
