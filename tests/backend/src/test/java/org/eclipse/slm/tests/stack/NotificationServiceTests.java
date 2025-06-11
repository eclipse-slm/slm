package org.eclipse.slm.tests.stack;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.get;

@DisplayName("Notification Service")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotificationServiceTests {

    @BeforeAll
    public static void init()
    {
        RestAssured.baseURI = TestConfig.NOTIFICATION_SERVICE_BASE_URL;
        RestAssured.port = TestConfig.NOTIFICATION_SERVICE_PORT;
        RestAssured.basePath = TestConfig.NOTIFICATION_SERVICE_BASE_PATH;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Order(10)
    @Test
    @DisplayName("Swagger UI is reachable")
    public void checkResourceRegistrySwaggerUiReachable() {
        get("/swagger-ui.html")
                .then().assertThat() .statusCode(200);
    }

    @Order(20)
    @Test
    @DisplayName("REST API is reachable and endpoint secured")
    public void checkRestApiReachableAndSecured() {
        get("/notifications")
                .then().assertThat() .statusCode(401);
    }

}
