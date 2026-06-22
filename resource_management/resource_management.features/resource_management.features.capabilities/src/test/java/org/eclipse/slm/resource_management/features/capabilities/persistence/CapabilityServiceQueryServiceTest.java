package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CapabilityServiceQueryServiceTest {

    private final CapabilityServiceJpaRepository repository = Mockito.mock(CapabilityServiceJpaRepository.class);
    private final CapabilityJpaRepository capabilityJpaRepository = Mockito.mock(CapabilityJpaRepository.class);
    private final CapabilityServicePersistenceMapper mapper = new CapabilityServicePersistenceMapper(capabilityJpaRepository);
    private final CapabilityServiceQueryService queryService =
            new CapabilityServiceQueryService(repository, mapper);

    private CapabilityServiceEntity entity(UUID resourceId, Capability capability) {
        CapabilityServiceEntity e = new CapabilityServiceEntity(UUID.randomUUID());
        e.setResourceId(resourceId);
        e.setCapabilityId(capability.getId());
        e.setServiceClass(CapabilityServiceClass.SINGLE_HOST);
        return e;
    }

    @Test
    public void getCapabilityServicesByCapabilityClassMapsResults() {
        Capability capability = new DeploymentCapability();
        capability.setId(UUID.randomUUID());
        UUID resourceId = UUID.randomUUID();
        when(repository.findByCapabilityClass("DeploymentCapability"))
                .thenReturn(List.of(entity(resourceId, capability)));
        when(capabilityJpaRepository.findById(capability.getId())).thenReturn(Optional.of(capability));

        var result = queryService.getCapabilityServicesByCapabilityClass(DeploymentCapability.class);

        assertEquals(1, result.size());
        assertEquals(resourceId, result.get(0).getResourceId());
    }

    @Test
    public void getCapabilityServicesOfResourceMapsResults() {
        Capability capability = new DeploymentCapability();
        capability.setId(UUID.randomUUID());
        UUID resourceId = UUID.randomUUID();
        when(repository.findByResourceId(resourceId)).thenReturn(List.of(entity(resourceId, capability)));
        when(capabilityJpaRepository.findById(capability.getId())).thenReturn(Optional.of(capability));

        var result = queryService.getCapabilityServicesOfResource(resourceId);

        assertEquals(1, result.size());
        assertEquals(resourceId, result.get(0).getResourceId());
    }
}
