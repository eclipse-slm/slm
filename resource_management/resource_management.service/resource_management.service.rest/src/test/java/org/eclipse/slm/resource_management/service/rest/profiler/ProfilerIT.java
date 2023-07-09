package org.eclipse.slm.resource_management.service.rest.profiler;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.eclipse.slm.common.awx.client.AwxClient;
import org.eclipse.slm.common.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakConfigResolver;
import org.eclipse.slm.resource_management.persistence.api.ProfilerJpaRepository;
import org.eclipse.slm.resource_management.service.rest.Application;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesRestControllerITHelper;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.assertNotNull;
import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class ProfilerIT {
    @Autowired
    ProfilerManager profilerManager;
    @Autowired
    ProfilerRestController profilerRestController;
    @Autowired
    ProfilerJpaRepository profilerJpaRepository;
    @MockBean
    AwxClient awxClient;
    @MockBean
    AwxJobExecutor awxJobExecutor;

    @Nested
    @Order(10)
    @DisplayName("Pretests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class preTests {
        @Test
        @Order(10)
        public void testCapabilitiesManagerNotNull() {
            assertNotNull(profilerManager);
            assertNotNull(profilerRestController);
            assertNotNull(profilerJpaRepository);
        }
    }

    @Nested
    @Order(20)
    @DisplayName("ProfilerIT")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class ProfilerITests {

        @BeforeAll
        public static void beforeAll(@Value("${local.server.port}") int serverPort) {
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = serverPort;
            RestAssured.basePath = "/resources/profiler";
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        }

        @Test
        @Order(10)
        public void getAllProfilerAndExpectNone() throws Exception {
            get().then()
                    .statusCode(200)
//                    .contentType(ContentType.JSON)
                    .assertThat()
                    .body("", hasSize(0));
        }
    }
}
