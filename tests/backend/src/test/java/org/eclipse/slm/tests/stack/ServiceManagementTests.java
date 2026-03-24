package org.eclipse.slm.tests.stack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.slm.tests.utils.KeycloakUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Service Management")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceManagementTests {

    @BeforeAll
    public static void init()
    {
        RestAssured.baseURI = TestConfig.SERVICE_MANAGEMENT_BASE_URL;
        RestAssured.port = TestConfig.SERVICE_MANAGEMENT_PORT;
        RestAssured.basePath = TestConfig.SERVICE_MANAGEMENT_BASE_PATH;
        RestAssured.useRelaxedHTTPSValidation();
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
        get("/services/offerings")
            .then().assertThat() .statusCode(401);
    }

    @Order(30)
    @Test
    @DisplayName("Get service offerings")
    public void getServiceOfferings() {
        given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/services/offerings?withImage=false")
        .then().assertThat() .statusCode(200);
    }

    @Order(40)
    @Test
    @DisplayName("Get service vendor")
    public void getServiceVendors() {
        given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/services/vendors")
        .then().assertThat() .statusCode(200);
    }

    @Order(50)
    @Test
    @DisplayName("Create service vendor")
    public void postServiceVendor() {
        TestServiceVendor serviceVendor = TestConfig.TEST_SERVICE_VENDOR;

        given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .contentType(ContentType.JSON)
        .body(serviceVendor)
        .post("/services/vendors")
        .then().assertThat() .statusCode(200);

        given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/services/vendors")
        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)));
    }

    @Order(60)
    @Test
    @DisplayName("Get developers of service vendor")
    public void getServiceVendorDevelopers() {
        var serviceVendorId = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
            .get("/services/vendors")
            .then()
            .assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)))
            .extract().body().path("[0].id").toString();

        given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/services/vendors/"+ serviceVendorId +"/developers")
        .then().assertThat() .statusCode(200);
    }

    @Order(70)
    @Test
    @DisplayName("Create developer for service vendor")
    public void putServiceVendorDeveloper() throws JsonProcessingException {
        String userUuid = KeycloakUtil.getKeycloakUserUuid();

        var serviceVendorId = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
            .get("/services/vendors")
        .then()
        .assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)))
        .extract().body().path("[0].id").toString();

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
            .put("/services/vendors/"+serviceVendorId+"/developers/"+userUuid)
        .then()
        .assertThat() .statusCode(200);

        given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/services/vendors/"+ serviceVendorId +"/developers")
        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)));
    }

    @Order(80)
    @Test
    @DisplayName("Create service offering")
    public void postServiceOffering() throws IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("service-offering.json");
        ObjectNode serviceOfferingJson = (ObjectNode) new ObjectMapper().readTree(new File(resource.toURI()));

        var serviceVendorId = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
            .get("/services/vendors")
        .then()
        .assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)))
        .extract().body().path("[0].id").toString();

        serviceOfferingJson.put("serviceVendorId", serviceVendorId);

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .contentType(ContentType.JSON)
        .body(serviceOfferingJson)
        .post("/services/offerings")
        .then()
        .assertThat() .statusCode(is(200));

        given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/services/offerings?withImage=false")
        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)));

        return;
    }

    @Order(90)
    @Test
    @DisplayName("Delete service offering")
    public void deleteServiceOffering() {
        var serviceOfferingId = given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/services/offerings?withImage=false")
        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)))
        .extract().body().path("[0].id").toString();

        given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
            .delete("/services/offerings/" + serviceOfferingId)
        .then()
        .assertThat() .statusCode(200);

        return;
    }
}
