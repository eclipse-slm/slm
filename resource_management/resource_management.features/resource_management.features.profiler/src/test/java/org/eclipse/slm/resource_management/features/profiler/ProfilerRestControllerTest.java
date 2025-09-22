package org.eclipse.slm.resource_management.features.profiler;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertNotNull;
import static org.hamcrest.Matchers.*;

@WebMvcTest(ProfilerRestController.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {
        ProfilerRestController.class
})
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class ProfilerRestControllerTest {

    @Autowired
    ProfilerRestController profilerRestController;

    @MockBean
    ProfilerService profilerService;

    @Nested
    @Order(10)
    @DisplayName("Pre tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class PreTests {
        @Test
        @Order(10)
        public void testProfilerControllerNotNull() {
            assertNotNull(profilerRestController);
        }
    }

    @Nested
    @Order(20)
    @DisplayName("Controller tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class ControllerTests {

        private String basePath = ProfilerRestControllerConfig.BASE_PATH;

        @BeforeEach
        public void beforeEach() {
            RestAssuredMockMvc.standaloneSetup(profilerRestController);
            RestAssuredMockMvc.enableLoggingOfRequestAndResponseIfValidationFails();
        }

        @Test
        @Order(10)
        public void getAllProfilerAndExpectNone() throws Exception {
            RestAssuredMockMvc.get(basePath)
            .then()
                .statusCode(200)
                .assertThat()
                .body("", hasSize(0));
        }
    }
}
