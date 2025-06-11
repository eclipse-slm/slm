package org.eclipse.slm.tests.stack;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;

@DisplayName("Consul")
public class ConsulTests {

    @BeforeAll
    public static void init()
    {
        RestAssured.baseURI = TestConfig.CONSUL_BASE_URL;
        RestAssured.port = TestConfig.CONSUL_PORT;
        RestAssured.basePath = "";
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    @DisplayName("Consul UI is reachable")
    public void checkConsulUiReachable() {
        get("/ui/")
            .then()
                .log().all()
                .assertThat() .statusCode(200);
    }

}
