package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClient;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClient;
import org.eclipse.slm.common.model.DeploymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeploymentTargetHandlerTest {

    @Mock private SubmodelRegistryClient submodelRegistryClient;
    @Mock private DeploymentSubmodelReader submodelReader;

    private DeploymentTargetHandler handler() {
        return new DeploymentTargetHandler(submodelRegistryClient, submodelReader);
    }

    private DeploymentTarget dockerTarget() {
        return new DeploymentTarget("Deployment-1", "https://rm/aas/submodels/Deployment-1",
                "Resource_abc", "edge-01", "Docker", "24.0.7",
                List.of(DeploymentType.DOCKER_COMPOSE), java.util.Map.of());
    }

    @Test
    @DisplayName("Targets are discovered by the deployment template semantic id")
    void targetsAreDiscoveredBySemanticId() {
        var descriptor = org.mockito.Mockito.mock(SubmodelDescriptor.class);
        when(submodelRegistryClient.findSubmodelDescriptorsWithSemanticIds(
                List.of("https://eclipse.dev/slm/submodels/Deployment/1/0")))
                .thenReturn(List.of(descriptor));
        when(submodelReader.read(descriptor)).thenReturn(Optional.of(dockerTarget()));

        assertThat(handler().getDeploymentTargets(Optional.empty()))
                .extracting(DeploymentTarget::mechanismName)
                .containsExactly("Docker");
    }

    @Test
    @DisplayName("Filtering by deployment type keeps only targets that declare it")
    void filteringByDeploymentTypeKeepsOnlyMatchingTargets() {
        var descriptor = org.mockito.Mockito.mock(SubmodelDescriptor.class);
        when(submodelRegistryClient.findSubmodelDescriptorsWithSemanticIds(any()))
                .thenReturn(List.of(descriptor));
        when(submodelReader.read(descriptor)).thenReturn(Optional.of(dockerTarget()));

        assertThat(handler().getDeploymentTargets(Optional.of(DeploymentType.KUBERNETES))).isEmpty();
        assertThat(handler().getDeploymentTargets(Optional.of(DeploymentType.DOCKER_COMPOSE))).hasSize(1);
    }

    @Test
    @DisplayName("A submodel that cannot be read is skipped instead of failing the whole list")
    void unreadableSubmodelIsSkipped() {
        var broken = org.mockito.Mockito.mock(SubmodelDescriptor.class);
        when(submodelRegistryClient.findSubmodelDescriptorsWithSemanticIds(any()))
                .thenReturn(List.of(broken));
        when(submodelReader.read(broken)).thenReturn(Optional.empty());

        assertThat(handler().getDeploymentTargets(Optional.empty())).isEmpty();
    }

    @Test
    @DisplayName("Requesting an unknown target reports it as not found")
    void unknownTargetIsReported() {
        when(submodelRegistryClient.findSubmodelDescriptorsWithSemanticIds(any())).thenReturn(List.of());

        assertThatThrownBy(() -> handler().getDeploymentTargetOrThrow("Deployment-nope"))
                .isInstanceOf(DeploymentTargetNotFoundException.class)
                .hasMessageContaining("Deployment-nope");
    }

    @Test
    @DisplayName("A null submodel id is reported as not found instead of throwing a NullPointerException")
    void nullSubmodelIdIsReportedAsNotFound() {
        assertThatThrownBy(() -> handler().getDeploymentTargetOrThrow(null))
                .isInstanceOf(DeploymentTargetNotFoundException.class)
                .isNotInstanceOf(NullPointerException.class);
    }
}
