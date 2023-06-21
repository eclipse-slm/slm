package de.fhg.ipa.ced.tests.stack;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.get;

@DisplayName("Notification Service")
public class NotificationServiceTests {

    @BeforeAll
    public static void init()
    {
        RestAssured.baseURI = TestConfig.NOTIFICATION_SERVICE_BASE_URL;
        RestAssured.port = TestConfig.NOTIFICATION_SERVICE_PORT;
        RestAssured.basePath = "";
    }

    @Test
    @DisplayName("Swagger UI is reachable")
    public void checkResourceRegistrySwaggerUiReachable() {
        get("/swagger-ui.html")
                .then().assertThat() .statusCode(200);
    }

}
