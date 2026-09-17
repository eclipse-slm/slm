package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.slm.common.aas.submodels.deployment.*;
import org.eclipse.slm.common.model.DeploymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AasDeploymentClientTest {

    private static final DeploymentTarget TARGET = new DeploymentTarget(
            "Deployment-1", "https://rm.invalid/aas/submodels/Deployment-1", "Resource_abc",
            "edge-01", "Docker", "24.0.7", List.of(DeploymentType.DOCKER_COMPOSE), java.util.Map.of());

    private DeployRequest request() {
        return new DeployRequest(UUID.randomUUID(), DeploymentType.DOCKER_COMPOSE,
                "services: {}\n".getBytes(StandardCharsets.UTF_8), "application/yaml", List.of());
    }

    @Test
    @DisplayName("Deploy passes the mapped request and returns the mapped result")
    void deployPassesMappedRequestAndReturnsResult() {
        var invoker = new RecordingInvoker(DeploymentOperationMapper.fromDeployResult(
                DeployResult.accepted("job-9")));
        var client = new AasDeploymentClient(invoker);

        var result = client.deploy(TARGET, request(), "token");

        assertThat(result.jobId()).isEqualTo("job-9");
        assertThat(invoker.lastOperation).isEqualTo(DeploymentSubmodelTemplate.OP_DEPLOY);
        assertThat(invoker.lastEndpoint).isEqualTo(TARGET.submodelEndpoint());
    }

    @Test
    @DisplayName("Status polling passes the job id and returns the mapped status")
    void statusPollingReturnsMappedStatus() {
        var invoker = new RecordingInvoker(DeploymentOperationMapper.fromDeploymentStatus(
                new DeploymentStatus(DeploymentJobState.RUNNING, "")));
        var client = new AasDeploymentClient(invoker);

        var status = client.getStatus(TARGET, "job-9", "token");

        assertThat(status.state()).isEqualTo(DeploymentJobState.RUNNING);
        assertThat(invoker.lastOperation).isEqualTo(DeploymentSubmodelTemplate.OP_GET_DEPLOYMENT_STATUS);
    }

    @Test
    @DisplayName("A failing invocation is wrapped with the target and operation that failed")
    void failingInvocationIsWrapped() {
        var client = new AasDeploymentClient((endpoint, submodelId, operation, input, token) -> {
            throw new IllegalStateException("connection refused");
        });

        assertThatThrownBy(() -> client.deploy(TARGET, request(), "token"))
                .isInstanceOf(DeploymentInvocationException.class)
                .hasMessageContaining("Deployment-1")
                .hasMessageContaining("Deploy");
    }

    private static class RecordingInvoker implements SubmodelOperationInvoker {
        private final OperationVariable[] response;
        String lastEndpoint;
        String lastOperation;

        RecordingInvoker(OperationVariable[] response) {
            this.response = response;
        }

        @Override
        public OperationVariable[] invoke(String endpoint, String submodelId, String idShortPath,
                                          OperationVariable[] input, String accessToken) {
            this.lastEndpoint = endpoint;
            this.lastOperation = idShortPath;
            return this.response;
        }
    }
}
