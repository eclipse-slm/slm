package org.eclipse.slm.service_management.service.rest.service_offerings;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.eclipse.slm.resource_management.service.client.ResourceManagementApiClientInitializer;
import org.eclipse.slm.service_management.model.offerings.*;
import org.eclipse.slm.service_management.model.offerings.options.*;
import org.eclipse.slm.service_management.service.rest.AbstractRestControllerIT;
import org.eclipse.slm.service_management.model.offerings.docker.compose.DockerComposeDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.docker.container.DockerContainerDeploymentDefinition;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.eclipse.slm.service_management.persistence.api.ServiceCategoryJpaRepository;
import org.eclipse.slm.service_management.persistence.api.ServiceOfferingJpaRepository;
import org.eclipse.slm.service_management.persistence.api.ServiceVendorJpaRepository;
import org.eclipse.slm.service_management.service.rest.Application;
import org.eclipse.slm.service_management.service.rest.service_categories.ServiceCategoryHandler;
import org.eclipse.slm.service_management.service.rest.service_deployment.ServiceDeploymentHandler;
import org.eclipse.slm.service_management.service.rest.mocks.AwxMockedResponses;
import org.eclipse.slm.service_management.service.rest.service_vendors.ServiceVendorHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.io.IOException;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {
                Application.class,
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
        classes = {
                ServiceOfferingRestController.class,
                ServiceOfferingHandler.class,
                ServiceOfferingGitUpdater.class,
                ServiceDeploymentHandler.class,
                ServiceVendorHandler.class,
                ServiceCategoryHandler.class,
                ResourceManagementApiClientInitializer.class
        }
)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 45678)
@ActiveProfiles("test")
@ComponentScan(basePackages = { "org.eclipse.slm.service_management" })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled
public class ServiceOfferingRestControllerIT extends AbstractRestControllerIT {

    private final static String BASE_PATH = "/services/offerings";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServiceOfferingRestController controller;

    @Autowired
    private ServiceVendorJpaRepository serviceVendorJpaRepository;

    @Autowired
    private ServiceOfferingJpaRepository serviceOfferingJpaRepository;

    @Autowired
    private ServiceCategoryJpaRepository serviceCategoryJpaRepository;

    private ServiceOffering serviceOfferingDockerContainer;

    private ServiceOffering serviceOfferingDockerCompose;

    @Autowired
    private transient WireMockServer wireMockServer;

    @Autowired
    private ResourceManagementApiClientInitializer resourceManagementApiClientInitializer;

    @BeforeAll
    public void init() throws IOException {
        this.addTestServiceOfferings();
    }

