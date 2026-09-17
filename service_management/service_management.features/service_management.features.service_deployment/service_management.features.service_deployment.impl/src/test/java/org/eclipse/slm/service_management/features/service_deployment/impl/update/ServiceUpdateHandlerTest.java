package org.eclipse.slm.service_management.features.service_deployment.impl.update;

import org.eclipse.slm.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.awx.client.observer.JobGoal;
import org.eclipse.slm.awx.client.observer.JobTarget;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.Action;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.eclipse.slm.resource_management.features.capabilities.providers.ServiceHoster;
import org.eclipse.slm.resource_management.service.client.ProvidersApiClient;
import org.eclipse.slm.resource_management.service.client.ResourceManagementClient;
import org.eclipse.slm.resource_management.service.client.ResourceManagementClientFactory;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrder;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrderResult;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.eclipse.slm.service_management.features.service_deployment.impl.deployment.ServiceOrderJpaRepository;
import org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances.ServiceInstanceEventMessageSender;
import org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances.ServiceInstancesConsulClient;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.ServiceOffering;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.docker.compose.DockerComposeDeploymentDefinition;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions.ServiceOfferingVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Focused regression test for the carry-forward fix in commit b20a08d9: an update's
 * ServiceOrder must keep the deployment target submodel id of the service instance's
 * latest successful order, otherwise a subsequent deployment operation cannot find the
 * target again. No prior test infrastructure existed for ServiceUpdateHandler, so this
 * builds the minimal mocking chain needed to exercise updateServiceInstance() end to end
 * (mirroring the mocking patterns already used for AwxJobExecutor/AwxJobObserverInitializer
 * in DeploymentJobManagerTest), rather than testing the assignment in isolation.
 */
@ExtendWith(MockitoExtension.class)
class ServiceUpdateHandlerTest {

    @Mock private AwxJobObserverInitializer awxJobObserverInitializer;
    @Mock private AwxJobExecutor awxJobExecutor;
    @Mock private KeycloakAdminClient keycloakAdminClient;
    @Mock private ResourceManagementClientFactory resourceManagementClientFactory;
    @Mock private ServiceOrderJpaRepository serviceOrderJpaRepository;
    @Mock private ServiceInstancesConsulClient serviceInstancesConsulClient;
    @Mock private ServiceInstanceEventMessageSender serviceInstanceEventMessageSender;

    private ServiceUpdateHandler handler;

    @BeforeEach
    void setUp() {
        this.handler = new ServiceUpdateHandler(awxJobObserverInitializer, awxJobExecutor, keycloakAdminClient,
                resourceManagementClientFactory, serviceOrderJpaRepository, serviceInstancesConsulClient,
                serviceInstanceEventMessageSender);
    }

    private JwtAuthenticationToken testJwt(String tokenValue) {
        var jwt = new Jwt(tokenValue, null, null,
                Map.of("alg", "none"), Map.of("sub", "test-user"));
        return new JwtAuthenticationToken(jwt, List.of(), "testUser");
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

    private CapabilityService dockerCapabilityService(UUID resourceId, UUID capabilityServiceId) {
        var capability = new DeploymentCapability();
        capability.setName("Docker");
        capability.setSupportedDeploymentTypes(List.of(DeploymentType.DOCKER_COMPOSE));

        var awxAction = new AwxAction();
        awxAction.setAwxRepo("https://example.invalid/playbooks.git");
        awxAction.setAwxBranch("main");
        awxAction.setPlaybook("update.yml");
        capability.setActions(Map.<ActionType, Action>of(ActionType.UPDATE, awxAction));

        return CapabilityService.builder(resourceId, capabilityServiceId, capability)
                .status(CapabilityServiceStatus.READY)
                .customMeta(Map.of("version", "24.0.7"))
                .build();
    }

    @Test
    @DisplayName("Updating a service instance carries the deployment target submodel id forward onto the new order")
    void updateCarriesDeploymentTargetSubmodelIdForward() throws Exception {
        var serviceInstanceId = UUID.randomUUID();
        var resourceId = UUID.randomUUID();
        var capabilityServiceId = UUID.randomUUID();

        var latestServiceOrder = new ServiceOrder();
        latestServiceOrder.setServiceInstanceId(serviceInstanceId);
        latestServiceOrder.setServiceOptionValues(new ArrayList<>());
        latestServiceOrder.setDeploymentTargetSubmodelId("Deployment-original-target");
        latestServiceOrder.setServiceOrderResult(ServiceOrderResult.SUCCESSFULL);

        when(serviceOrderJpaRepository.findByServiceInstanceId(serviceInstanceId))
                .thenReturn(List.of(latestServiceOrder));

        var resourceManagementClient = mock(ResourceManagementClient.class);
        var providersApiClient = mock(ProvidersApiClient.class);
        when(resourceManagementClientFactory.createWithBearerTokenAuth(any())).thenReturn(resourceManagementClient);
        when(resourceManagementClient.providers()).thenReturn(providersApiClient);
        var serviceHoster = new ServiceHoster(dockerCapabilityService(resourceId, capabilityServiceId));
        when(providersApiClient.getServiceHosters(any())).thenReturn(List.of(serviceHoster));

        when(awxJobExecutor.executeJob(any(), any(), any(), any(), any())).thenReturn(4711);
        when(awxJobObserverInitializer.initNewObserver(anyInt(), any(), any(), any()))
                .thenReturn(new AwxJobObserver(4711, JobTarget.SERVICE, JobGoal.UPDATE));

        var serviceInstance = new ServiceInstance(serviceInstanceId, new ArrayList<>(), Map.of(),
                resourceId, capabilityServiceId, UUID.randomUUID(), UUID.randomUUID(),
                new ArrayList<>(), new ArrayList<>());

        var updateJobRun = handler.updateServiceInstance(
                testJwt("token"), serviceInstance, dockerComposeOfferingVersion());

        assertThat(updateJobRun.getServiceOrder().getDeploymentTargetSubmodelId())
                .isEqualTo("Deployment-original-target");
    }
}
