package org.eclipse.slm.resource_management.service.rest.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceAlreadyExistsException;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotReachableException;
import org.eclipse.slm.resource_management.model.resource.BasicResource;
import org.eclipse.slm.resource_management.service.rest.Application;
import org.eclipse.slm.resource_management.service.rest.capabilities.CapabilitiesManager;
import org.eclipse.slm.resource_management.service.rest.utils.AuthorizationHeaderRequestFactory;
import org.eclipse.slm.resource_management.service.rest.utils.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = { Application.class, ResourcesRestController.class, ResourcesManager.class, CapabilitiesManager.class },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 34567)
@ComponentScan(basePackages = { "org.eclipse.slm.resource_management", "org.eclipse.slm.resource_management.service.rest" })
@Disabled
public class ResourcesRestControllerIT {

    private final static Logger LOG = LoggerFactory.getLogger(ResourcesRestControllerIT.class);

    public final static String BASE_PATH = "/resources";

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourcesRestController controller;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private AuthorizationHeaderRequestFactory authorizationHeaderRequestFactory;

    @Autowired
    private ResourcesRestControllerITHelper resourcesRestControllerITHelper;

    @BeforeEach
    public void beforeEach() throws ConsulLoginFailedException {
        this.testUtils.cleanConsul();
        // Mock configuration
    }

    @AfterEach
    public void afterEach() throws ConsulLoginFailedException {
        this.wireMockServer.resetAll();
        this.testUtils.cleanConsul();
    }

    @Test
    @DisplayName("Application Context loads")
    public void contextLoads() {
        Assertions.assertThat(controller).isNotNull();
    }

    @Nested
    @DisplayName("Get existing resource (GET /resources/{resourceId})")
    public  class getResource {
        private final static String PATH = BASE_PATH + "/";

        private String testProject = "testProject";

        @Test
        @DisplayName("Resource doesn't exist")
        public void noResources() throws Exception {
            var path = ResourcesRestControllerIT.BASE_PATH + "/" + UUID.randomUUID();

            mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                            get(path)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
        }

        @Test
        @DisplayName("Resource exists")
        public void resourceExists() throws Exception {
            var resourceId = resourcesRestControllerITHelper.addExistingResource(
                    testProject,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE_CREDENTIAL,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE,
                    false,
                    MockMvcResultMatchers.status().isCreated()
            );

            var resource = resourcesRestControllerITHelper.getResource(resourceId);

            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getHostname(), resource.getHostname());
            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getIp(), resource.getIp());
            //TODO Replace with access to credentials via Remote Access Service
//            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE_CREDENTIAL.getUsername(), resource.getUsername());
//            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE_CREDENTIAL.getPassword(), resource.getPassword());
            assertEquals(testProject, resource.getProject());
        }
    }

    @Nested
    @DisplayName("Get existing resources (GET /resources/)")
    public  class getExistingResources {
        private final static String PATH = BASE_PATH + "/";

        private String testProject = "testProject";

        @Test
        @DisplayName("No resources present")
        public void noResources() throws Exception {
            var resources = resourcesRestControllerITHelper.getResources();

            assertEquals(resources.size(), 0);
        }

        @Test
        @DisplayName("Two resources present")
        public void twoResourcesPresent() throws Exception {
            resourcesRestControllerITHelper.addExistingResource(
                    testProject,
                    ResourcesRestControllerITConfig.EXISTING_RESOURCE_CREDENTIAL,
                    ResourcesRestControllerITConfig.EXISTING_RESOURCE,
                    false,
                    MockMvcResultMatchers.status().isCreated()
            );

            resourcesRestControllerITHelper.addExistingResource(
                    testProject,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE_CREDENTIAL,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE,
                    false,
                    MockMvcResultMatchers.status().isCreated()
            );

            var resources = resourcesRestControllerITHelper.getResources();
            assertEquals(resources.size(), 2);

            var optionalFictionalResource = resources.stream().filter(r -> r.getHostname().equals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getHostname())).findAny();
            assertTrue(optionalFictionalResource.isPresent());
            var fictionalResource = optionalFictionalResource.get();
            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getHostname(), fictionalResource.getHostname());
            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getIp(), fictionalResource.getIp());

