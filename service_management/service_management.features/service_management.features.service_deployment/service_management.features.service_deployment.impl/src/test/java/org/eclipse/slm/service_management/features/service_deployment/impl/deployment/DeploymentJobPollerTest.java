package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.slm.common.aas.submodels.deployment.DeploymentJobState;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentStatus;
import org.eclipse.slm.common.model.DeploymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeploymentJobPollerTest {

    private static final DeploymentTarget TARGET = new DeploymentTarget(
            "Deployment-1", "https://rm.invalid/aas/submodels/Deployment-1", "Resource_abc",
            "edge-01", "Docker", "24.0.7", List.of(DeploymentType.DOCKER_COMPOSE), Map.of());

    @Mock private AasDeploymentClient deploymentClient;

    @Test
    @DisplayName("Polling stops as soon as the job reaches a terminal state")
    void pollingStopsAtTerminalState() {
        when(deploymentClient.getStatus(eq(TARGET), eq("job-1"), any()))
                .thenReturn(DeploymentStatus.of(DeploymentJobState.RUNNING))
                .thenReturn(DeploymentStatus.of(DeploymentJobState.SUCCEEDED));

        var poller = new DeploymentJobPoller(deploymentClient, Duration.ofMillis(1), Duration.ofSeconds(5));
        var outcome = new AtomicReference<DeploymentStatus>();

        poller.awaitTerminalState(TARGET, "job-1", "token", outcome::set);

        assertThat(outcome.get().state()).isEqualTo(DeploymentJobState.SUCCEEDED);
    }

    @Test
    @DisplayName("A timeout ends the wait as FAILED and says the job may still be running")
    void timeoutEndsWaitAsFailed() {
        when(deploymentClient.getStatus(eq(TARGET), eq("job-2"), any()))
                .thenReturn(DeploymentStatus.of(DeploymentJobState.RUNNING));

        var poller = new DeploymentJobPoller(deploymentClient, Duration.ofMillis(1), Duration.ofMillis(10));
        var outcome = new AtomicReference<DeploymentStatus>();

        poller.awaitTerminalState(TARGET, "job-2", "token", outcome::set);

        assertThat(outcome.get().state()).isEqualTo(DeploymentJobState.FAILED);
        assertThat(outcome.get().message()).contains("may still be running");
    }

    @Test
    @DisplayName("An unreachable target ends the wait as FAILED instead of retrying forever")
    void unreachableTargetEndsWaitAsFailed() {
        when(deploymentClient.getStatus(eq(TARGET), eq("job-3"), any()))
                .thenThrow(new DeploymentInvocationException("Deployment-1", "GetDeploymentStatus",
                        new IllegalStateException("connection refused")));

        var poller = new DeploymentJobPoller(deploymentClient, Duration.ofMillis(1), Duration.ofSeconds(5));
        var outcome = new AtomicReference<DeploymentStatus>();

        poller.awaitTerminalState(TARGET, "job-3", "token", outcome::set);

        assertThat(outcome.get().state()).isEqualTo(DeploymentJobState.FAILED);
        assertThat(outcome.get().message()).contains("connection refused");
    }
}
