package org.eclipse.slm.tests.stack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.eclipse.slm.tests.utils.KeycloakUtil;
import org.junit.jupiter.api.*;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * End-to-end coverage of a service deployment routed entirely through the AAS "deployment
 * submodel" operation (Tasks 12-19 of the AAS deployment submodel plan):
 * <p>
 * 1. A resource with an installed Docker deployment capability has a submodel whose semanticId
 *    is {@code https://eclipse.dev/slm/submodels/Deployment/1/0}
 *    ({@code DeploymentSubmodelTemplate.SEMANTIC_ID_VALUE}).
 * 2. {@code GET /services/offerings/deployment-targets?deploymentType=DOCKER_COMPOSE} returns that
 *    target.
 * 3. Ordering a service using the discovered target's {@code submodelId} deploys the service.
 * 4. Once the order has completed, a Consul service entry exists for the resulting service
 *    instance.
 * <p>
 * IMPORTANT: like every other test in this module, this is a black-box test that exercises a
 * fully deployed SLM stack (real Keycloak, Consul, resource-management, service-management, and
 * an AAS submodel registry) reachable at {@link TestConfig#HOST}. It cannot be executed in a
 * sandboxed/CI environment without that stack; it is only compile-verified there.
 * <p>
 * Structurally modeled on {@link DcDummyTest} (single resource, single non-cluster deployment
 * capability), but every endpoint/body/response shape below was verified against the current
 * controllers in this codebase rather than against the (partially stale) reference tests -- see
 * the per-step comments for specifics, and see the note on {@link #verifyDeploymentSubmodelRegistered()}
 * for the one part of this test that is best-effort due to the submodel registry's externally
 * reachable path not being pinned down anywhere in this repo.
 */
@DisplayName("AAS Deployment Operation")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AasDeploymentTest {

    // Deployment semanticId asserted against; see
    // common.aas: org.eclipse.slm.common.aas.submodels.deployment.DeploymentSubmodelTemplate.SEMANTIC_ID_VALUE
    private static final String DEPLOYMENT_SUBMODEL_SEMANTIC_ID = "https://eclipse.dev/slm/submodels/Deployment/1/0";

    private static final TestResource TEST_RESOURCE = TestConfig.testResourceList.get(0);

    private static String resourceId;
    private static String aasIdForResource;
    private static String serviceOfferingId;
    private static String serviceOfferingVersionId;
    private static String deploymentTargetSubmodelId;
    private static String serviceInstanceId;

    @BeforeAll
    public static void init() {
        RestAssured.baseURI = TestConfig.RESOURCE_MANAGEMENT_BASE_URL;
        RestAssured.port = TestConfig.RESOURCE_MANAGEMENT_PORT;
        RestAssured.basePath = TestConfig.RESOURCE_MANAGEMENT_BASE_PATH;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Order(10)
    @Test
    @DisplayName("Add resource")
    public void addResource() {
        var resourceCreateRequest = new HashMap<String, Object>();
        resourceCreateRequest.put("resourceHostname", TEST_RESOURCE.hostname);
        resourceCreateRequest.put("resourceIp", TEST_RESOURCE.ip);
        resourceCreateRequest.put("fullPathOwnerGroupId", KeycloakUtil.getKeycloakFullPathUserGroupId());
        var digitalNameplate = new HashMap<String, String>();
        digitalNameplate.put("uriOfTheProduct", "N/A");
        digitalNameplate.put("manufacturerName", "N/A");
        digitalNameplate.put("manufacturerProductDesignation", "N/A");
        digitalNameplate.put("addressInformation", "N/A");
        digitalNameplate.put("serialNumber", "N/A");
        resourceCreateRequest.put("digitalNameplateV3", digitalNameplate);

        resourceId = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .contentType(ContentType.JSON)
                .body(resourceCreateRequest)
                .post("/resources")
                .then()
                .assertThat().statusCode(201).extract().body().asString().replace("\"", "");

        try {
            UUID.fromString(resourceId);
        } catch (IllegalArgumentException exception) {
            Assertions.fail("Response string '" + resourceId + "' from addResource request is not a UUID");
        }

        // AAS id of an SLM-managed resource: "Resource_" + resourceId
        // (org.eclipse.slm.resource_management.common.aas.ResourceAas.AAS_ID_PREFIX)
        aasIdForResource = "Resource_" + resourceId;

        var credentialId = UUID.randomUUID();
        var credentialData = new HashMap<String, Object>();
        credentialData.put("credentialDataType", "USERNAME_PASSWORD");
        credentialData.put("username", TEST_RESOURCE.username);
        credentialData.put("password", TEST_RESOURCE.password);

        var credential = new HashMap<String, Object>();
        credential.put("id", credentialId.toString());
        credential.put("name", "aas-deployment-test-credential-" + credentialId);
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
        remoteAccessRequest.put("username", TEST_RESOURCE.username);
        remoteAccessRequest.put("connectionPort", 22);
        remoteAccessRequest.put("connectionType", "ssh");

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .contentType(ContentType.JSON)
                .body(remoteAccessRequest)
                .post("/resources/" + resourceId + "/remote-access")
                .then()
                .assertThat().statusCode(200);
    }

    @Order(20)
    @Test
    @DisplayName("Install Docker deployment capability on resource")
    public void addDockerDeploymentCapability() throws InterruptedException {
        // Capability name confirmed against resource_management.features.capabilities test fixtures
        // (e.g. CapabilityInitTest, DeploymentSubmodelTest, DeploymentSubmodelRepositoryTest), which
        // consistently use capability.setName("Docker") with supported deployment types including
        // DOCKER_COMPOSE.
        var dcName = "Docker";
        var capabilityId = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .get("/resources/capabilities")
                .then()
                .assertThat().statusCode(200).extract().body().path("find{ it.name == '" + dcName + "' }.id").toString();

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .queryParam("capabilityId", capabilityId)
                .queryParam("fullPathOwnerGroupId", KeycloakUtil.getKeycloakFullPathUserGroupId())
                .contentType(ContentType.JSON)
                .body(new HashMap<String, String>())
                .put("/resources/" + resourceId + "/capabilities")
                .then()
                .assertThat().statusCode(200);

        long start_time = System.currentTimeMillis();
        long timeout = 20000;
        while (System.currentTimeMillis() < start_time + timeout) {
            var deploymentCapabilities = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                    .get("/resources/" + resourceId + "/capabilities/services")
                    .then()
                    .assertThat().statusCode(200).extract().body().as(new TypeRef<List<Object>>() {
                    });

            if (deploymentCapabilities.size() > 0) {
                return;
            } else {
                Thread.sleep(2000);
            }
        }

        Assertions.fail("Install of Docker deployment capability timed out");
    }

    /**
     * Point 1: a submodel with the Deployment semanticId exists once the capability is installed.
     * <p>
     * BEST EFFORT / DISCLOSED LIMITATION: the AAS submodel registry's real, standard REST surface
     * ({@code GET /submodel-descriptors}, confirmed against
     * org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClient in the aas-sdk repository,
     * which itself lists all descriptors and filters client-side by semanticId - there is no
     * server-side semanticId query parameter) is used here. What is NOT independently confirmed in
     * this repository is the submodel registry's externally reachable base path from
     * {@link TestConfig#HOST}; see the comment on {@link TestConfig#SUBMODEL_REGISTRY_BASE_URL}.
     * If that base path turns out to be wrong for a given deployment, this step - and only this
     * step - would need its base URL adjusted; points 2-4 do not depend on it.
     */
    @Order(30)
    @Test
    @DisplayName("Deployment submodel with Deployment semanticId is registered")
    public void verifyDeploymentSubmodelRegistered() throws InterruptedException {
        var requestSpecification = RestAssured.given()
                .baseUri(TestConfig.SUBMODEL_REGISTRY_BASE_URL)
                .port(TestConfig.SUBMODEL_REGISTRY_PORT)
                .basePath(TestConfig.SUBMODEL_REGISTRY_BASE_PATH);

        long start_time = System.currentTimeMillis();
        long timeout = 30000;
        while (System.currentTimeMillis() < start_time + timeout) {
            var response = given().spec(requestSpecification)
                    .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                    .log().all()
                    .get("/submodel-descriptors")
                    .then()
                    .extract().response();

            if (response.statusCode() == 200) {
                List<Map<String, Object>> descriptors = response.jsonPath().getList("result");
                boolean hasDeploymentSubmodel = descriptors.stream().anyMatch(descriptor -> {
                    var semanticId = (Map<String, Object>) descriptor.get("semanticId");
                    if (semanticId == null) {
                        return false;
                    }
                    List<Map<String, Object>> keys = (List<Map<String, Object>>) semanticId.get("keys");
                    return keys != null && keys.stream()
                            .anyMatch(key -> DEPLOYMENT_SUBMODEL_SEMANTIC_ID.equals(key.get("value")));
                });

                if (hasDeploymentSubmodel) {
                    return;
                }
            }

            Thread.sleep(3000);
        }

        Assertions.fail("Timed out waiting for a submodel with semanticId '" + DEPLOYMENT_SUBMODEL_SEMANTIC_ID
                + "' to be registered for resource " + resourceId);
    }

    /**
     * Point 2: {@code GET /services/offerings/deployment-targets?deploymentType=DOCKER_COMPOSE}
     * (ServiceOfferingVersionDeploymentRestController#getDeploymentTargets) returns our resource as
     * a deployment target, and its {@code submodelId} is captured for the order in point 3.
     */
    @Order(40)
    @Test
    @DisplayName("Resource is discoverable as a DOCKER_COMPOSE deployment target")
    public void discoverDeploymentTarget() throws InterruptedException {
        var requestSpecification = RestAssured.given()
                .baseUri(TestConfig.SERVICE_MANAGEMENT_BASE_URL)
                .port(TestConfig.SERVICE_MANAGEMENT_PORT)
                .basePath(TestConfig.SERVICE_MANAGEMENT_BASE_PATH);

        long start_time = System.currentTimeMillis();
        long timeout = 30000;
        while (System.currentTimeMillis() < start_time + timeout) {
            var deploymentTargets = given().spec(requestSpecification)
                    .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                    .log().all()
                    .queryParam("deploymentType", "DOCKER_COMPOSE")
                    .get("/services/offerings/deployment-targets")
                    .then()
                    .assertThat().statusCode(200)
                    .extract().body().as(new TypeRef<List<Map<String, Object>>>() {
                    });

            var matchingTarget = deploymentTargets.stream()
                    .filter(target -> aasIdForResource.equals(target.get("aasId")))
                    .findFirst();

            if (matchingTarget.isPresent()) {
                deploymentTargetSubmodelId = (String) matchingTarget.get().get("submodelId");
                Assertions.assertNotNull(deploymentTargetSubmodelId,
                        "Deployment target for resource " + resourceId + " has no submodelId");
                return;
            }

            Thread.sleep(3000);
        }

        Assertions.fail("Timed out waiting for resource " + resourceId
                + " (aasId " + aasIdForResource + ") to show up as a DOCKER_COMPOSE deployment target");
    }

    @Order(50)
    @Test
    @DisplayName("Create Service Offering")
    public void createServiceOffering() throws Exception {
        var requestSpecification = RestAssured.given()
                .baseUri(TestConfig.SERVICE_MANAGEMENT_BASE_URL)
                .port(TestConfig.SERVICE_MANAGEMENT_PORT)
                .basePath(TestConfig.SERVICE_MANAGEMENT_BASE_PATH);

        var classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("service-offering.json");
        ObjectNode serviceOfferingJson = (ObjectNode) new ObjectMapper().readTree(new File(resource.toURI()));

        var serviceVendorCount = Integer.parseInt(
                given().spec(requestSpecification)
                        .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                        .log().all()
                        .get("/services/vendors")
                        .then().assertThat().statusCode(200)
                        .extract().body().path("count{ it }").toString()
        );

        if (serviceVendorCount == 0) {
            given().spec(requestSpecification)
                    .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                    .log().all()
                    .contentType(ContentType.JSON)
                    .body(TestConfig.TEST_SERVICE_VENDOR)
                    .post("/services/vendors/")
                    .then().assertThat().statusCode(200);
        }

        var serviceVendorId = given().spec(requestSpecification)
                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .get("/services/vendors")
                .then().assertThat().statusCode(200).body("$", hasSize(greaterThanOrEqualTo(1)))
                .extract().body().path("[0].id").toString();

        serviceOfferingJson.put("serviceVendorId", serviceVendorId);
        serviceOfferingJson.put("name", "AAS Deployment Test Service Offering");

        serviceOfferingId = given().spec(requestSpecification)
                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .contentType(ContentType.JSON)
                .body(serviceOfferingJson)
                .post("/services/offerings")
                .then()
                .assertThat().statusCode(200)
                .extract().body().path("id").toString();

        Assertions.assertNotNull(serviceOfferingId);
    }

    /**
     * Creates a DOCKER_COMPOSE version of the service offering. Endpoint and request body verified
     * against ServiceOfferingVersionsRestApi ({@code POST /services/offerings/{id}/versions}) and
     * ServiceOfferingVersionDTOApi / DeploymentDefinition / DockerComposeDeploymentDefinition -
     * neither of the plan's reference tests (DcDummyTest, DcDockerSwarmTest) create an explicit
     * service offering version, because the version concept post-dates them; ordering now requires
     * a {@code serviceOfferingVersionId} path segment (see Task 16/17), so this step is new.
     */
    @Order(60)
    @Test
    @DisplayName("Create DOCKER_COMPOSE Service Offering Version")
    public void createServiceOfferingVersion() {
        var requestSpecification = RestAssured.given()
                .baseUri(TestConfig.SERVICE_MANAGEMENT_BASE_URL)
                .port(TestConfig.SERVICE_MANAGEMENT_PORT)
                .basePath(TestConfig.SERVICE_MANAGEMENT_BASE_PATH);

        var deploymentDefinition = new HashMap<String, Object>();
        deploymentDefinition.put("deploymentType", "DOCKER_COMPOSE");
        deploymentDefinition.put("composeFile",
                "version: '3'\nservices:\n  aas-deployment-test:\n    image: hello-world\n");
        deploymentDefinition.put("dotEnvFile", "");
        deploymentDefinition.put("envFiles", new HashMap<String, String>());

        var serviceOfferingVersionRequest = new HashMap<String, Object>();
        serviceOfferingVersionRequest.put("serviceOfferingId", serviceOfferingId);
        serviceOfferingVersionRequest.put("version", "1.0.0");
        serviceOfferingVersionRequest.put("deploymentDefinition", deploymentDefinition);
        serviceOfferingVersionRequest.put("serviceOptionCategories", List.of());
        serviceOfferingVersionRequest.put("serviceRequirements", List.of());
        serviceOfferingVersionRequest.put("serviceRepositories", List.of());

        serviceOfferingVersionId = given().spec(requestSpecification)
                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .contentType(ContentType.JSON)
                .body(serviceOfferingVersionRequest)
                .post("/services/offerings/" + serviceOfferingId + "/versions")
                .then()
                .assertThat().statusCode(200)
                .extract().body().path("serviceOfferingVersionId").toString();

        Assertions.assertNotNull(serviceOfferingVersionId);
    }

    /**
     * Point 3: order the service offering version using the {@code submodelId} discovered in
     * {@link #discoverDeploymentTarget()}, via the CURRENT real endpoint/body shape:
     * {@code POST /services/offerings/{serviceOfferingId}/versions/{serviceOfferingVersionId}/order}
     * with a {@code ServiceOrder} body of {@code {serviceOptionValues, deploymentTargetSubmodelId}}
     * (see ServiceOfferingVersionDeploymentRestController#orderServiceOfferingVersionById and
     * ServiceOrder.kt). This differs from DcDummyTest.deployService(), which posts to
     * {@code /services/offerings/{serviceOfferingId}/order} with a {@code resourceId} query param -
     * that shape no longer exists (Tasks 16/17 replaced the resourceId concept with
     * deploymentTargetSubmodelId inside the request body, and added the mandatory
     * serviceOfferingVersionId path segment).
     */
    @Order(70)
    @Test
    @DisplayName("Deploy Service via discovered AAS deployment target")
    public void deployServiceViaDeploymentTarget() throws InterruptedException {
        var requestSpecification = RestAssured.given()
                .baseUri(TestConfig.SERVICE_MANAGEMENT_BASE_URL)
                .port(TestConfig.SERVICE_MANAGEMENT_PORT)
                .basePath(TestConfig.SERVICE_MANAGEMENT_BASE_PATH);

        var preOrderServiceInstanceCount = Integer.parseInt(
                given().spec(requestSpecification)
                        .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                        .log().all()
                        .get("/services/instances")
                        .then().assertThat().statusCode(200)
                        .extract().body().path("count{ it }").toString()
        );

        var serviceOrderRequest = new HashMap<String, Object>();
        serviceOrderRequest.put("serviceOptionValues", List.of());
        serviceOrderRequest.put("deploymentTargetSubmodelId", deploymentTargetSubmodelId);

        given().spec(requestSpecification)
                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .contentType(ContentType.JSON)
                .body(serviceOrderRequest)
                .post("/services/offerings/" + serviceOfferingId + "/versions/" + serviceOfferingVersionId + "/order")
                .then()
                .assertThat().statusCode(anyOf(is(200), is(500)));

        var postOrderServiceInstanceCount = preOrderServiceInstanceCount;
        long start_time = System.currentTimeMillis();
        long timeout = 60000;

        while (postOrderServiceInstanceCount == preOrderServiceInstanceCount
                && System.currentTimeMillis() < start_time + timeout) {
            postOrderServiceInstanceCount = Integer.parseInt(
                    given().spec(requestSpecification)
                            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                            .get("/services/instances")
                            .then().assertThat().statusCode(200)
                            .extract().body().path("count{ it }").toString()
            );

            if (postOrderServiceInstanceCount == preOrderServiceInstanceCount) {
                System.out.println("Wait for order to be finished.");
                Thread.sleep(5000);
            }
        }

        var instancesResponse = given().spec(requestSpecification)
                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .get("/services/instances")
                .then().assertThat().statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(preOrderServiceInstanceCount + 1)))
                .extract().response();

        var instanceIds = instancesResponse.jsonPath().getList("id", String.class);
        serviceInstanceId = instanceIds.get(instanceIds.size() - 1);

        Assertions.assertNotNull(serviceInstanceId, "No service instance id captured after ordering");
    }

    /**
     * Point 4: once the order has completed, a Consul service entry for the resulting service
     * instance exists. Consul service naming (
     * {@code "service-instance_" + serviceInstanceId}) confirmed against
     * ServiceInstancesConsulClient#getConsulServiceNameForServiceInstance /
     * #convertServiceInstanceToConsulService in service_management.features.service_deployment.impl.
     * No REST-facing endpoint in resource_management/service_management surfaces Consul
     * registration directly, so - matching this test module's existing precedent in
     * ConsulTests.java, which also talks to Consul's own HTTP API directly via
     * TestConfig.CONSUL_BASE_URL/CONSUL_PORT - this queries Consul's standard catalog API directly.
     */
    @Order(80)
    @Test
    @DisplayName("Consul service entry exists for the deployed service instance")
    public void verifyConsulServiceEntryExists() throws InterruptedException {
        var consulServiceName = "service-instance_" + serviceInstanceId;

        long start_time = System.currentTimeMillis();
        long timeout = 30000;
        while (System.currentTimeMillis() < start_time + timeout) {
            var response = given()
                    .baseUri(TestConfig.CONSUL_BASE_URL)
                    .port(TestConfig.CONSUL_PORT)
                    .log().all()
                    .get("/v1/catalog/service/" + consulServiceName)
                    .then()
                    .extract().response();

            if (response.statusCode() == 200) {
                List<Object> catalogEntries = response.jsonPath().getList("$");
                if (catalogEntries != null && !catalogEntries.isEmpty()) {
                    return;
                }
            }

            Thread.sleep(3000);
        }

        Assertions.fail("Timed out waiting for Consul service entry '" + consulServiceName + "' to appear");
    }

    @Order(90)
    @Test
    @DisplayName("Undeploy service")
    public void undeployService() throws InterruptedException {
        var requestSpecification = RestAssured.given()
                .baseUri(TestConfig.SERVICE_MANAGEMENT_BASE_URL)
                .port(TestConfig.SERVICE_MANAGEMENT_PORT)
                .basePath(TestConfig.SERVICE_MANAGEMENT_BASE_PATH);

        var preUndeployServiceInstanceCount = Integer.parseInt(
                given().spec(requestSpecification)
                        .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                        .log().all()
                        .get("/services/instances")
                        .then().assertThat().statusCode(200)
                        .extract().body().path("count{ it }").toString()
        );

        given().spec(requestSpecification)
                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .delete("/services/instances/" + serviceInstanceId)
                .then().assertThat().statusCode(200);

        var postUndeployServiceInstanceCount = preUndeployServiceInstanceCount;
        long start_time = System.currentTimeMillis();
        long timeout = 240000;

        while (postUndeployServiceInstanceCount == preUndeployServiceInstanceCount
                && System.currentTimeMillis() < start_time + timeout) {
            postUndeployServiceInstanceCount = Integer.parseInt(
                    given().spec(requestSpecification)
                            .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                            .get("/services/instances")
                            .then().assertThat().statusCode(200)
                            .extract().body().path("count{ it }").toString()
            );

            if (postUndeployServiceInstanceCount == preUndeployServiceInstanceCount) {
                System.out.println("Wait for undeploy to be finished.");
                Thread.sleep(5000);
            }
        }

        given().spec(requestSpecification)
                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .get("/services/instances")
                .then().assertThat().statusCode(200)
                .body("$", hasSize(lessThanOrEqualTo(preUndeployServiceInstanceCount - 1)));
    }

    @Order(100)
    @Test
    @DisplayName("Remove Docker deployment capability")
    public void removeDockerDeploymentCapability() throws InterruptedException {
        var dcName = "Docker";
        var capabilityId = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .get("/resources/capabilities")
                .then()
                .assertThat().statusCode(200).body("$", hasSize(greaterThanOrEqualTo(1)))
                .extract().body().path("find{ it.name == '" + dcName + "' }.id").toString();

        given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .queryParam("capabilityId", capabilityId)
                .delete("/resources/" + resourceId + "/capabilities")
                .then()
                .assertThat().statusCode(200);

        long start_time = System.currentTimeMillis();
        long timeout = 20000;
        while (System.currentTimeMillis() < start_time + timeout) {
            var deploymentCapabilities = given().auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                    .get("/resources/" + resourceId + "/capabilities/services")
                    .then()
                    .assertThat().statusCode(200).extract().body().as(new TypeRef<List<Object>>() {
                    });

            if (deploymentCapabilities.size() == 0) {
                return;
            } else {
                Thread.sleep(2000);
            }
        }

        Assertions.fail("Removal of Docker deployment capability timed out");
    }

    @Order(110)
    @Test
    @DisplayName("Delete resource")
    public void deleteResource() {
        given()
                .auth().preemptive().oauth2(KeycloakUtil.getKeycloakAccessToken())
                .log().all()
                .delete("/resources/" + resourceId)
                .then()
                .assertThat().statusCode(200);
    }
}
