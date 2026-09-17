package org.eclipse.slm.resource_management.features.capabilities.aas;

import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.slm.common.aas.submodels.deployment.*;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeploymentSubmodelServiceTest {

    private static final UUID CAPABILITY_SERVICE_ID = UUID.randomUUID();

    @Mock private DeploymentJobManager jobManager;

    private CapabilityService capabilityService;
    private DeploymentSubmodelService submodelService;
    private JwtAuthenticationToken theToken;

    /**
     * DeploymentJobManager.deploy takes a JwtAuthenticationToken rather than a plain token
     * string (AwxCredential, an external type, has no plain-string-token constructor - see
     * DeploymentJobManagerTest.testJwt for the same pattern). The accessTokenSupplier here
     * therefore must be a Supplier<JwtAuthenticationToken>, not Supplier<String> as the
     * original plan assumed.
     */
    private JwtAuthenticationToken testJwt(String tokenValue) {
        var jwt = new Jwt(tokenValue, null, null,
                Map.of("alg", "none"), Map.of("sub", "test-user"));
        return new JwtAuthenticationToken(jwt, List.of(), "testUser");
    }

    @BeforeEach
    void setUp() {
        var capability = new DeploymentCapability();
        capability.setName("Docker");
        capability.setSupportedDeploymentTypes(List.of(DeploymentType.DOCKER_COMPOSE));

        capabilityService = CapabilityService.builder(UUID.randomUUID(), CAPABILITY_SERVICE_ID, capability)
                .status(CapabilityServiceStatus.READY)
                .customMeta(Map.of("version", "24.0.7"))
                .build();

        theToken = testJwt("the-token");
        submodelService = new DeploymentSubmodelService(capabilityService, jobManager, () -> theToken);
    }

    private OperationVariableFixture fixture() {
        return new OperationVariableFixture();
    }

    private static class OperationVariableFixture {
        DeployRequest request = new DeployRequest(UUID.randomUUID(), DeploymentType.DOCKER_COMPOSE,
                "services: {}\n".getBytes(StandardCharsets.UTF_8), "application/yaml", List.of());
    }

    @Test
    @DisplayName("getSubmodel returns the deployment submodel of the capability service")
    void getSubmodelReturnsDeploymentSubmodel() {
        assertThat(submodelService.getSubmodel().getId())
                .isEqualTo(DeploymentSubmodelTemplate.submodelIdFor(CAPABILITY_SERVICE_ID));
    }

    @Test
    @DisplayName("Invoking Deploy forwards the request to the job manager and returns its result")
    void invokingDeployForwardsToJobManager() {
        when(jobManager.deploy(eq(CAPABILITY_SERVICE_ID), any(), eq(theToken)))
                .thenReturn(DeployResult.accepted("job-1"));

        var output = submodelService.invokeOperation(DeploymentSubmodelTemplate.OP_DEPLOY,
                DeploymentOperationMapper.fromDeployRequest(fixture().request));

        var result = DeploymentOperationMapper.toDeployResult(output);
        assertThat(result.accepted()).isTrue();
        assertThat(result.jobId()).isEqualTo("job-1");
    }

    @Test
    @DisplayName("Invoking GetDeploymentStatus returns the state the job manager reports")
    void invokingStatusReturnsJobManagerState() {
        when(jobManager.getStatus("job-1"))
                .thenReturn(new DeploymentStatus(DeploymentJobState.SUCCEEDED, ""));

        var output = submodelService.invokeOperation(
                DeploymentSubmodelTemplate.OP_GET_DEPLOYMENT_STATUS,
                DeploymentOperationMapper.fromStatusRequestJobId("job-1"));

        assertThat(DeploymentOperationMapper.toDeploymentStatus(output).state())
                .isEqualTo(DeploymentJobState.SUCCEEDED);
    }

    @Test
    @DisplayName("An unknown operation path is refused as a missing element")
    void unknownOperationIsRefused() {
        assertThatThrownBy(() -> submodelService.invokeOperation("Undeploy", new org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable[0]))
                .isInstanceOf(ElementDoesNotExistException.class);
    }

    @Test
    @DisplayName("A malformed Deploy input is refused rather than forwarded")
    void malformedDeployInputIsRefused() {
        assertThatThrownBy(() -> submodelService.invokeOperation(
                DeploymentSubmodelTemplate.OP_DEPLOY,
                DeploymentOperationMapper.fromStatusRequestJobId("job-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
