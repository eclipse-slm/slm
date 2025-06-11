package org.eclipse.slm.tests.stack;

import org.eclipse.slm.tests.utils.KeycloakUtil;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Raspi Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RaspiTests {
    @BeforeAll
    public static void init()
    {
        RestAssured.baseURI = TestConfig.RESOURCE_MANAGEMENT_BASE_URL;
        RestAssured.port = TestConfig.RESOURCE_MANAGEMENT_PORT;
        RestAssured.basePath = "";
        RestAssured.useRelaxedHTTPSValidation();
    }

    public static Stream<TestResource> getTestResources() {
        return TestConfig.testRaspiResourceList.stream();
    }

    @Order(10)
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Add resource")
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

    @Order(20)
    @Test
    @DisplayName("Add Docker deployment capability")
    public void addDockerDeploymentCapability() throws InterruptedException {
        List<TestResource> testResourceList =  getTestResources().collect(Collectors.toList());
        int resourceCount = testResourceList.size();

        var resourceId = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .get("/resources")
                .then()
                .assertThat() .statusCode(200).body("$", hasSize(lessThanOrEqualTo (resourceCount))).extract().body().path("find{ it.ip == '"+testResourceList.get(0).ip+"' }.id").toString();

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .queryParam("deploymentCapability", "DOCKER")
                .put("/resources/" + resourceId + "/deployment-capabilities")
                .then()
                .assertThat() .statusCode(200);

        long start_time = System.currentTimeMillis();
        long timeout = 180000;
        while (System.currentTimeMillis() < start_time + timeout) {

            var deploymentCapabilities = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                    .get("/resources/" + resourceId + "/deployment-capabilities")
                    .then()
                    .assertThat() .statusCode(200).extract().body().as(new TypeRef<List<Object>>(){});

            if (deploymentCapabilities.size() > 0)
            {
                return;
            }
            else
            {
                Thread.sleep(10000);
            }
        }

        Assertions.fail("Add of deployment capability timed out");
    }

    @Order(70)
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Delete resource")
    public void deleteResource(TestResource testResource) {
        int resourceCountStart = 7;

        var response = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .get("/resources")
                .then()
                .assertThat() .statusCode(200).body("$", hasSize(lessThanOrEqualTo (resourceCountStart))).extract().response();

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
