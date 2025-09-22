package org.eclipse.slm.service_management.service.rest.service_deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.eclipse.slm.common.awx.client.observer.JobFinalState;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.resource_management.service.client.ResourceManagementApiClientInitializer;
import org.eclipse.slm.resource_management.service.client.handler.ApiClient;
import org.eclipse.slm.resource_management.service.client.handler.ApiException;
import org.eclipse.slm.service_management.model.exceptions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.ServiceOffering;
import org.eclipse.slm.service_management.model.offerings.ServiceOrder;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.model.offerings.docker.container.DockerContainerDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.exceptions.InvalidServiceOfferingDefinitionException;
import org.eclipse.slm.service_management.persistence.api.ServiceOrderJpaRepository;
import org.eclipse.slm.service_management.service.rest.service_instances.ServiceInstanceEventMessageSender;
import org.eclipse.slm.service_management.service.rest.service_instances.ServiceInstancesConsulClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.AccessToken;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLException;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Testcontainers
@Disabled
public class ServiceDeploymentHandlerTest {

    public final static Logger LOG = LoggerFactory.getLogger(ServiceDeploymentHandlerTest.class);

    private ServiceDeploymentHandler serviceDeploymentHandler;

    private WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(0));

    private AwxJobObserverInitializer awxJobObserverInitializer;

    @Mock
    private AwxJobExecutor awxJobExecutor;

    @Mock
    private ServiceInstanceEventMessageSender serviceInstanceEventMessageSender;

    @Mock
    private ConsulServicesApiClient consulServicesApiClient;

    @Mock
    private KeycloakAdminClient keycloakAdminClient;

    @Mock
    private ResourceManagementApiClientInitializer resourceManagementApiClientInitializer;

    @Mock
    private ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Mock
    private ServiceInstancesConsulClient serviceInstancesConsulClient;

    private ApiClient apiClient;

    private JwtAuthenticationToken jwtAuthenticationToken;

    @BeforeEach
    public void initEach() throws SSLException {
        wireMockServer.start();

        var accessToken = new AccessToken();
        accessToken.setSubject(UUID.randomUUID().toString());
        var jwt = new Jwt(accessToken.toString(), null, null, new HashMap<>(), new HashMap<>());
        this.jwtAuthenticationToken = new JwtAuthenticationToken(jwt, new ArrayList<>(), "testUser");

        awxJobObserverInitializer = new AwxJobObserverInitializer("http", "localhost", String.valueOf(this.wireMockServer.port()), "", "", 4);

        this.apiClient = new ApiClient();
        this.apiClient.setBasePath("http://localhost:" + this.wireMockServer.port());
        when(this.resourceManagementApiClientInitializer.init(any(JwtAuthenticationToken.class))).thenReturn(this.apiClient);

        // Mock Resource Management REST API
        this.wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathMatching("/resources/.*/deployment-capabilities"))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", "application/json")
                                .withBody("""
                                        [
                                            {
                                                "uuid": "08c5b8de-5d4a-4116-a73f-1d1f616c7c70",
                                                "capabilityClass": "DeploymentCapability",
                                                "name": "Docker",
                                                "logo": "mdi-docker",
                                                "type": [
                                                    "SETUP",
                                                    "DEPLOY"
                                                ],
                                                "actions": {
                                                    "DEPLOY": {
                                                        "capabilityActionClass": "AwxCapabilityAction",
                                                        "capabilityActionType": "DEPLOY",
                                                        "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-dummy.git",
                                                        "awxBranch": "main",
                                                        "playbook": "deploy.yml"
                                                    },
                                                    "UNINSTALL": {
                                                        "capabilityActionClass": "AwxCapabilityAction",
                                                        "capabilityActionType": "UNINSTALL",
                                                        "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-dummy.git",
                                                        "awxBranch": "main",
                                                        "playbook": "uninstall.yml"
                                                    },
                                                    "INSTALL": {
                                                        "capabilityActionClass": "AwxCapabilityAction",
                                                        "capabilityActionType": "INSTALL",
                                                        "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-dummy.git",
                                                        "awxBranch": "main",
                                                        "playbook": "install.yml"
                                                    },
                                                    "UNDEPLOY": {
                                                        "capabilityActionClass": "AwxCapabilityAction",
                                                        "capabilityActionType": "UNDEPLOY",
                                                        "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-dummy.git",
                                                        "awxBranch": "main",
                                                        "playbook": "undeploy.yml"
                                                    }
                                                },
                                                "supportedDeploymentTypes": [
                                                    "DOCKER_CONTAINER",
                                                    "DOCKER_COMPOSE"
                                                ],
                                                "cluster": false
                                            }
                                        ]
                                        """)));

        this.serviceDeploymentHandler = new ServiceDeploymentHandler(
                awxJobObserverInitializer,
                awxJobExecutor,
                consulServicesApiClient,
                keycloakAdminClient,
                resourceManagementApiClientInitializer,
                serviceOrderJpaRepository,
                serviceInstancesConsulClient,
                serviceInstanceEventMessageSender);
    }

    @Test
    @DisplayName("Deploy Docker Container service offering")
    @Disabled
    public void deployDockerContainerServiceOffering()
            throws SSLException, JsonProcessingException, ServiceOptionNotFoundException, ApiException, ConsulLoginFailedException, InvalidServiceOfferingDefinitionException, CapabilityServiceNotFoundException {
        var resourceId = UUID.randomUUID();

        var deploymentDefinition = new DockerContainerDeploymentDefinition();
        var serviceOffering = new ServiceOffering(UUID.randomUUID());
        var serviceOfferingVersion = new ServiceOfferingVersion(
                UUID.randomUUID(), serviceOffering, "1.0.0", deploymentDefinition);
        var serviceOfferingOrder = new ServiceOrder();

        var deploymentJobRun = this.serviceDeploymentHandler
                .deployServiceOfferingToResource(jwtAuthenticationToken, resourceId, serviceOfferingVersion, serviceOfferingOrder);
        var serviceId = deploymentJobRun.getServiceInstance().getId();

        this.serviceDeploymentHandler.onJobStateFinished(deploymentJobRun.getAwxJobObserver(), JobFinalState.SUCCESSFUL);
        verify(this.keycloakAdminClient).createRealmRoleAndAssignToUser(
                argThat(jwtAuthenticationToken -> jwtAuthenticationToken.equals(this.jwtAuthenticationToken)),
                argThat(roleName -> roleName.startsWith("service_")));

        ArgumentCaptor<ConsulCredential> consulCredentialArg = ArgumentCaptor.forClass(ConsulCredential.class);
        ArgumentCaptor<UUID> nodeIdArg = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<UUID> serviceIdArg = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> serviceNameArg = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<String>> serviceTagsArg = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, String>> serviceMetaDataArg = ArgumentCaptor.forClass(Map.class);
        verify(this.consulServicesApiClient).registerServiceForNodeWithReadAccessViaKeycloakRole(
                consulCredentialArg.capture(),
                nodeIdArg.capture(),
                serviceNameArg.capture(),
                serviceIdArg.capture(),
                Optional.empty(),
                serviceTagsArg.capture(),
                serviceMetaDataArg.capture());
        assertThat(nodeIdArg.getValue()).isEqualTo(resourceId.toString());
        assertThat(serviceIdArg.getValue()).isEqualTo(serviceId.toString());
        assertThat(serviceNameArg.getValue()).isEqualTo("service_" + serviceId);
    }

}
