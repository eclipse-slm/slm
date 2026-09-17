package org.eclipse.slm.resource_management.features.capabilities.aas;

import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.slm.aas.repositories.exceptions.SubmodelNotFoundException;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentSubmodelTemplate;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.common.aas.ResourceAas;
import org.eclipse.slm.resource_management.features.capabilities.model.*;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeploymentSubmodelRepositoryTest {

    private static final UUID RESOURCE_ID = UUID.randomUUID();
    private static final UUID DOCKER_SERVICE_ID = UUID.randomUUID();
    private static final UUID K3S_SERVICE_ID = UUID.randomUUID();

    @Mock private CapabilitiesConsulClient capabilitiesConsulClient;
    @Mock private DeploymentJobManager jobManager;

    /**
     * See DeploymentJobManagerTest/DeploymentSubmodelServiceTest for why this needs to be a real
     * JwtAuthenticationToken rather than a plain string: DeploymentJobManager.deploy requires one
     * (the external AwxCredential type has no plain-string-token constructor).
     */
    private JwtAuthenticationToken testJwt(String tokenValue) {
        var jwt = new Jwt(tokenValue, null, null,
                Map.of("alg", "none"), Map.of("sub", "test-user"));
        return new JwtAuthenticationToken(jwt, List.of(), "testUser");
    }

    private CapabilityService capabilityService(UUID serviceId, String name, DeploymentType type) {
        var capability = new DeploymentCapability();
        capability.setName(name);
        capability.setSupportedDeploymentTypes(List.of(type));
        return CapabilityService.builder(RESOURCE_ID, serviceId, capability)
                .status(CapabilityServiceStatus.READY)
                .customMeta(Map.of())
                .build();
    }

    private DeploymentSubmodelRepository repository() {
        return new DeploymentSubmodelRepository(
                ResourceAas.createAasIdFromResourceId(RESOURCE_ID),
                capabilitiesConsulClient, jobManager, () -> testJwt("token"));
    }

    @Test
    @DisplayName("A resource with two deployment capabilities serves two deployment submodels")
    void twoCapabilitiesServeTwoSubmodels() {
        when(capabilitiesConsulClient.getCapabilityServicesOfResource(RESOURCE_ID)).thenReturn(List.of(
                capabilityService(DOCKER_SERVICE_ID, "Docker", DeploymentType.DOCKER_COMPOSE),
                capabilityService(K3S_SERVICE_ID, "K3s", DeploymentType.KUBERNETES)));

        var submodels = repository().getAllSubmodels(new PaginationInfo(0, null)).getResult();

        assertThat(submodels).extracting(submodel -> submodel.getIdShort())
                .containsExactlyInAnyOrder("Deployment_Docker", "Deployment_K3s");
    }

    @Test
    @DisplayName("Non-deployment capability services contribute no submodel")
    void nonDeploymentCapabilitiesAreIgnored() {
        var baseConfig = new BaseConfigurationCapability();
        baseConfig.setName("Base Config");
        when(capabilitiesConsulClient.getCapabilityServicesOfResource(RESOURCE_ID)).thenReturn(List.of(
                CapabilityService.builder(RESOURCE_ID, UUID.randomUUID(), baseConfig)
                        .status(CapabilityServiceStatus.READY).customMeta(Map.of()).build()));

        assertThat(repository().getAllSubmodels(new PaginationInfo(0, null)).getResult()).isEmpty();
    }

    @Test
    @DisplayName("A submodel is retrievable by its id")
    void submodelIsRetrievableById() {
        when(capabilitiesConsulClient.getCapabilityServicesOfResource(RESOURCE_ID)).thenReturn(List.of(
                capabilityService(DOCKER_SERVICE_ID, "Docker", DeploymentType.DOCKER_COMPOSE)));

        var submodel = repository().getSubmodel(DeploymentSubmodelTemplate.submodelIdFor(DOCKER_SERVICE_ID));

        assertThat(submodel.getIdShort()).isEqualTo("Deployment_Docker");
    }

    @Test
    @DisplayName("An unknown submodel id is reported as not found")
    void unknownSubmodelIdIsReported() {
        when(capabilitiesConsulClient.getCapabilityServicesOfResource(RESOURCE_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> repository().getSubmodel("Deployment-" + UUID.randomUUID()))
                .isInstanceOf(SubmodelNotFoundException.class);
    }
}
