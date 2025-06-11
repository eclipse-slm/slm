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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("K3s Deployment Capability")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DcK3sTest {

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
    @DisplayName("Add K3s deployment capability")
    public void addK3sDeploymentCapability() throws InterruptedException {
        List<TestResource> testResourceList =  getTestResources().collect(Collectors.toList());
        int resourceCount = testResourceList.size();
        List<String> serverList = new ArrayList();
        List<String> agentList = new ArrayList();
        String serverName = "Server";
        String agentName = "Agent";
        String clusterTypeName = "K3S";

        for(int i = 0; i < 3; i++) {
            var resourceId = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                    .log().all()
                    .get("/resources")
                    .then()
                    .assertThat().statusCode(200).body("$", hasSize(lessThanOrEqualTo(resourceCount))).extract().body().path("find{ it.ip == '" + testResourceList.get(i).ip + "' }.id").toString();

            serverList.add(resourceId);
        }

        var resourceId = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .get("/resources")
                .then()
                .assertThat().statusCode(200).body("$", hasSize(lessThanOrEqualTo(resourceCount))).extract().body().path("find{ it.ip == '" + testResourceList.get(3).ip + "' }.id").toString();

        agentList.add(resourceId);

        Map<String,List<String>> clusterMemberMap = new HashMap() {{
           put(serverName, serverList);
           put(agentName, agentList);
        }};

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .queryParam("project", "fabos")
        .queryParam("clusterType", clusterTypeName)
        .contentType(ContentType.JSON)
        .body(clusterMemberMap)
        .post("/resources/clusters")
        .then()
        .assertThat() .statusCode(200);

        long start_time = System.currentTimeMillis();
        long timeout = 600000;
        while (System.currentTimeMillis() < start_time + timeout) {

            var cluster = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                    .get("/resources/clusters")
                    .then()
                    .assertThat() .statusCode(200).extract().body().as(new TypeRef<Map<String,List<Object>>>(){});

            if (cluster.size() > 0)
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

    @Order(30)
    @Test
    @DisplayName("Scale Up K3s")
    public void scaleUpK3s() throws InterruptedException {
        var clusters = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .get("/resources/clusters")
                .then()
                .assertThat() .statusCode(200).extract().body().as(new TypeRef<Map<String,List<Object>>>(){});

        String clusterName = clusters.entrySet().iterator().next().getKey();

        var clusterMembers = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .get("/resources/clusters/"+clusterName+"/members")
                .then()
            .assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (4)))
            .extract().body().as(new TypeRef<List<Map<String, Object>>>(){});

        List<TestResource> testResourceList =  getTestResources().collect(Collectors.toList());

        var resourceId = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
            .get("/resources")
            .then()
        .assertThat().statusCode(200).body("$", hasSize(lessThanOrEqualTo(5)))
        .extract().body().path("find{ it.ip == '" + testResourceList.get(4).ip + "' }.id").toString();

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .queryParam("project", "fabos")
        .queryParam("resourceId", resourceId)
        .post("/resources/clusters/"+clusterName+"/members")
        .then()
        .assertThat() .statusCode(200);

        // TODO: Check why always returns clusterMembers.size() == 5 even if scale up is not finished
        long start_time = System.currentTimeMillis();
        long timeout = 600000;
        while (System.currentTimeMillis() < start_time + timeout) {
            clusterMembers = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .get("/resources/clusters/"+clusterName+"/members")
                .then()
            .assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (4)))
            .extract().body().as(new TypeRef<List<Map<String, Object>>>(){});

            if (clusterMembers.size() > 4)
            {
                return;
            }
            else
            {
                Thread.sleep(10000);
            }
        }

        Assertions.fail("Scale up of Docker Swarm timed out");

        // TODO: remove when waiting for finished scale up operation works
        Thread.sleep(60000);

        return;
    }

    @Order(40)
    @Test
    @DisplayName("Scale Down K3s")
    public void scaleDownK3s() throws InterruptedException {
        var clusters = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .get("/resources/clusters")
                .then()
                .assertThat() .statusCode(200).extract().body().as(new TypeRef<Map<String,List<Object>>>(){});

        String clusterName = clusters.entrySet().iterator().next().getKey();

        var clusterMembers = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .get("/resources/clusters/"+clusterName+"/members")
                .then()
                .assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (5)))
                .extract().body().as(new TypeRef<List<Map<String, Object>>>(){});

        Map<String, Object> scaleDownHost = clusterMembers.stream()
                .filter(
                        cm -> cm.get("address").equals("192.168.0.154")
                )
                .findFirst()
                .get();

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .queryParam("resourceId", scaleDownHost.get("id"))
        .delete("/resources/clusters/"+clusterName+"/members")
        .then()
        .assertThat() .statusCode(200);

        long start_time = System.currentTimeMillis();
        long timeout = 600000;
        while (System.currentTimeMillis() < start_time + timeout) {
            clusterMembers = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .get("/resources/clusters/"+clusterName+"/members")
                .then()
            .assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (4)))
            .extract().body().as(new TypeRef<List<Map<String, Object>>>(){});

            if (clusterMembers.size() < 5)
            {
                return;
            }
            else
            {
                Thread.sleep(10000);
            }
        }

        Assertions.fail("Scale Down of K3s timed out");

        return;
    }

    @Order(50)
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

