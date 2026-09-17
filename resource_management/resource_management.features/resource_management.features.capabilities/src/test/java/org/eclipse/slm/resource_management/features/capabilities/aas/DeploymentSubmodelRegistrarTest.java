package org.eclipse.slm.resource_management.features.capabilities.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClient;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClient;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentSubmodelTemplate;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.features.capabilities.model.*;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeploymentSubmodelRegistrarTest {

    private static final UUID RESOURCE_ID = UUID.randomUUID();
    private static final UUID CAPABILITY_SERVICE_ID = UUID.randomUUID();

    @Mock private SubmodelRegistryClient submodelRegistryClient;
    @Mock private AasRepositoryClient aasRepositoryClient;
    @Mock private CapabilitiesConsulClient capabilitiesConsulClient;

    private CapabilityService dockerCapabilityService() {
        var capability = new DeploymentCapability();
        capability.setName("Docker");
        capability.setSupportedDeploymentTypes(List.of(DeploymentType.DOCKER_COMPOSE));
        return CapabilityService.builder(RESOURCE_ID, CAPABILITY_SERVICE_ID, capability)
                .status(CapabilityServiceStatus.READY).customMeta(Map.of()).build();
    }

    private DeploymentSubmodelRegistrar registrar() {
        return new DeploymentSubmodelRegistrar(submodelRegistryClient, aasRepositoryClient,
                capabilitiesConsulClient, "https://rm.example.invalid");
    }

    @Test
    @DisplayName("Registering adds a registry descriptor and an AAS submodel reference")
    void registeringAddsDescriptorAndReference() {
        registrar().register(dockerCapabilityService());

        verify(submodelRegistryClient).registerSubmodel(
                contains(DeploymentSubmodelTemplate.submodelIdFor(CAPABILITY_SERVICE_ID)),
                eq(DeploymentSubmodelTemplate.submodelIdFor(CAPABILITY_SERVICE_ID)),
                eq("Deployment_Docker"),
                eq(DeploymentSubmodelTemplate.SEMANTIC_ID_VALUE));
        verify(aasRepositoryClient).addSubmodelReferenceToAas(eq("Resource_" + RESOURCE_ID), any(Submodel.class));
    }

    @Test
    @DisplayName("Unregistering removes descriptor and reference")
    void unregisteringRemovesDescriptorAndReference() {
        registrar().unregister(RESOURCE_ID, CAPABILITY_SERVICE_ID);

        var submodelId = DeploymentSubmodelTemplate.submodelIdFor(CAPABILITY_SERVICE_ID);
        verify(submodelRegistryClient).unregisterSubmodel(submodelId);
        verify(aasRepositoryClient).removeSubmodelReferenceFromAas("Resource_" + RESOURCE_ID, submodelId);
    }

    @Test
    @DisplayName("A non-deployment capability service is not registered")
    void nonDeploymentCapabilityIsNotRegistered() {
        var baseConfig = new BaseConfigurationCapability();
        baseConfig.setName("Base Config");
        var capabilityService = CapabilityService.builder(RESOURCE_ID, UUID.randomUUID(), baseConfig)
                .status(CapabilityServiceStatus.READY).customMeta(Map.of()).build();

        registrar().register(capabilityService);

        verifyNoInteractions(submodelRegistryClient);
        verifyNoInteractions(aasRepositoryClient);
    }

    @Test
    @DisplayName("On startup every existing deployment capability service is registered")
    void startupRegistersExistingCapabilityServices() {
        when(capabilitiesConsulClient.getCapabilityServices())
                .thenReturn(List.of(dockerCapabilityService()));

        registrar().registerExistingCapabilityServices();

        verify(submodelRegistryClient).registerSubmodel(
                any(String.class), any(String.class), any(String.class), any(String.class));
    }
}
