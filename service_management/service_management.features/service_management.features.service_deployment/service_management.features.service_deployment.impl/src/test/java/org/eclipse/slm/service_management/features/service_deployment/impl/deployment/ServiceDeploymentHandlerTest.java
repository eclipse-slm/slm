package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.slm.common.aas.submodels.deployment.DeployRequest;
import org.eclipse.slm.common.aas.submodels.deployment.DeployResult;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentJobState;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentStatus;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrder;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrderResult;
import org.eclipse.slm.service_management.features.service_deployment.api.events.ServiceInstanceEventType;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances.ServiceInstanceEventMessageSender;
import org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances.ServiceInstancesConsulClient;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.ServiceOffering;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.docker.compose.DockerComposeDeploymentDefinition;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions.ServiceOfferingVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceDeploymentHandlerTest {

    @Mock private DeploymentDescriptorRenderer descriptorRenderer;
    @Mock private DeploymentTargetHandler deploymentTargetHandler;
    @Mock private AasDeploymentClient deploymentClient;
    @Mock private DeploymentJobPoller deploymentJobPoller;
    @Mock private KeycloakAdminClient keycloakAdminClient;
    @Mock private ServiceInstancesConsulClient serviceInstancesConsulClient;
    @Mock private ServiceInstanceEventMessageSender serviceInstanceEventMessageSender;
    @Mock private ServiceOrderJpaRepository serviceOrderJpaRepository;

    private ServiceDeploymentHandler handler;

    @BeforeEach
    void setUp() {
        this.handler = new ServiceDeploymentHandler(descriptorRenderer, deploymentTargetHandler, deploymentClient,
                deploymentJobPoller, keycloakAdminClient, serviceInstancesConsulClient,
                serviceInstanceEventMessageSender, serviceOrderJpaRepository);
    }

    /**
     * JwtAuthenticationToken has no builder and its constructor rejects a plain string,
     * only String username/password or JwtAuthenticationToken. It needs a minimal but
     * valid Jwt to construct, following the same pattern used elsewhere in this codebase
     * (e.g. DeploymentJobManagerTest, DeploymentSubmodelServiceTest).
     */
    private JwtAuthenticationToken testJwt(String tokenValue) {
        var jwt = new Jwt(tokenValue, null, null,
                Map.of("alg", "none"), Map.of("sub", "test-user"));
        return new JwtAuthenticationToken(jwt, List.of(), "testUser");
    }

    private DeploymentTarget dockerTarget(String aasId) {
        return new DeploymentTarget("Deployment-1", "https://rm/aas/submodels/Deployment-1",
                aasId, "edge-01", "Docker", "24.0.7",
                List.of(DeploymentType.DOCKER_COMPOSE), Map.of());
    }

    private ServiceOfferingVersion dockerComposeOfferingVersion() {
        var serviceOffering = new ServiceOffering();
        serviceOffering.setId(UUID.randomUUID());
        serviceOffering.setName("Demo Service");

        var deploymentDefinition = new DockerComposeDeploymentDefinition();
        deploymentDefinition.setComposeFile("services:\n  web:\n    image: nginx:1.25\n");

        var version = new ServiceOfferingVersion();
        version.setId(UUID.randomUUID());
        version.setVersion("1.0.0");
        version.setServiceOffering(serviceOffering);
        version.setDeploymentDefinition(deploymentDefinition);
        version.setServicePorts(new ArrayList<>());
        version.setServiceRepositories(new ArrayList<>());
        return version;
    }

    private ServiceOrder serviceOrder() {
        var order = new ServiceOrder();
        order.setDeploymentTargetSubmodelId("Deployment-1");
        order.setServiceOptionValues(new ArrayList<>());
        return order;
    }

    private RenderedDescriptor renderedDescriptor() {
        return new RenderedDescriptor("content".getBytes(), "application/yaml",
                new java.util.HashMap<>(), new ArrayList<>());
    }

    @Test
    @DisplayName("Happy path: renders, deploys, persists the job id and starts polling")
    void happyPathDeploysAndPersistsJobId() throws Exception {
        var target = dockerTarget("Resource_" + UUID.randomUUID());
        var offeringVersion = dockerComposeOfferingVersion();
        var order = serviceOrder();
        var rendered = renderedDescriptor();

        when(deploymentTargetHandler.getDeploymentTargetOrThrow("Deployment-1")).thenReturn(target);
        when(descriptorRenderer.render(offeringVersion, order)).thenReturn(rendered);
        when(deploymentClient.deploy(eq(target), any(DeployRequest.class), eq("token")))
                .thenReturn(DeployResult.accepted("job-1"));

        var result = handler.deployServiceOfferingToTarget(testJwt("token"), offeringVersion, order);

        assertThat(result.getDeploymentJobId()).isEqualTo("job-1");
        verify(serviceOrderJpaRepository, times(1)).save(order);
        verify(deploymentJobPoller).awaitTerminalState(eq(target), eq("job-1"), eq("token"), any());
    }

    @Test
    @DisplayName("An unsupported deployment type is refused before the deployment is ever attempted")
    void unsupportedDeploymentTypeIsRefused() throws Exception {
        var target = new DeploymentTarget("Deployment-1", "https://rm/aas/submodels/Deployment-1",
                "Resource_" + UUID.randomUUID(), "edge-01", "Kubernetes", "1.29",
                List.of(DeploymentType.KUBERNETES), Map.of());
        var offeringVersion = dockerComposeOfferingVersion();
        var order = serviceOrder();

        when(deploymentTargetHandler.getDeploymentTargetOrThrow("Deployment-1")).thenReturn(target);

        assertThatThrownBy(() -> handler.deployServiceOfferingToTarget(testJwt("token"), offeringVersion, order))
                .isInstanceOf(IllegalArgumentException.class);

        verify(deploymentClient, never()).deploy(any(), any(), any());
    }

    @Test
    @DisplayName("A rejected deployment marks the order as failed and surfaces an exception")
    void rejectedDeploymentMarksOrderFailed() throws Exception {
        var target = dockerTarget("Resource_" + UUID.randomUUID());
        var offeringVersion = dockerComposeOfferingVersion();
        var order = serviceOrder();
        var rendered = renderedDescriptor();

        when(deploymentTargetHandler.getDeploymentTargetOrThrow("Deployment-1")).thenReturn(target);
        when(descriptorRenderer.render(offeringVersion, order)).thenReturn(rendered);
        when(deploymentClient.deploy(eq(target), any(DeployRequest.class), eq("token")))
                .thenReturn(DeployResult.rejected("no capacity"));

        assertThatThrownBy(() -> handler.deployServiceOfferingToTarget(testJwt("token"), offeringVersion, order))
                .isInstanceOf(IllegalStateException.class);

        assertThat(order.getServiceOrderResult()).isEqualTo(ServiceOrderResult.FAILED);
        verify(serviceOrderJpaRepository).save(order);
    }

    @SuppressWarnings("unchecked")
    private Consumer<DeploymentStatus> deployAndCaptureCallback(DeploymentTarget target,
                                                                ServiceOfferingVersion offeringVersion,
                                                                ServiceOrder order) throws Exception {
        var rendered = renderedDescriptor();

        when(deploymentTargetHandler.getDeploymentTargetOrThrow("Deployment-1")).thenReturn(target);
        when(descriptorRenderer.render(offeringVersion, order)).thenReturn(rendered);
        when(deploymentClient.deploy(eq(target), any(DeployRequest.class), eq("token")))
                .thenReturn(DeployResult.accepted("job-1"));

        handler.deployServiceOfferingToTarget(testJwt("token"), offeringVersion, order);

        ArgumentCaptor<Consumer<DeploymentStatus>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(deploymentJobPoller).awaitTerminalState(eq(target), eq("job-1"), eq("token"), captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("A successful deployment to an SLM-managed target assigns a role, registers Consul and sends CREATED")
    void successfulDeploymentToSlmManagedTargetRegistersEverything() throws Exception {
        var target = dockerTarget("Resource_" + UUID.randomUUID());
        var offeringVersion = dockerComposeOfferingVersion();
        var order = serviceOrder();

        var callback = deployAndCaptureCallback(target, offeringVersion, order);
        callback.accept(new DeploymentStatus(DeploymentJobState.SUCCEEDED, ""));

        verify(keycloakAdminClient).createRealmRoleAndAssignToUser(eq("test-user"), anyString());
        verify(serviceInstancesConsulClient).registerConsulServiceForServiceInstance(any(ServiceInstance.class), anyString());
        assertThat(order.getServiceOrderResult()).isEqualTo(ServiceOrderResult.SUCCESSFULL);
        verify(serviceInstanceEventMessageSender).sendMessage(any(ServiceInstance.class), eq(ServiceInstanceEventType.CREATED));
    }

    @Test
    @DisplayName("A successful deployment to a foreign target skips Consul registration but still fires the rest")
    void successfulDeploymentToForeignTargetSkipsConsul() throws Exception {
        var target = dockerTarget("MyCompany-Machine-42");
        var offeringVersion = dockerComposeOfferingVersion();
        var order = serviceOrder();

        var callback = deployAndCaptureCallback(target, offeringVersion, order);
        callback.accept(new DeploymentStatus(DeploymentJobState.SUCCEEDED, ""));

        verify(serviceInstancesConsulClient, never()).registerConsulServiceForServiceInstance(any(), anyString());
        verify(keycloakAdminClient).createRealmRoleAndAssignToUser(eq("test-user"), anyString());
        assertThat(order.getServiceOrderResult()).isEqualTo(ServiceOrderResult.SUCCESSFULL);
        verify(serviceInstanceEventMessageSender).sendMessage(any(ServiceInstance.class), eq(ServiceInstanceEventType.CREATED));
    }

    @Test
    @DisplayName("A terminal failure marks the order as failed without assigning roles, Consul or CREATED events")
    void terminalFailureMarksOrderFailedWithoutSideEffects() throws Exception {
        var target = dockerTarget("Resource_" + UUID.randomUUID());
        var offeringVersion = dockerComposeOfferingVersion();
        var order = serviceOrder();

        var callback = deployAndCaptureCallback(target, offeringVersion, order);
        callback.accept(new DeploymentStatus(DeploymentJobState.FAILED, "some reason"));

        assertThat(order.getServiceOrderResult()).isEqualTo(ServiceOrderResult.FAILED);
        verifyNoInteractions(keycloakAdminClient);
        verifyNoInteractions(serviceInstancesConsulClient);
        verifyNoInteractions(serviceInstanceEventMessageSender);
    }
}
