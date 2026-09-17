package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOfferingOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeploymentTargetRestControllerTest {

    @Mock private ServiceOfferingOrderService serviceOfferingOrderService;
    @Mock private DeploymentTargetHandler deploymentTargetHandler;

    private ServiceOfferingVersionDeploymentRestController controller;

    private DeploymentTarget dockerTarget() {
        return new DeploymentTarget("Deployment-1", "https://rm/aas/submodels/Deployment-1",
                "Resource_abc", "edge-01", "Docker", "24.0.7",
                List.of(DeploymentType.DOCKER_COMPOSE), Map.of());
    }

    @Test
    @DisplayName("The endpoint returns the targets the handler found")
    void endpointReturnsTargets() {
        controller = new ServiceOfferingVersionDeploymentRestController(serviceOfferingOrderService, deploymentTargetHandler);
        when(deploymentTargetHandler.getDeploymentTargets(Optional.of(DeploymentType.DOCKER_COMPOSE)))
                .thenReturn(List.of(dockerTarget()));

        var response = controller.getDeploymentTargets(DeploymentType.DOCKER_COMPOSE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("Without a deploymentType filter the handler is asked for all targets")
    void endpointWithoutFilterAsksForAllTargets() {
        controller = new ServiceOfferingVersionDeploymentRestController(serviceOfferingOrderService, deploymentTargetHandler);
        when(deploymentTargetHandler.getDeploymentTargets(Optional.empty()))
                .thenReturn(List.of(dockerTarget()));

        var response = controller.getDeploymentTargets(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(deploymentTargetHandler).getDeploymentTargets(Optional.empty());
    }
}
