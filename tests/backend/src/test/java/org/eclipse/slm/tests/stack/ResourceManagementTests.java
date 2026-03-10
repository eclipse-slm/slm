package org.eclipse.slm.tests.stack;

import org.eclipse.slm.tests.utils.KeycloakUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Resource Management")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResourceManagementTests {

    @BeforeAll
    public static void init()
    {
        RestAssured.baseURI = TestConfig.RESOURCE_MANAGEMENT_BASE_URL;
        RestAssured.port = TestConfig.RESOURCE_MANAGEMENT_PORT;
        RestAssured.basePath = TestConfig.RESOURCE_MANAGEMENT_BASE_PATH;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.useRelaxedHTTPSValidation();
    }

    public static Stream<TestResource> getTestResources() {
        return TestConfig.testResourceList.stream();
    }

    @Order(10)
    @Test
    @DisplayName("Swagger UI is reachable")
    public void checkSwaggerUiReachable() {
        get("/swagger-ui.html")
            .then().assertThat() .statusCode(200);
    }

    @Order(20)
    @Test
    @DisplayName("REST API is reachable and endpoint secured")
    public void checkRestApiReachableAndSecured() {
        get("/resources")
            .then().assertThat() .statusCode(401);
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

        var resourceCreateRequest = new HashMap<String, Object>();
        resourceCreateRequest.put("resourceHostname", testResource.hostname);
        resourceCreateRequest.put("resourceIp", testResource.ip);
        resourceCreateRequest.put("fullPathOwnerGroupId", KeycloakUtil.getKeycloakFullPathUserGroupId());
        var digitalNameplate = new HashMap<String, String>();
        digitalNameplate.put("uriOfTheProduct", "N/A");
        digitalNameplate.put("manufacturerName", "N/A");
        digitalNameplate.put("manufacturerProductDesignation", "N/A");
        digitalNameplate.put("addressInformation", "N/A");
        digitalNameplate.put("serialNumber", "N/A");
        resourceCreateRequest.put("digitalNameplateV3", digitalNameplate);
        var resourceId = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .contentType(ContentType.JSON)
                .body(resourceCreateRequest)
                .post("/resources")
            .then()
                .assertThat().statusCode(201).extract().body().asString().replace("\"", "");

        var credentialId = UUID.randomUUID();
        var credentialData = new HashMap<String, Object>();
        credentialData.put("credentialDataType", "USERNAME_PASSWORD");
        credentialData.put("username", testResource.username);
        credentialData.put("password", testResource.password);

        var credential = new HashMap<String, Object>();
        credential.put("id", credentialId.toString());
        credential.put("name", "test-credential-" + credentialId);
        credential.put("scopesRaw", List.of("REMOTE_ACCESS"));
        credential.put("data", credentialData);

        var credentialCreateRequest = new HashMap<String, Object>();
        credentialCreateRequest.put("credential", credential);
        credentialCreateRequest.put("entityLinks", List.of());
        credentialCreateRequest.put("fullPathOwnerGroupId", KeycloakUtil.getKeycloakFullPathUserGroupId());

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .baseUri(TestConfig.PLATFORM_MANAGEMENT_BASE_URL)
                .port(TestConfig.PLATFORM_MANAGEMENT_PORT)
                .basePath(TestConfig.PLATFORM_MANAGEMENT_BASE_PATH)
                .contentType(ContentType.JSON)
                .body(credentialCreateRequest)
                .put("/credentials/" + credentialId)
                .then()
                .assertThat().statusCode(200);

        var remoteAccessRequest = new HashMap<String, Object>();
        remoteAccessRequest.put("fullPathOwnerGroupId", KeycloakUtil.getKeycloakFullPathUserGroupId());
        remoteAccessRequest.put("credentialId", credentialId.toString());
        remoteAccessRequest.put("username", testResource.username);
        remoteAccessRequest.put("connectionPort", 22);
        remoteAccessRequest.put("connectionType", "ssh");

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .contentType(ContentType.JSON)
                .body(remoteAccessRequest)
                .post("/resources/" + resourceId + "/remote-access")
                .then()
                .assertThat().statusCode(200);

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
    public void addDummyDeploymentCapability() {
        given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
            .contentType(ContentType.JSON)
            .body(ResourceManagementTestsData.DUMMY_DEPLOYMENT_CAPABILITY_JSON)
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