    @BeforeEach
    public void beforeEach()
    {
        // Mock AWX
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/jwt/token/"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "text/plain")
                        .withBody("FakeTokenKkaNZby2DOXFw9zrD6wuC")));

        wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathMatching("/api/v2/projects/"))
                        .withQueryParam("scm_type", WireMock.matching("(.*)"))
                        .withQueryParam("scm_url__icontains", WireMock.matching("(.*)"))
                        .withQueryParam("scm_branch", WireMock.matching("(.*)"))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", "application/json")
                                .withBody(AwxMockedResponses.GET_PROJECT_RESPONSE_JSON)));

        wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathMatching("/api/v2/job_templates/"))
                        .withQueryParam("project", WireMock.matching("(.*)"))
                        .withQueryParam("playbook", WireMock.matching("(.*)"))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", "application/json")
                                .withBody(AwxMockedResponses.GET_JOB_TEMPLATE_RESPONSE_JSON)));

        wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathMatching("/api/v2/jobs/(.*)/"))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", "application/json")
                                .withBody(AwxMockedResponses.GET_JOB_RESPONSE_JSON)));

        wireMockServer.stubFor(
                WireMock.post(WireMock.urlPathMatching("/observer/job"))
                        .withQueryParam("userUuid", WireMock.matching("(.*)"))
                        .withQueryParam("jobId", WireMock.matching("(.*)"))
                        .withQueryParam("jobTarget", WireMock.matching("(.*)"))
                        .withQueryParam("jobGoal", WireMock.matching("(.*)"))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())));

        // Mock Resource Management
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathMatching("/resources/.*/deployment-capabilities"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [{
                                "prettyName": "Shell",
                                "logo": "mdi-bash",
                                "name": "SHELL",
                                "default": true,
                                "cluster": false
                            },
                            {
                                "prettyName": "Docker",
                                "logo": "mdi-docker",
                                "name": "DOCKER",
                                "default": false,
                                "cluster": false
                            }]
                        """)));
    }

    @AfterEach
    public void afterEach()
    {
        this.wireMockServer.resetAll();
    }

    @Nested
    @DisplayName("Order Service Offering (" + BASE_PATH + "/{serviceOfferingId}/order)")
    @WithMockJwtAuth(
            claims = @OpenIdClaims(
                    iss = "https://localhost/auth/realms/vfk",
                    preferredUsername = "testUser123"
            )
    )
    public class orderServiceOfferingById {

        private String testResourceId = "11a8ada6-0f00-4e70-a41d-7d562c6e24a4";

        @Nested
        @DisplayName("Docker Container Service Offering")
        public  class dockerContainerServiceOffering {

            @Test
            @DisplayName("Service Offering exists")
            @Disabled
            public void serviceOfferingExists() throws Exception {
                var serviceOfferingId = serviceOfferingDockerContainer.getId();
                var serviceOfferingVersionId = serviceOfferingDockerContainer.getVersions().get(0).getId();
                var path = BASE_PATH + "/" + serviceOfferingId + "/versions/" + serviceOfferingVersionId + "/order";
                var serviceOfferingOder = new ServiceOrder();
                serviceOfferingOder.setServiceOptionValues(Arrays.asList(
                        new ServiceOptionValue("envVar1", "value1"),
                        new ServiceOptionValue("envVar2", "value2"),
                        new ServiceOptionValue("5555", "5555"),
                        new ServiceOptionValue("6666", "3333"),
                        new ServiceOptionValue("/sample/path/data", "data"),
                        new ServiceOptionValue("/sample/path/config", "config"),
                        new ServiceOptionValue("label1", "labelVal1"),
                        new ServiceOptionValue("label2", "labelVal2")
                ));

                wireMockServer.stubFor(
                        WireMock.post(WireMock.urlPathMatching("/api/v2/job_templates/(.*)/launch/"))
                                .withRequestBody(WireMock.equalToJson(
                                        ServiceOfferingsRestControllerITData.serviceOfferingExists_expectedAwxJobLaunchRequestBody,
                                        true, true))
                                .willReturn(aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(AwxMockedResponses.POST_JOB_RUN_RESPONSE_JSON)));

                mockMvc.perform(
                                post(path).with(csrf())
                                        .queryParam("resourceId", testResourceId)
                                        .content(asJsonString(serviceOfferingOder))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());
            }

            @Test
            @DisplayName("Service Offering exists, but service option value missing")
            @Disabled
            public void serviceOfferingExistsButServiceOptionValueMissing() throws Exception {
                var serviceOfferingId = serviceOfferingDockerContainer.getId();
                var serviceOfferingVersionId = serviceOfferingDockerContainer.getVersions().get(0).getId();
                var path = BASE_PATH + "/" + serviceOfferingId + "/versions/" + serviceOfferingVersionId + "/order";
                var serviceOfferingOder = new ServiceOrder();

                mockMvc.perform(
                                post(path).with(csrf())
                                        .queryParam("resourceId", testResourceId)
                                        .content(asJsonString(serviceOfferingOder))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andDo(MockMvcResultHandlers.log())
                        .andExpect(status().isBadRequest())
                        .andExpect(status().reason(containsString("Value for required service option")))
                        .andExpect(status().reason(containsString("is missing")));
            }
        }

        @Test
        @DisplayName("Service offering not existing")
        public void serviceOfferingNotExisting() throws Exception {
            var serviceOfferingId = UUID.randomUUID();
            var serviceOfferingVersionId = UUID.randomUUID();
            var resourceId = UUID.randomUUID();
            var path = BASE_PATH + "/" + serviceOfferingId + "/versions/" + serviceOfferingVersionId + "/order";
            var serviceOfferingOder = new ServiceOrder();
            var expectedErrorMessage = "Service Offering '" + serviceOfferingId + "' not found";

            mockMvc.perform(
                post(path).with(csrf())
                    .queryParam("resourceId", testResourceId)
                    .content(asJsonString(serviceOfferingOder))
                    .contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("Context loads")
    public void contextLoads() throws Exception {
        Assertions.assertThat(controller).isNotNull();
    }

    public void addTestServiceOfferings() throws IOException {
        /// Vendors
        var vendor1Id = UUID.fromString("dac52253-a580-479a-84fe-2ca0ae90fb2c");
        var vendor1 = new ServiceVendor(vendor1Id);
        vendor1.setName("SampleVendor1");
        vendor1 = serviceVendorJpaRepository.save(vendor1);

        /// Categories
        var testCategory = new ServiceCategory("TestCategory");
        testCategory = this.serviceCategoryJpaRepository.save(testCategory);

        /// Service Offerings
        this.addTestServiceOfferingDockerContainer(vendor1, testCategory);
    }

    public void addTestServiceOfferingDockerContainer(ServiceVendor vendor, ServiceCategory serviceCategory) throws IOException {
        this.serviceOfferingDockerContainer = new ServiceOffering(UUID.randomUUID());
        this.serviceOfferingDockerContainer.setName("Test Docker Service Offering");
        this.serviceOfferingDockerContainer.setDescription("Description of test Service Offering for Docker Container");
        this.serviceOfferingDockerContainer.setShortDescription("Short Description of test Service Offering for Docker Container");
        this.serviceOfferingDockerContainer.setServiceCategory(serviceCategory);
        this.serviceOfferingDockerContainer.setServiceVendor(vendor);

        var dockerContainerDeploymentDefinition = new DockerContainerDeploymentDefinition();
        dockerContainerDeploymentDefinition.setImageRepository("nginx");
        dockerContainerDeploymentDefinition.setImageTag("latest");
        dockerContainerDeploymentDefinition.setEnvironmentVariables(new ArrayList<>(Arrays.asList(
            new ServiceOfferingEnvironmentVariable("envVarButNotServiceOption1", "val3"),
            new ServiceOfferingEnvironmentVariable("envVarButNotServiceOption2", "val3")
        )));

        var serviceOfferingVersion = new ServiceOfferingVersion(
                UUID.randomUUID(), this.serviceOfferingDockerContainer, "0.0.1", dockerContainerDeploymentDefinition);

        /// Service Options
        var dockerContainerServiceOfferingServiceOptionCategories = new ArrayList<ServiceOptionCategory>();
        var serviceOptionsEnv = new ArrayList<ServiceOption>();
        serviceOptionsEnv.add(new ServiceOption(
                "", "envVar1", "Env 1", "Env Var 1", ServiceOptionType.ENVIRONMENT_VARIABLE,
                 "EnvVarVal1", ServiceOptionValueType.STRING,
                true, true));
        serviceOptionsEnv.add(new ServiceOption(
                "", "envVar2", "Env 2", "Env Var 2", ServiceOptionType.ENVIRONMENT_VARIABLE,
                "EnvVarVal2", ServiceOptionValueType.INTEGER,
                true, true));
        var serviceOptionsPorts = new ArrayList<ServiceOption>();
        serviceOptionsPorts.add(new ServiceOption(
                "" ,"5555", "Port XY", "Port XY published by container", ServiceOptionType.PORT_MAPPING,
                5555, ServiceOptionValueType.INTEGER,
                true, true));
        serviceOptionsPorts.add(new ServiceOption(
                "", "6666", "Port AB", "Port AB published by container", ServiceOptionType.PORT_MAPPING,
                6666, ServiceOptionValueType.INTEGER,
                true, true));
        var serviceOptionsVolumes = new ArrayList<ServiceOption>();
        serviceOptionsVolumes.add(new ServiceOption(
                "", "/sample/path/data", "Data Volume", "Volume of container used for data", ServiceOptionType.VOLUME,
                "/my/path/data", ServiceOptionValueType.STRING,
                true, true));
        serviceOptionsVolumes.add(new ServiceOption(
                "", "/sample/path/config", "Config Volume", "Volume of container used for config", ServiceOptionType.VOLUME,
                "/my/path/config", ServiceOptionValueType.STRING,
                true, true));
        var serviceOptionsLabels = new ArrayList<ServiceOption>();
        serviceOptionsLabels.add(new ServiceOption(
                "", "label1", "Label 1", "Label 1 of container", ServiceOptionType.LABEL,
                "LabelVal1", ServiceOptionValueType.STRING,
                true, true));
        serviceOptionsLabels.add(new ServiceOption(
                "", "label2", "Label 2", "Label 2 of container", ServiceOptionType.LABEL,
                "LabelVal2", ServiceOptionValueType.STRING,
                true, true));
        dockerContainerServiceOfferingServiceOptionCategories.add(new ServiceOptionCategory(0, "Environment", serviceOptionsEnv));
        dockerContainerServiceOfferingServiceOptionCategories.add(new ServiceOptionCategory(1, "Ports", serviceOptionsPorts));
        dockerContainerServiceOfferingServiceOptionCategories.add(new ServiceOptionCategory(2, "Volumes", serviceOptionsVolumes));
        dockerContainerServiceOfferingServiceOptionCategories.add(new ServiceOptionCategory(3, "Labels", serviceOptionsLabels));
        serviceOfferingVersion.setServiceOptionCategories(dockerContainerServiceOfferingServiceOptionCategories);

        var optionalServiceOffering = serviceOfferingJpaRepository.findById(this.serviceOfferingDockerContainer.getId());
        if (optionalServiceOffering.isPresent())
        {
            serviceOfferingJpaRepository.delete(optionalServiceOffering.get());
        }
        this.serviceOfferingDockerContainer.getVersions().add(serviceOfferingVersion);
        this.serviceOfferingDockerContainer = serviceOfferingJpaRepository.save(this.serviceOfferingDockerContainer);
    }

    public void addTestServiceOfferingDockerCompose(ServiceVendor vendor, ServiceCategory serviceCategory) throws IOException {
        this.serviceOfferingDockerCompose = new ServiceOffering(UUID.randomUUID());
        this.serviceOfferingDockerCompose.setName("Test Docker Compose Offering");
        this.serviceOfferingDockerCompose.setDescription("Description of test Service Offering for Docker Compose");
        this.serviceOfferingDockerCompose.setShortDescription("Short Description of test Service Offering for Docker Compose");
        this.serviceOfferingDockerCompose.setServiceCategory(serviceCategory);
        this.serviceOfferingDockerCompose.setServiceVendor(vendor);

        var dockerComposeDeploymentDefinition = new DockerComposeDeploymentDefinition();
        dockerComposeDeploymentDefinition.setComposeFile("");
        dockerComposeDeploymentDefinition.setDotEnvFile("");
        dockerComposeDeploymentDefinition.setEnvFiles(new HashMap<>());

        var serviceOfferingVersion = new ServiceOfferingVersion(
                UUID.randomUUID(), this.serviceOfferingDockerCompose, "0.0.1", dockerComposeDeploymentDefinition);

        /// Service Options
        var dockerComposeServiceOfferingServiceOptionCategories = new ArrayList<ServiceOptionCategory>();
        var serviceOptionsEnv = new ArrayList<ServiceOption>();
        serviceOptionsEnv.add(new ServiceOption(
                "", "envVar1", "Env 1", "Env Var 1", ServiceOptionType.ENVIRONMENT_VARIABLE,
                "EnvVarVal1", ServiceOptionValueType.STRING,
                true, true));
        serviceOptionsEnv.add(new ServiceOption(
                "", "envVar2", "Env 2", "Env Var 2", ServiceOptionType.ENVIRONMENT_VARIABLE,
                "EnvVarVal2", ServiceOptionValueType.INTEGER,
                true, true));
        var serviceOptionsPorts = new ArrayList<ServiceOption>();
        serviceOptionsPorts.add(new ServiceOption(
                "", "5555", "Port XY", "Port XY published by container", ServiceOptionType.PORT_MAPPING,
                5555, ServiceOptionValueType.INTEGER,
                true, true));
        serviceOptionsPorts.add(new ServiceOption(
                "", "6666", "Port AB", "Port AB published by container", ServiceOptionType.PORT_MAPPING,
                6666, ServiceOptionValueType.INTEGER,
                true, true));
        var serviceOptionsVolumes = new ArrayList<ServiceOption>();
        serviceOptionsVolumes.add(new ServiceOption(
                "", "/sample/path/data", "Data Volume", "Volume of container used for data", ServiceOptionType.VOLUME,
                "/my/path/data", ServiceOptionValueType.STRING,
                true, true));
        serviceOptionsVolumes.add(new ServiceOption(
                "", "/sample/path/config", "Config Volume", "Volume of container used for config", ServiceOptionType.VOLUME,
                "/my/path/config", ServiceOptionValueType.STRING,
                true, true));
        var serviceOptionsLabels = new ArrayList<ServiceOption>();
        serviceOptionsLabels.add(new ServiceOption(
                "", "label1", "Label 1", "Label 1 of container", ServiceOptionType.LABEL,
                "LabelVal1", ServiceOptionValueType.STRING,
                true, true));
        serviceOptionsLabels.add(new ServiceOption(
                "", "label2", "Label 2", "Label 2 of container", ServiceOptionType.LABEL,
                "LabelVal2", ServiceOptionValueType.STRING,
                true, true));
        dockerComposeServiceOfferingServiceOptionCategories.add(new ServiceOptionCategory(0, "Environment", serviceOptionsEnv));
        dockerComposeServiceOfferingServiceOptionCategories.add(new ServiceOptionCategory(1, "Ports", serviceOptionsPorts));
        dockerComposeServiceOfferingServiceOptionCategories.add(new ServiceOptionCategory(2, "Volumes", serviceOptionsVolumes));
        dockerComposeServiceOfferingServiceOptionCategories.add(new ServiceOptionCategory(3, "Labels", serviceOptionsLabels));
        serviceOfferingVersion.setServiceOptionCategories(dockerComposeServiceOfferingServiceOptionCategories);

        var optionalDockerComposeServiceOffering = serviceOfferingJpaRepository.findById(serviceOfferingDockerCompose.getId());
        if (optionalDockerComposeServiceOffering.isPresent())
        {
            serviceOfferingJpaRepository.delete(optionalDockerComposeServiceOffering.get());
        }
        this.serviceOfferingDockerCompose.getVersions().add(serviceOfferingVersion);
        this.serviceOfferingDockerCompose = serviceOfferingJpaRepository.save(serviceOfferingDockerCompose);
    }
}