//            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getUsername(), fictionalResource.getUsername());
//            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getPassword(), fictionalResource.getPassword());
            assertEquals(testProject, fictionalResource.getProject());

            var optionalExistingResource = resources.stream().filter(r -> r.getHostname().equals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getHostname())).findAny();
            assertTrue(optionalExistingResource.isPresent());
            var existingResource = optionalExistingResource.get();
            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getHostname(), existingResource.getHostname());
            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getIp(), existingResource.getIp());
            //TODO Replace with access to credentials via Remote Access Service
//            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getUsername(), existingResource.getUsername());
//            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getPassword(), existingResource.getPassword());
            assertEquals(testProject, existingResource.getProject());
        }
    }

    @Nested
    @DisplayName("Add existing resource (PUT /resources/)")
    public  class addExistingResource {
        private final static String PATH = BASE_PATH + "/";

        private String testProject = "testProject";

        @Test
        @DisplayName("Without resource check (via AWX)")
        public void withoutResourceCheck() throws Exception {
            resourcesRestControllerITHelper.addExistingResource(
                    testProject,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE_CREDENTIAL,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE,
                    false,
                    MockMvcResultMatchers.status().isCreated()
            );

            var responseContent = mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                    get(PATH)
                        .contentType(MediaType.APPLICATION_JSON)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

            var objectMapper = new ObjectMapper();
            objectMapper.registerModule(new KotlinModule());
            var resources = objectMapper.readValue(responseContent, new TypeReference<List<BasicResource>>(){});

            assertEquals(resources.size(), 1);
            var createdResource = resources.get(0);
            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getHostname(), createdResource.getHostname());
            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getIp(), createdResource.getIp());
            //TODO Replace with access to credentials via Remote Access Service
//            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getUsername(), createdResource.getUsername());
//            assertEquals(ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getPassword(), createdResource.getPassword());
            assertEquals(testProject, createdResource.getProject());
        }

        @Test
        @DisplayName("With resource check (via AWX) - Resource not reachable")
        public void withResourceCheckButResourceNotReachable() throws Exception {
            var path = ResourcesRestControllerIT.BASE_PATH + "/";
            var response = mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                            put(path)
                                    .queryParam("project", testProject)
                                    .queryParam("resourceUsername", ResourcesRestControllerITConfig.FICTIONAL_RESOURCE_CREDENTIAL.getUsername())
                                    .queryParam("resourcePassword", ResourcesRestControllerITConfig.FICTIONAL_RESOURCE_CREDENTIAL.getPassword())
                                    .queryParam("resourceHostname", ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getHostname())
                                    .queryParam("resourceIp", ResourcesRestControllerITConfig.FICTIONAL_RESOURCE.getIp())
                                    .queryParam("checkResource", "true")
                                    .contentType(MediaType.APPLICATION_JSON)))
                    .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotReachableException));
        }

        @Test
        @DisplayName("With resource check (via AWX)")
        public void withResourceCheck() throws Exception {
            resourcesRestControllerITHelper.addExistingResource(
                    testProject,
                    ResourcesRestControllerITConfig.EXISTING_RESOURCE_CREDENTIAL,
                    ResourcesRestControllerITConfig.EXISTING_RESOURCE,
                    true,
                    MockMvcResultMatchers.status().isCreated()
            );

            long start_time = System.currentTimeMillis();
            long timeout = 60000;
            var responseContent = "";
            while (System.currentTimeMillis() < start_time + timeout) {
                responseContent = mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                                get(PATH)
                                        .contentType(MediaType.APPLICATION_JSON)))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

                if (responseContent.length() > 5)
                {
                    break;
                }
                else
                {
                    Thread.sleep(1000);
                }
            }

            assertTrue(responseContent.length() > 5);
            var objectMapper = new ObjectMapper();
            objectMapper.registerModule(new KotlinModule());
            var resources = objectMapper.readValue(responseContent, new TypeReference<List<BasicResource>>(){});

            assertEquals(resources.size(), 1);
            var createdResource = resources.get(0);
            assertEquals(ResourcesRestControllerITConfig.EXISTING_RESOURCE.getHostname(), createdResource.getHostname());
            assertEquals(ResourcesRestControllerITConfig.EXISTING_RESOURCE.getIp(), createdResource.getIp());
            //TODO Replace with access to credentials via Remote Access Service
