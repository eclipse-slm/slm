package org.eclipse.slm.resource_management.features.profiler;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.*;

@WebMvcTest(ProfilerRestController.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {
        ProfilerRestController.class
})
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class ProfilerRestControllerControllerTest {

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

        private String basePath = ProfilerRestApiConfig.BASE_PATH;

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

        private String profilerBody(UUID id, String name) {
            return """
                    {
                      "id": "%s",
                      "name": "%s",
                      "action": {
                        "actionClass": "AwxAction",
                        "awxRepo": "https://example.org/repo.git",
                        "awxBranch": "main",
                        "playbook": "playbook.yml"
                      }
                    }
                    """.formatted(id, name);
        }

        @Test
        @Order(20)
        public void updateProfilerDelegatesAndReturnsResult() {
            var id = UUID.randomUUID();
            var updated = new Profiler();
            updated.setId(id);
            updated.setName("renamed");
            when(profilerService.updateProfiler(eq(id), any())).thenReturn(updated);

            RestAssuredMockMvc.given()
                .contentType("application/json")
                .accept("application/json")
                .body(profilerBody(id, "renamed"))
            .when()
                .put(basePath + "/" + id)
            .then()
                .statusCode(200)
                .body("name", equalTo("renamed"));
        }

        @Test
        @Order(30)
        public void updateProfilerUsesPathId() {
            var pathId = UUID.randomUUID();
            var bodyId = UUID.randomUUID();
            when(profilerService.updateProfiler(any(), any())).thenReturn(new Profiler());

            RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(profilerBody(bodyId, "x"))
            .when()
                .put(basePath + "/" + pathId)
            .then()
                .statusCode(200);

            verify(profilerService).updateProfiler(eq(pathId), any());
        }

        @Test
        @Order(40)
        public void updateUnknownProfilerReturns404() {
            var id = UUID.randomUUID();
            when(profilerService.updateProfiler(eq(id), any()))
                    .thenThrow(new ProfilerNotFoundException(id));

            RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(profilerBody(id, "x"))
            .when()
                .put(basePath + "/" + id)
            .then()
                .statusCode(404);
        }
    }
}
