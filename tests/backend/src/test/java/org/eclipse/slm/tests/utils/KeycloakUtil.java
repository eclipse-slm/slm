package org.eclipse.slm.tests.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.tests.stack.TestConfig;

import java.util.Base64;

import static io.restassured.RestAssured.given;

public class KeycloakUtil {

    public static String getKeycloakAccessToken()
    {
        try {
            String accessToken = given().spec(TestConfig.KEYCLOAK_SERVICE_SPEC)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .formParam("grant_type", "password")
                    .formParam("client_id", "ui")
                    .formParam("username", TestConfig.KEYCLOAK_USERNAME)
                    .formParam("password", TestConfig.KEYCLOAK_PASSWORD)
                    .post("/protocol/openid-connect/token")
                    .then()
                    .assertThat().statusCode(200)
                    .extract().response().jsonPath().getString("access_token");

            return accessToken;
        } catch (Exception ex) {
            throw new KeycloakAuthException("Failed to obtain access token from Keycloak", ex);
        }
    }

    public static String getKeycloakUserUuid() throws KeycloakAuthException {
        try {
            String accessToken = getKeycloakAccessToken();
            String[] chunks = accessToken.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            JsonNode jsonNode = new ObjectMapper().readTree(payload);

            return jsonNode.get("sub").asText();
        } catch (JsonProcessingException e) {
            throw new KeycloakAuthException("Failed to parse access token", e);
        }
    }

    public static String getKeycloakFullPathUserGroupId() throws KeycloakAuthException {
        var userId = getKeycloakUserUuid();
        return "/users/" + userId;
    }

}
