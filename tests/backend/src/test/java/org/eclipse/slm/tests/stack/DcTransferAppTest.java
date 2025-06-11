package org.eclipse.slm.tests.stack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.slm.tests.utils.KeycloakUtil;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("TransferApp Deployment Capability")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DcTransferAppTest {

    @BeforeAll
    public static void init()
    {
        RestAssured.baseURI = TestConfig.RESOURCE_MANAGEMENT_BASE_URL;
        RestAssured.port = TestConfig.RESOURCE_MANAGEMENT_PORT;
        RestAssured.basePath = "";
        RestAssured.useRelaxedHTTPSValidation();
    }

    public static Stream<TestResource> getTestResources() {
        return TestConfig.testResourceList.stream();
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
    @DisplayName("Add TransferApp deployment capability")
    public void addTransferAppDeploymentCapability() throws InterruptedException {
        List<TestResource> testResourceList =  getTestResources().collect(Collectors.toList());
        int resourceCount = testResourceList.size();
        String dcName = "TRANSFERAPP";
        int resourceIndex = 0;

        var resourceId = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .get("/resources")
                .then()
                .assertThat() .statusCode(200).body("$", hasSize(lessThanOrEqualTo (resourceCount))).extract().body().path("find{ it.ip == '"+testResourceList.get(resourceIndex).ip+"' }.id").toString();

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .queryParam("deploymentCapability", dcName)
                .put("/resources/" + resourceId + "/deployment-capabilities")
                .then()
                .assertThat() .statusCode(200);

        long start_time = System.currentTimeMillis();
        long timeout = 300000;
        while (System.currentTimeMillis() < start_time + timeout) {

            var deploymentCapabilities = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                    .get("/resources/" + resourceId + "/deployment-capabilities")
                    .then()
                    .assertThat() .statusCode(200).extract().body().as(new TypeRef<List<Map<String,String>>>(){});

            if (deploymentCapabilities.size() > 0)
            {
                assert( deploymentCapabilities.get(0).get("name").equals("TRANSFERAPP") );
                return;
            }
            else
            {
                Thread.sleep(10000);
            }
        }

        Assertions.fail("Add of deployment capability timed out");
    }

    @Order(30)
    @Test
    @DisplayName("Create Service Offering")
    public void createServiceOffering() throws URISyntaxException, IOException {
        RequestSpecification requestSpecification = RestAssured.given()
            .baseUri(TestConfig.SERVICE_MANAGEMENT_BASE_URL)
            .port(TestConfig.SERVICE_MANAGEMENT_PORT);
        TestServiceVendor serviceVendor = TestConfig.TEST_SERVICE_VENDOR;

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("service-offering.json");
        ObjectNode serviceOfferingJson = (ObjectNode) new ObjectMapper().readTree(new File(resource.toURI()));

        // Get Service Vendor count
        var serviceVendorCount = Integer.parseInt(
                given().spec(requestSpecification)
                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
            .get("/services/vendors")
            .then().assertThat() .statusCode(200)
            .extract().body().path("count{ it }").toString()
        );

        if(serviceVendorCount == 0) {
            given().spec(requestSpecification)
                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
            .contentType(ContentType.JSON)
            .body(serviceVendor)
            .post("/services/vendors/")
            .then().assertThat() .statusCode(200);
        }

        var serviceVendorId = given().spec(requestSpecification)
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/services/vendors")
        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)))
        .extract().body().path("[0].id").toString();

        serviceOfferingJson.put("serviceVendorId", serviceVendorId);


        var serviceOfferingCount = Integer.parseInt(
            given().spec(requestSpecification)
                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
            .get("/services/offerings/?withImage=false")
            .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (0)))
            .extract().body().path("count{ it }").toString()
        );

        if(serviceOfferingCount == 0) {
            given().spec(requestSpecification)
                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
            .contentType(ContentType.JSON)
            .body(serviceOfferingJson)
            .post("/services/offerings/")
            .then()
            .assertThat().statusCode(anyOf(is(200), is(500)));
        }

        given().spec(requestSpecification)
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/services/offerings/?withImage=false")
        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)));

        return;
    }

    @Order(40)
    @Test
    @DisplayName("Deploy Service")
    public void deployService() throws URISyntaxException, IOException, InterruptedException {
        RequestSpecification requestSpecification = RestAssured.given()
            .baseUri(TestConfig.SERVICE_MANAGEMENT_BASE_URL)
            .port(TestConfig.SERVICE_MANAGEMENT_PORT);

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("service-order.json");
        ObjectNode serviceOrderJson = (ObjectNode) new ObjectMapper().readTree(new File(resource.toURI()));

        String resources = given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/resources/")
        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)))
        .extract().body().toString();

        // Get Resource ID
        String resourceId = given()
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/resources/")
        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)))
        .extract().body().path("[0].id").toString();

        // Get Service Offering ID
        String serviceOfferingId = given().spec(requestSpecification)
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/services/offerings/?withImage=false")
        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)))
        .extract().body().path("[0].id").toString();

