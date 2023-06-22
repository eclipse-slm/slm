package org.eclipse.slm.tests.stack;

import org.eclipse.slm.tests.utils.KeycloakUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Resource Registry")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResourceManagementTests {

    @BeforeAll
    public static void init()
    {
        RestAssured.baseURI = TestConfig.RESOURCE_REGISTRY_BASE_URL;
        RestAssured.port = TestConfig.RESOURCE_REGISTRY_PORT;
        RestAssured.basePath = "";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    public static Stream<TestResource> getTestResources() {
        return TestConfig.testResourceList.stream();
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
    public void checkResourceRegistryReachable() {
        get("/resources/")
            .then().assertThat() .statusCode(200);
    }

    @Order(30)
    @Test
    @DisplayName("Get resources")
    public void getResources() {
        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
        .log().all()
        .get("/resources")
            .then().assertThat() .statusCode(200);
    }

    @Order(40)
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Add resources")
    public void addResource(TestResource testResource) {
        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
        .log().all()
        .get("/resources")
        .then()
            .assertThat() .statusCode(200);

        var resourceId = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
                .queryParam("project", "fabos")
                .queryParam("sshAccessAvailable", "true")
                .queryParam("checkResource", "false")
                .queryParam("resourceHostname", testResource.hostname)
                .queryParam("resourceIp", testResource.ip)
                .queryParam("resourceUsername", testResource.username)
                .queryParam("resourcePassword", testResource.password)
                .queryParam("checkResource", false)
        .put("/resources")
        .then()
            .assertThat() .statusCode(201).extract().body().asString().replace("\"", "");

        try{
            var uuid = UUID.fromString(resourceId);
        } catch (IllegalArgumentException exception){
            Assertions.fail("Response string '" + resourceId + "'from addResource request is not an UUID");
        }

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/resources")
        .then()
            .assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)));
    }

    @Order(50)
    @Test
    @DisplayName("Add Dummy Deployment Capability")
    public void addDockerDeploymentCapability() {
        given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
            .contentType(ContentType.JSON)
            .body(ResourceRegistryTestsData.DUMMY_DEPLOYMENT_CAPABILITY_JSON)
            .post("/resources/capabilities")
            .then().assertThat() .statusCode(201);
    }

    @Order(60)
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Delete resources")
    public void deleteResource(TestResource testResource) {

        var response = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .get("/resources")
                .then()
                .assertThat() .statusCode(200).body("$", hasSize(lessThanOrEqualTo (5))).extract().response();

        var resourceId = response.body().path("find{ it.ip == '"+testResource.ip+"' }.id").toString();
        var resourceCount = Integer.parseInt(response.body().path("count{ it }").toString());

        given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .delete("/resources/" + resourceId)
        .then()
            .assertThat() .statusCode(200);

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .get("/resources")
                .then()
                .assertThat() .statusCode(200).body("$", hasSize(lessThanOrEqualTo (resourceCount-1)));
    }
}
