package org.eclipse.slm.tests.stack;

import org.eclipse.slm.tests.utils.KeycloakUtil;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.get;

@DisplayName("Keycloak")
public class KeycloakTests {

    @BeforeAll
    public static void init()
    {
        RestAssured.baseURI = TestConfig.KEYCLOAK_BASE_URL;
        RestAssured.port = TestConfig.KEYCLOAK_PORT;
        RestAssured.basePath= "/auth";
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    @DisplayName("Keycloak is reachable")
    public void checkKeycloakReachable() {
        get()
            .then().assertThat() .statusCode(200);
    }

    @Test
    @DisplayName("Get Keycloak Access Token for default user")
    public void getKeycloakAccessToken()
    {
        var accessToken = KeycloakUtil.getKeycloakAccessToken();
        Assertions.assertTrue(accessToken.length() > 0);
    }

    @Test
    @DisplayName("Check Keycloak Realm 'FabOS' exists")
    public void checkKeycloakRealmExists()
    {
        // Check if Keycloak Realm fabos is present
        // Keylcoak Admin client / api (https://www.keycloak.org/docs-api/15.0/rest-api/index.html)
        // --> Keycloak --> Admin Login
        // --> ({{ _.keycloak_url }}/admin/realms/{{ keycloak_realm }})
        //Assertions.fail();
    }

}
