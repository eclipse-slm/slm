package org.eclipse.slm.resource_management.features.capabilities.aas;

import org.eclipse.slm.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.awx.client.observer.JobGoal;
import org.eclipse.slm.awx.client.observer.JobTarget;
import org.eclipse.slm.common.aas.submodels.deployment.DeployRequest;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentJobState;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.Action;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeploymentJobManagerTest {

    private static final UUID RESOURCE_ID = UUID.randomUUID();
    private static final UUID CAPABILITY_SERVICE_ID = UUID.randomUUID();

    @Mock private CapabilitiesConsulClient capabilitiesConsulClient;
    @Mock private AwxJobExecutor awxJobExecutor;
    @Mock private AwxJobObserverInitializer awxJobObserverInitializer;

    private DeploymentJobStore jobStore;
    private DeploymentJobManager jobManager;

    /**
     * AwxCredential (external awx.client type) has no plain-String-token constructor,
     * only String username/password or JwtAuthenticationToken. DeploymentJobManager.deploy
     * therefore takes a JwtAuthenticationToken instead of a raw token string; this builds a
     * minimal but valid one for tests.
     */
    private JwtAuthenticationToken testJwt(String tokenValue) {
        var jwt = new Jwt(tokenValue, null, null,
                Map.of("alg", "none"), Map.of("sub", "test-user"));
        return new JwtAuthenticationToken(jwt, List.of(), "testUser");
    }

    private CapabilityService dockerCapabilityService() {
        var capability = new DeploymentCapability();
        capability.setName("Docker");
        capability.setSupportedDeploymentTypes(List.of(DeploymentType.DOCKER_COMPOSE));

        var awxAction = new AwxAction();
        awxAction.setAwxRepo("https://example.invalid/playbooks.git");
        awxAction.setAwxBranch("main");
        awxAction.setPlaybook("deploy.yml");
        capability.setActions(Map.<ActionType, Action>of(ActionType.DEPLOY, awxAction));

        return CapabilityService.builder(RESOURCE_ID, CAPABILITY_SERVICE_ID, capability)
                .status(CapabilityServiceStatus.READY)
                .customMeta(Map.of("version", "24.0.7"))
                .build();
    }

    private DeployRequest composeRequest() {
        return new DeployRequest(UUID.randomUUID(), DeploymentType.DOCKER_COMPOSE,
                "services:\n  web:\n    image: nginx\n".getBytes(StandardCharsets.UTF_8),
                "application/yaml", List.of());
    }

    @BeforeEach
    void setUp() {
        jobStore = new DeploymentJobStore();
        jobManager = new DeploymentJobManager(capabilitiesConsulClient, awxJobExecutor,
                awxJobObserverInitializer, new DeploymentExtraVarsBuilder(), jobStore);
    }

    @Test
    @DisplayName("An unsupported deployment type is rejected before any job starts")
    void unsupportedDeploymentTypeIsRejected() {
        when(capabilitiesConsulClient.getCapabilityServices())
                .thenReturn(List.of(dockerCapabilityService()));

        var request = new DeployRequest(UUID.randomUUID(), DeploymentType.KUBERNETES,
                "kind: Deployment\n".getBytes(StandardCharsets.UTF_8), "application/yaml", List.of());

        var result = jobManager.deploy(CAPABILITY_SERVICE_ID, request, testJwt("token"));

        assertThat(result.accepted()).isFalse();
        assertThat(result.message()).contains("KUBERNETES");
    }

    @Test
    @DisplayName("An unknown capability service is rejected with a clear message")
    void unknownCapabilityServiceIsRejected() {
        when(capabilitiesConsulClient.getCapabilityServices()).thenReturn(List.of());

        var result = jobManager.deploy(CAPABILITY_SERVICE_ID, composeRequest(), testJwt("token"));

        assertThat(result.accepted()).isFalse();
        assertThat(result.message()).contains(CAPABILITY_SERVICE_ID.toString());
    }

    @Test
    @DisplayName("An accepted deployment returns a job id and registers as running")
    void acceptedDeploymentReturnsRunningJob() throws Exception {
        when(capabilitiesConsulClient.getCapabilityServices())
                .thenReturn(List.of(dockerCapabilityService()));
        when(awxJobExecutor.executeJob(any(), any(), any(), any(), any())).thenReturn(4711);
        when(awxJobObserverInitializer.initNewObserver(anyInt(), any(), any(), any()))
                .thenReturn(new AwxJobObserver(4711, JobTarget.SERVICE, JobGoal.CREATE));

        var result = jobManager.deploy(CAPABILITY_SERVICE_ID, composeRequest(), testJwt("token"));

        assertThat(result.accepted()).isTrue();
        assertThat(result.jobId()).isNotBlank();
        assertThat(jobStore.get(result.jobId()).state()).isEqualTo(DeploymentJobState.RUNNING);
    }

    @Test
    @DisplayName("An unknown job id reports UNKNOWN rather than throwing")
    void unknownJobIdReportsUnknown() {
        assertThat(jobManager.getStatus("no-such-job").state()).isEqualTo(DeploymentJobState.UNKNOWN);
    }
}
