package org.eclipse.slm.common.aas.submodels.deployment;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.slm.common.model.DeploymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeploymentOperationMapperTest {

    @Test
    @DisplayName("DeployRequest survives the round trip through operation variables")
    void deployRequestRoundTrip() {
        var original = new DeployRequest(
                UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"),
                DeploymentType.DOCKER_COMPOSE,
                "services:\n  web:\n    image: nginx\n".getBytes(StandardCharsets.UTF_8),
                "application/yaml",
                List.of("vendor_1/registry-a", "vendor_1/registry-b"));

        var restored = DeploymentOperationMapper.toDeployRequest(
                DeploymentOperationMapper.fromDeployRequest(original));

        assertThat(restored.serviceInstanceId()).isEqualTo(original.serviceInstanceId());
        assertThat(restored.deploymentType()).isEqualTo(DeploymentType.DOCKER_COMPOSE);
        assertThat(new String(restored.descriptor(), StandardCharsets.UTF_8))
                .isEqualTo("services:\n  web:\n    image: nginx\n");
        assertThat(restored.descriptorContentType()).isEqualTo("application/yaml");
        assertThat(restored.credentialReferences())
                .containsExactly("vendor_1/registry-a", "vendor_1/registry-b");
    }

    @Test
    @DisplayName("DeployRequest without credential references round trips to an empty list")
    void deployRequestWithoutCredentialsRoundTrips() {
        var original = new DeployRequest(UUID.randomUUID(), DeploymentType.KUBERNETES,
                "kind: Deployment\n".getBytes(StandardCharsets.UTF_8), "application/yaml", List.of());

        var restored = DeploymentOperationMapper.toDeployRequest(
                DeploymentOperationMapper.fromDeployRequest(original));

        assertThat(restored.credentialReferences()).isEmpty();
    }

    @Test
    @DisplayName("DeployResult survives the round trip in both accepted and rejected form")
    void deployResultRoundTrip() {
        var acceptedRestored = DeploymentOperationMapper.toDeployResult(
                DeploymentOperationMapper.fromDeployResult(DeployResult.accepted("job-42")));
        assertThat(acceptedRestored.accepted()).isTrue();
        assertThat(acceptedRestored.jobId()).isEqualTo("job-42");

        var rejectedRestored = DeploymentOperationMapper.toDeployResult(
                DeploymentOperationMapper.fromDeployResult(DeployResult.rejected("type not supported")));
        assertThat(rejectedRestored.accepted()).isFalse();
        assertThat(rejectedRestored.message()).isEqualTo("type not supported");
    }

    @Test
    @DisplayName("DeploymentStatus survives the round trip")
    void deploymentStatusRoundTrip() {
        var restored = DeploymentOperationMapper.toDeploymentStatus(
                DeploymentOperationMapper.fromDeploymentStatus(
                        new DeploymentStatus(DeploymentJobState.FAILED, "playbook exited 2")));

        assertThat(restored.state()).isEqualTo(DeploymentJobState.FAILED);
        assertThat(restored.message()).isEqualTo("playbook exited 2");
    }

    @Test
    @DisplayName("Job id survives the round trip for the status request")
    void statusRequestRoundTrip() {
        var jobId = DeploymentOperationMapper.toStatusRequestJobId(
                DeploymentOperationMapper.fromStatusRequestJobId("job-7"));

        assertThat(jobId).isEqualTo("job-7");
    }

    @Test
    @DisplayName("A missing required input variable is reported by name")
    void missingRequiredVariableIsReportedByName() {
        var incomplete = DeploymentOperationMapper.fromStatusRequestJobId("job-1");

        assertThatThrownBy(() -> DeploymentOperationMapper.toDeployRequest(incomplete))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ServiceInstanceId");
    }

    @Test
    @DisplayName("An unknown deployment type is rejected rather than silently defaulted")
    void unknownDeploymentTypeIsRejected() {
        var variables = DeploymentOperationMapper.fromDeployRequest(new DeployRequest(
                UUID.randomUUID(), DeploymentType.CODESYS,
                "{}".getBytes(StandardCharsets.UTF_8), "application/json", List.of()));
        DeploymentOperationMapper.overwriteStringValue(
                variables, DeploymentSubmodelTemplate.VAR_DEPLOYMENT_TYPE, "HELM");

        assertThatThrownBy(() -> DeploymentOperationMapper.toDeployRequest(variables))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HELM");
    }

    @Test
    @DisplayName("A deployment descriptor that is not a Blob is rejected with a clear message")
    void nonBlobDeploymentDescriptorIsRejected() {
        var variables = DeploymentOperationMapper.fromDeployRequest(new DeployRequest(
                UUID.randomUUID(), DeploymentType.DOCKER_COMPOSE,
                "{}".getBytes(StandardCharsets.UTF_8), "application/json", List.of()));

        for (int i = 0; i < variables.length; i++) {
            if (DeploymentSubmodelTemplate.VAR_DEPLOYMENT_DESCRIPTOR.equals(variables[i].getValue().getIdShort())) {
                variables[i] = new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort(DeploymentSubmodelTemplate.VAR_DEPLOYMENT_DESCRIPTOR)
                                .valueType(DataTypeDefXsd.STRING)
                                .value("not-a-blob")
                                .build())
                        .build();
            }
        }

        assertThatThrownBy(() -> DeploymentOperationMapper.toDeployRequest(variables))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DeploymentDescriptor")
                .hasMessageContaining("Blob");
    }
}