//        serviceOrderJson.put("projectAbbreviation", "fabos");

        // Get PreOrder Service Instance Count
        int preOrderServiceInstanceCount = Integer.parseInt(
            given().spec(requestSpecification)
                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
            .get("/services/instances")
            .then().assertThat() .statusCode(200)
            .extract().body().path("count{ it }").toString()
        );

        // Order Service
        given().spec(requestSpecification)
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .contentType(ContentType.JSON)
        .queryParam("resourceId", resourceId)
        .body(serviceOrderJson)
        .post("/services/offerings/"+serviceOfferingId+"/order")
        .then()
        .assertThat() .statusCode( anyOf(is(200),is(500)) );

        int postOrderServiceInstanceCount = preOrderServiceInstanceCount;

        long start_time = System.currentTimeMillis();
        long timeout = 240000;

        while(
            postOrderServiceInstanceCount == preOrderServiceInstanceCount &&
            System.currentTimeMillis() < start_time + timeout
        ) {
            // Get Service Instance Count
            postOrderServiceInstanceCount = Integer.parseInt(
                given().spec(requestSpecification)
                    .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                    .log().all()
                .get("/services/instances")
                .then().assertThat() .statusCode(200)
                .extract().body().path("count{ it }").toString()
            );

            System.out.println("Wait for order to be finished.");
            Thread.sleep(10000);;
        }

        // Check if Pre-/PostOrder Count diffs
        given().spec(requestSpecification)
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/services/instances")
        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (preOrderServiceInstanceCount+1)));

        return;
    }

    @Order(50)
    @Test
    @DisplayName("Undeploy docker service")
    public void undeployService() throws InterruptedException {
        RequestSpecification requestSpecification = RestAssured.given()
            .baseUri(TestConfig.SERVICE_MANAGEMENT_BASE_URL)
            .port(TestConfig.SERVICE_MANAGEMENT_PORT);

        // Get Service Instance ID
        String serviceInstanceId = given().spec(requestSpecification)
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("services/instances")
        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)))
        .extract().body().path("[0].id").toString();

        // Get PreUndeploy Service Instance Count
        int preUndeployServiceInstanceCount = Integer.parseInt(
            given().spec(requestSpecification)
                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
            .get("/services/instances")
            .then().assertThat() .statusCode(200)
            .extract().body().path("count{ it }").toString()
        );

        // Delete Service Instance ID
        given().spec(requestSpecification)
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .delete("/services/instances/" + serviceInstanceId)
        .then().assertThat() .statusCode(200);

        int postUndeployServiceInstanceCount = preUndeployServiceInstanceCount;

        long start_time = System.currentTimeMillis();
        long timeout = 240000;

        while(
            postUndeployServiceInstanceCount == preUndeployServiceInstanceCount &&
            System.currentTimeMillis() < start_time + timeout
        ) {
            postUndeployServiceInstanceCount = Integer.parseInt(
                given().spec(requestSpecification)
                    .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                    .log().all()
                .get("/services/instances")
                .then().assertThat() .statusCode(200)
                .extract().body().path("count{ it }").toString()
            );

            System.out.println("Wait for undeploy to be finished.");
            Thread.sleep(10000);;
        }

        given().spec(requestSpecification)
            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .get("/services/instances")
        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (preUndeployServiceInstanceCount-1)));


        return;
    }

    @Order(60)
    @Test
    @DisplayName("Remove TransferApp deployment capability")
    public void removeTransferAppDeploymentCapability() throws InterruptedException {
        List<TestResource> testResourceList =  getTestResources().collect(Collectors.toList());
        int resourceCount = testResourceList.size();
        String dcName = "TRANSFERAPP";
        int resourceIndex = 0;

        var resourceId = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .get("/resources")
                .then()
                .assertThat() .statusCode(200).body("$", hasSize(lessThanOrEqualTo (resourceCount))).extract().body().path("find{ it.ip == '"+testResourceList.get(resourceIndex).ip+"' }.id").toString();

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .queryParam("deploymentCapability", dcName)
                .delete("/resources/" + resourceId + "/deployment-capabilities")
                .then()
                .assertThat() .statusCode(200);

        long start_time = System.currentTimeMillis();
        long timeout = 300000;
        while (System.currentTimeMillis() < start_time + timeout) {

            var deploymentCapabilities = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                    .get("/resources/" + resourceId + "/deployment-capabilities")
                    .then()
                    .assertThat() .statusCode(200).extract().body().as(new TypeRef<List<Object>>(){});

            if (deploymentCapabilities.size() == 0)
            {
                return;
            }
            else
            {
                Thread.sleep(10000);
            }
        }

        Assertions.fail("Remove of deployment capability timed out");
    }

    @Order(70)
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Delete resource")
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
