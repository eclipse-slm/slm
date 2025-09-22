package org.eclipse.slm.resource_management.common.resources;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.assertj.core.api.Assertions;
import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.resource_types.ResourceTypesManager;
import org.eclipse.slm.resource_management.common.test_utils.AuthorizationHeaderRequestFactory;
import org.eclipse.slm.resource_management.common.test_utils.TestUtils;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {
        ResourcesRestController.class,
        AuthorizationHeaderRequestFactory.class,
        ResourcesRestControllerITHelper.class,
        ConsulNodesApiClient.class,
        ConsulAclApiClient.class,
        RestTemplate.class,
        TestUtils.class
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 34567)
public class ResourcesRestControllerIT {

    private final static Logger LOG = LoggerFactory.getLogger(ResourcesRestControllerIT.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourcesRestController controller;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ResourcesRestControllerITHelper resourcesRestControllerITHelper;

    @MockBean
    private ResourcesManager resourcesManager;
    @MockBean
    private ResourceTypesManager resourceTypesManager;
    @MockBean
    private ConsulNodesApiClient consulNodesApiClient;
    @MockBean
    private ConsulAclApiClient consulAclApiClient;

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
    @DisplayName("Get existing resource (GET " + ResourcesRestControllerConfig.BASE_PATH + "/{resourceId})")
    public  class getResource {

        @Test
        @DisplayName("Unauthenticated request")
        public void unauthenticatedRequest() throws Exception {
            var resourceId = UUID.randomUUID();
            var path = ResourcesRestControllerConfig.BASE_PATH + "/" + resourceId;

            when(resourcesManager.getResourceByIdOrThrow(any(JwtAuthenticationToken.class), eq(resourceId)))
                    .thenThrow(new ResourceNotFoundException(resourceId));

            mockMvc.perform(
                            get(path))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Resource doesn't exist")
        @WithJwt("default_user.json")
        public void noResources() throws Exception {
            var resourceId = UUID.randomUUID();
            var path = ResourcesRestControllerConfig.BASE_PATH + "/" + resourceId;

            when(resourcesManager.getResourceByIdOrThrow(any(JwtAuthenticationToken.class), eq(resourceId)))
                    .thenThrow(new ResourceNotFoundException(resourceId));

            mockMvc.perform(
                            get(path))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
        }

        @Test
        @DisplayName("Resource exists")
        @WithJwt("default_user.json")
        public void resourceExists() throws Exception {
            var testResource = ResourcesRestControllerITHelper.getTestResource();
            when(resourcesManager.getResourceByIdOrThrow(any(JwtAuthenticationToken.class), eq(testResource.getId())))
                    .thenReturn(testResource);

            var receivedResource = resourcesRestControllerITHelper.getResource(testResource.getId());

            assertEquals(testResource.getHostname(), receivedResource.getHostname());
            assertEquals(testResource.getIp(), receivedResource.getIp());
        }
    }

    @Nested
    @DisplayName("Get existing resources (GET " + ResourcesRestControllerConfig.BASE_PATH + ")")
    public  class getExistingResources {

        @Test
        @DisplayName("Unauthenticated request")
        public void unauthenticatedRequest() throws Exception {
            var resourceId = UUID.randomUUID();
            var path = ResourcesRestControllerConfig.BASE_PATH;

            when(resourcesManager.getResourceByIdOrThrow(any(JwtAuthenticationToken.class), eq(resourceId)))
                    .thenThrow(new ResourceNotFoundException(resourceId));

            mockMvc.perform(
                            get(path))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("No resources present")
        @WithJwt("default_user.json")
        public void noResources() throws Exception {
            var resources = resourcesRestControllerITHelper.getResources();

            assertEquals(resources.size(), 0);
        }

        @Test
        @DisplayName("Two resources present")
        @WithJwt("default_user.json")
        public void twoResourcesPresent() throws Exception {
            var testResource1 = ResourcesRestControllerITHelper.getTestResource();
            var testResource2 = ResourcesRestControllerITHelper.getTestResource();
            when(resourcesManager.getResources(any(JwtAuthenticationToken.class)))
                    .thenReturn(List.of(testResource1, testResource2));

            var resources = resourcesRestControllerITHelper.getResources();
            assertEquals(2, resources.size());

            var optionalFictionalResource = resources.stream().filter(r -> r.getId().equals(testResource1.getId())).findAny();
            assertTrue(optionalFictionalResource.isPresent());
            var fictionalResource = optionalFictionalResource.get();
            assertEquals(testResource1.getHostname(), fictionalResource.getHostname());
            assertEquals(testResource1.getIp(), fictionalResource.getIp());

            var optionalExistingResource = resources.stream().filter(r -> r.getId().equals(testResource2.getId())).findAny();
            assertTrue(optionalExistingResource.isPresent());
            var existingResource = optionalExistingResource.get();
            assertEquals(testResource2.getHostname(), existingResource.getHostname());
            assertEquals(testResource2.getIp(), existingResource.getIp());
        }
    }

    @Nested
    @DisplayName("Add new resource (POST " + ResourcesRestControllerConfig.BASE_PATH + "/{resourceId})")
    public  class addResource {

        @Test
        @DisplayName("Unauthenticated request")
        public void unauthenticatedRequest() throws Exception {
            var resourceId = UUID.randomUUID();
            var path = ResourcesRestControllerConfig.BASE_PATH + "/" + resourceId;

            mockMvc.perform(
                            post(path)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Add new resource")
        @WithJwt("default_user.json")
        public void addNewResource() throws Exception {
            var testResource = ResourcesRestControllerITHelper.getTestResource();
            var path = ResourcesRestControllerConfig.BASE_PATH;
            var resourceCreateRequest = new CreateResourceRequest();
            resourceCreateRequest.setResourceHostname(testResource.getHostname());
            resourceCreateRequest.setResourceIp(testResource.getIp());
            var objectMapper = new ObjectMapper();
            var jsonBody = objectMapper.writeValueAsString(resourceCreateRequest);

            var response = mockMvc.perform(
                            post(path)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse();
        }
    }


    @Nested
    @DisplayName("Add or update existing resource (PUT " + ResourcesRestControllerConfig.BASE_PATH + "/{resourceId})")
    public  class addOrUpdateExistingResource {

        @Test
        @DisplayName("Unauthenticated request")
        public void unauthenticatedRequest() throws Exception {
            var resourceId = UUID.randomUUID();
            var path = ResourcesRestControllerConfig.BASE_PATH + "/" + resourceId;

            mockMvc.perform(
                            put(path)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Add existing resource")
        @WithJwt("default_user.json")
        public void addExistingResource() throws Exception {
            var testResource = ResourcesRestControllerITHelper.getTestResource();
            var path = ResourcesRestControllerConfig.BASE_PATH + "/" + testResource.getId();
            var resourceCreateRequest = new CreateResourceRequest();
            resourceCreateRequest.setResourceHostname(testResource.getHostname());
            resourceCreateRequest.setResourceIp(testResource.getIp());
            var objectMapper = new ObjectMapper();
            var jsonBody = objectMapper.writeValueAsString(resourceCreateRequest);

            var response = mockMvc.perform(
                            put(path).with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse();
        }
    }


    @Nested
    @DisplayName("Delete resources (DELETE /resources/)")
    public  class deleteResource {

        @Test
        @DisplayName("Unauthenticated request")
        public void unauthenticatedRequest() throws Exception {
            var resourceId = UUID.randomUUID();
            var path = ResourcesRestControllerConfig.BASE_PATH + "/" + resourceId;

            mockMvc.perform(
                            delete(path)
                                    .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Resource doesn't exist")
        @WithJwt("default_user.json")
        public void noResources() throws Exception {
            var resourceId = UUID.randomUUID();
            var path = ResourcesRestControllerConfig.BASE_PATH + "/" + resourceId;
            doThrow(new ResourceNotFoundException(resourceId))
                    .when(resourcesManager).deleteResource(any(JwtAuthenticationToken.class), eq(resourceId));

            mockMvc.perform(
                            delete(path).with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
        }

        @Test
        @DisplayName("Resource exists")
        @WithJwt("default_user.json")
        public void resourceExists() throws Exception {
            var testResource = ResourcesRestControllerITHelper.getTestResource();
            when(resourcesManager.getResources(any(JwtAuthenticationToken.class)))
                    .thenReturn(List.of(testResource));

            var path = ResourcesRestControllerConfig.BASE_PATH + "/" + testResource.getId();
            mockMvc.perform(
                        delete(path).with(csrf()))
                    .andExpect(status().isOk());

            verify(resourcesManager).deleteResource(
                    any(JwtAuthenticationToken.class),
                    eq(testResource.getId())
            );
        }
    }
}