//            assertEquals(ResourcesRestControllerITConfig.EXISTING_RESOURCE.getUsername(), createdResource.getUsername());
//            assertEquals(ResourcesRestControllerITConfig.EXISTING_RESOURCE.getPassword(), createdResource.getPassword());
            assertEquals(testProject, createdResource.getProject());
        }
    }

    @Nested
    @DisplayName("Delete resources (DELETE /resources/)")
    public  class deleteResource {
        private final static String PATH = BASE_PATH + "/";

        private String testProject = "testProject";

        @Test
        @DisplayName("Resource doesn't exist")
        public void noResources() throws Exception {
            var path = ResourcesRestControllerIT.BASE_PATH + "/" + UUID.randomUUID();

            mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                            delete(path)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
        }

        @Test
        @DisplayName("Resource exists")
        public void resourceExists() throws Exception {
            var resourceId = resourcesRestControllerITHelper.addExistingResource(
                    testProject,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE_CREDENTIAL,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE,
                    false,
                    MockMvcResultMatchers.status().isCreated()
            );
            var resources = resourcesRestControllerITHelper.getResources();
            assertEquals(1, resources.size());

            var path = ResourcesRestControllerIT.BASE_PATH + "/" + resourceId;
            mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                            delete(path)))
                    .andExpect(status().isOk());
            resources = resourcesRestControllerITHelper.getResources();
            assertEquals(resources.size(), 0);
        }
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("Add deployment capability (PUT /{resourceId}/deployment-capability)")
    public  class addDeploymentCapability {
        private final static String PATH_SUFFIX = "/deployment-capabilities";

        private String testProject = "testProject";

        @Test
        @DisplayName("Add to non-existent resource")
        public void addDeploymentCapabilityToNonExistentResource() throws Exception {
            var resourceId = UUID.randomUUID();
            var path = ResourcesRestControllerIT.BASE_PATH + "/" + resourceId + PATH_SUFFIX;
            var deploymentCapabilityName = "DOCKER";

            mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                            put(path))
                                .queryParam("deploymentCapability", deploymentCapabilityName))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
        }

        @Test
        @DisplayName("Unsupported Deployment Capability")
        public void addDeploymentCapabilityNotSupported() throws Exception {
            var resourceId = resourcesRestControllerITHelper.addExistingResource(
                    testProject,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE_CREDENTIAL,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE,
                    false,
                    MockMvcResultMatchers.status().isCreated()
            );
            var path = ResourcesRestControllerIT.BASE_PATH + "/" + resourceId + PATH_SUFFIX;
            var deploymentCapabilityName = "K8S";

            mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                            put(path))
                                .queryParam("deploymentCapability", deploymentCapabilityName))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceAlreadyExistsException.DeploymentCapabilityNotSupported));
        }

        @Test
        @DisplayName("Unknown Deployment Capability")
        public void addDeploymentCapabilityUnknown() throws Exception {
            var resourceId = resourcesRestControllerITHelper.addExistingResource(
                    testProject,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE_CREDENTIAL,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE,
                    false,
                    MockMvcResultMatchers.status().isCreated()
            );
            var path = ResourcesRestControllerIT.BASE_PATH + "/" + resourceId + PATH_SUFFIX;
            var deploymentCapabilityName = "NonExistentCapability";

            mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                            put(path))
                                .queryParam("deploymentCapability", deploymentCapabilityName))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentTypeMismatchException));
        }

        @Test
        @DisplayName("Docker Deployment Capability")
        public void addDeploymentCapabilityDocker() throws Exception {
            var resourceId = resourcesRestControllerITHelper.addExistingResource(
                    testProject,
                    ResourcesRestControllerITConfig.EXISTING_RESOURCE_CREDENTIAL,
                    ResourcesRestControllerITConfig.EXISTING_RESOURCE,
                    false,
                    MockMvcResultMatchers.status().isCreated()
            );
            var path = ResourcesRestControllerIT.BASE_PATH + "/" + resourceId + PATH_SUFFIX;
            var deploymentCapabilityName = "DOCKER";

            var response = mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                put(path)
                    .queryParam("deploymentCapability", deploymentCapabilityName)
                    .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

            long start_time = System.currentTimeMillis();
            long timeout = 180000;
            var responseContent = "";
            while (System.currentTimeMillis() < start_time + timeout) {
                responseContent = mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                    get(path)
                        .contentType(MediaType.APPLICATION_JSON)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

                if (responseContent.length() > 5)
                {
                    LOG.info("Waiting until deployment capability 'DOCKER' is installed...");
                    break;
                }
                else
                {
                    Thread.sleep(1000);
                }
            }

//            assertTrue(responseContent.contains(DeploymentCapability.DOCKER.getName()));
        }
    }

    @Nested
    @DisplayName("Remove deployment capability (DELETE /{resourceId}/deployment-capability)")
    public  class removeDeploymentCapability {
        private final static String PATH_SUFFIX = "/deployment-capabilities";

        private String testProject = "testProject";

        @Test
        @DisplayName("Remove from non-existent resource")
        public void removeDeploymentCapabilityToNonExistentResource() throws Exception {
            var resourceId = UUID.randomUUID();
            var path = ResourcesRestControllerIT.BASE_PATH + "/" + resourceId + PATH_SUFFIX;
            var deploymentCapabilityName = "DOCKER";

            mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                                    delete(path))
                            .queryParam("deploymentCapability", deploymentCapabilityName))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
        }

        @Test
        @DisplayName("Unsupported Deployment Capability")
        public void removeDeploymentCapabilityNotSupported() throws Exception {
            var resourceId = resourcesRestControllerITHelper.addExistingResource(
                    testProject,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE_CREDENTIAL,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE,
                    false,
                    MockMvcResultMatchers.status().isCreated()
            );
            var path = ResourcesRestControllerIT.BASE_PATH + "/" + resourceId + PATH_SUFFIX;
            var deploymentCapabilityName = "K8S";

            mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                                    delete(path))
                            .queryParam("deploymentCapability", deploymentCapabilityName))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceAlreadyExistsException.DeploymentCapabilityNotSupported));
        }

        @Test
        @DisplayName("Unknown Deployment Capability")
        public void removeDeploymentCapabilityUnknown() throws Exception {
            var resourceId = resourcesRestControllerITHelper.addExistingResource(
                    testProject,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE_CREDENTIAL,
                    ResourcesRestControllerITConfig.FICTIONAL_RESOURCE,
                    false,
                    MockMvcResultMatchers.status().isCreated()
            );
            var path = ResourcesRestControllerIT.BASE_PATH + "/" + resourceId + PATH_SUFFIX;
            var deploymentCapabilityName = "NonExistentCapability";

            mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                                    delete(path))
                            .queryParam("deploymentCapability", deploymentCapabilityName))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentTypeMismatchException));
        }

        @Test
        @DisplayName("Docker Deployment Capability")
        public void removeDeploymentCapabilityDocker() throws Exception {
            var resourceId = resourcesRestControllerITHelper.addExistingResource(
                    testProject,
                    ResourcesRestControllerITConfig.EXISTING_RESOURCE_CREDENTIAL,
                    ResourcesRestControllerITConfig.EXISTING_RESOURCE,
                    false,
                    MockMvcResultMatchers.status().isCreated()
            );
            var path = ResourcesRestControllerIT.BASE_PATH + "/" + resourceId + PATH_SUFFIX;
            var deploymentCapabilityName = "DOCKER";

            var response = mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                            put(path)
                                    .queryParam("deploymentCapability", deploymentCapabilityName)
                                    .contentType(MediaType.APPLICATION_JSON)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse();

            long start_time = System.currentTimeMillis();
            long timeout = 180000;
            var responseContent = "";
            while (System.currentTimeMillis() < start_time + timeout) {
                responseContent = mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                                get(path)
                                        .contentType(MediaType.APPLICATION_JSON)))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

                if (responseContent.length() > 5)
                {
                    LOG.info("Waiting until deployment capability 'DOCKER' is installed...");
                    break;
                }
                else
                {
                    Thread.sleep(1000);
                }
            }
//            assertTrue(responseContent.contains(DeploymentCapability.DOCKER.getName()));

            response = mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                            delete(path)
                                    .queryParam("deploymentCapability", deploymentCapabilityName)
                                    .contentType(MediaType.APPLICATION_JSON)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse();

            start_time = System.currentTimeMillis();
            timeout = 180000;
            responseContent = "";
            while (System.currentTimeMillis() < start_time + timeout) {
                responseContent = mockMvc.perform(authorizationHeaderRequestFactory.createRequestWithAuthorizationHeader(
                                get(path)
                                        .contentType(MediaType.APPLICATION_JSON)))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

                if (responseContent.length() < 5)
                {
                    LOG.info("Waiting until deployment capability 'DOCKER' is removed...");
                    break;
                }
                else
                {
                    Thread.sleep(1000);
                }
            }

//            assertFalse(responseContent.contains(DeploymentCapability.DOCKER.getName()));
        }
    }
}