//    @Order(40)
//    @Test
//    @DisplayName("Deploy Service")
//    public void deployService() throws URISyntaxException, IOException, InterruptedException {
//        RequestSpecification requestSpecification = RestAssured.given()
//            .baseUri(TestConfig.SERVICE_REGISTRY_BASE_URL)
//            .port(TestConfig.SERVICE_REGISTRY_PORT);
//
//        ClassLoader classLoader = getClass().getClassLoader();
//        URL resource = classLoader.getResource("service-order.json");
//        ObjectNode serviceOrderJson = (ObjectNode) new ObjectMapper().readTree(new File(resource.toURI()));
//
//        // Get Resource ID
//        String resourceId = given()
//            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
//            .log().all()
//        .get("/resources/")
//        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)))
//        .extract().body().path("[0].id").toString();
//
//        // Get Service Offering ID
//        String serviceOfferingId = given().spec(requestSpecification)
//            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
//            .log().all()
//        .get("/services/offerings/?withImage=false")
//        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)))
//        .extract().body().path("[0].id").toString();
//
//        serviceOrderJson.put("projectAbbreviation", "fabos");
//
//        // Get PreOrder Service Instance Count
//        int preOrderServiceInstanceCount = Integer.parseInt(
//            given().spec(requestSpecification)
//                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
//                .log().all()
//            .get("/services/instances")
//            .then().assertThat() .statusCode(200)
//            .extract().body().path("count{ it }").toString()
//        );
//
//        // Order Service
//        given().spec(requestSpecification)
//            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
//            .log().all()
//        .contentType(ContentType.JSON)
//        .queryParam("resourceId", resourceId)
//        .body(serviceOrderJson)
//        .post("/services/offerings/"+serviceOfferingId+"/order")
//        .then()
//        .assertThat() .statusCode( anyOf(is(200),is(500)) );
//
//        int postOrderServiceInstanceCount = preOrderServiceInstanceCount;
//
//        long start_time = System.currentTimeMillis();
//        long timeout = 240000;
//
//        while(
//            postOrderServiceInstanceCount == preOrderServiceInstanceCount &&
//            System.currentTimeMillis() < start_time + timeout
//        ) {
//            // Get Service Instance Count
//            postOrderServiceInstanceCount = Integer.parseInt(
//                given().spec(requestSpecification)
//                    .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
//                    .log().all()
//                .get("/services/instances")
//                .then().assertThat() .statusCode(200)
//                .extract().body().path("count{ it }").toString()
//            );
//
//            System.out.println("Wait for order to be finished.");
//            Thread.sleep(10000);;
//        }
//
//        // Check if Pre-/PostOrder Count diffs
//        given().spec(requestSpecification)
//            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
//            .log().all()
//        .get("/services/instances")
//        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (preOrderServiceInstanceCount+1)));
//
//        return;
//    }
//
//    @Order(50)
//    @Test
//    @DisplayName("Undeploy docker service")
//    public void undeployService() throws InterruptedException {
//        RequestSpecification requestSpecification = RestAssured.given()
//            .baseUri(TestConfig.SERVICE_REGISTRY_BASE_URL)
//            .port(TestConfig.SERVICE_REGISTRY_PORT);
//
//        // Get Service Instance ID
//        String serviceInstanceId = given().spec(requestSpecification)
//            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
//            .log().all()
//        .get("services/instances")
//        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (1)))
//        .extract().body().path("[0].id").toString();
//
//        // Get PreUndeploy Service Instance Count
//        int preUndeployServiceInstanceCount = Integer.parseInt(
//            given().spec(requestSpecification)
//                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
//                .log().all()
//            .get("/services/instances")
//            .then().assertThat() .statusCode(200)
//            .extract().body().path("count{ it }").toString()
//        );
//
//        // Delete Service Instance ID
//        given().spec(requestSpecification)
//            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
//            .log().all()
//        .delete("/services/instances/" + serviceInstanceId)
//        .then().assertThat() .statusCode(200);
//
//        // TODO add wait for deployment finish
//        int postUndeployServiceInstanceCount = preUndeployServiceInstanceCount;
//
//        long start_time = System.currentTimeMillis();
//        long timeout = 240000;
//
//        while(
//            postUndeployServiceInstanceCount == preUndeployServiceInstanceCount &&
//            System.currentTimeMillis() < start_time + timeout
//        ) {
//            postUndeployServiceInstanceCount = Integer.parseInt(
//                given().spec(requestSpecification)
//                    .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
//                    .log().all()
//                .get("/services/instances")
//                .then().assertThat() .statusCode(200)
//                .extract().body().path("count{ it }").toString()
//            );
//
//            System.out.println("Wait for undeploy to be finished.");
//            Thread.sleep(10000);;
//        }
//
//        given().spec(requestSpecification)
//            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
//            .log().all()
//        .get("/services/instances")
//        .then().assertThat() .statusCode(200).body("$", hasSize(greaterThanOrEqualTo (preUndeployServiceInstanceCount-1)));
//
//
//        return;
//    }

    @Order(60)
    @Test
    @DisplayName("Remove K3s deployment capability")
    public void removeK3sDeploymentCapability() throws InterruptedException {
        var clusters = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .get("/resources/clusters")
                .then()
                .assertThat() .statusCode(200).extract().body().as(new TypeRef<Map<String,List<Object>>>(){});

        String clusterName = clusters.entrySet().iterator().next().getKey();


        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
            .log().all()
        .queryParam("project", "fabos")
        .delete("/resources/clusters/" + clusterName)
        .then()
        .assertThat() .statusCode(200);

        long start_time = System.currentTimeMillis();
        long timeout = 600000;
        while (System.currentTimeMillis() < start_time + timeout) {

            clusters = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                    .get("/resources/clusters")
                    .then()
                    .assertThat() .statusCode(200).extract().body().as(new TypeRef<Map<String,List<Object>>>(){});

            if (clusters.size() == 0)
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
