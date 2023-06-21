package de.fhg.ipa.ced.tests.stack;

import de.fhg.ipa.ced.tests.utils.KeycloakUtil;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;

@DisplayName("AWX")
public class AWXTests {

    @BeforeAll
    public static void init()
    {
        RestAssured.baseURI = TestConfig.AWX_BASE_URL;
        RestAssured.port = TestConfig.AWX_PORT;
        RestAssured.basePath = "";
    }

    @Test
    @DisplayName("AWX is reachable")
    public void checkAwxReachable() {
        get("")
            .then().assertThat() .statusCode(200);
    }

    @Test
    @DisplayName("Get Token from AWX JWT Proxy")
    public void getAwxTokenViaJwtProxy() {
        given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
        .post("/jwt/token/")
            .then()
                .log().all()
                .assertThat().statusCode(200);
    }

}
