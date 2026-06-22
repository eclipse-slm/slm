package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.SingleHostCapabilityService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CapabilityServicePersistenceMapperTest {

    private final CapabilityJpaRepository capabilityJpaRepository = Mockito.mock(CapabilityJpaRepository.class);
    private final CapabilityServicePersistenceMapper mapper =
            new CapabilityServicePersistenceMapper(capabilityJpaRepository);

    @Test
    public void toEntityThenToDomainRoundTripsSingleHost() {
        UUID serviceId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        Capability capability = new DeploymentCapability();
        capability.setId(UUID.randomUUID());
        capability.setName("Docker");

        HashMap<String, String> config = new HashMap<>();
        config.put("param", "x");
        SingleHostCapabilityService domain = new SingleHostCapabilityService(
                resourceId, serviceId, capability, CapabilityServiceStatus.READY, true, config);
        domain.setPort(8080);

        CapabilityServiceEntity entity = mapper.toEntity(domain);
        assertEquals(serviceId, entity.getId());
        assertEquals(resourceId, entity.getResourceId());
        assertEquals(capability.getId(), entity.getCapabilityId());
        assertEquals(CapabilityServiceStatus.READY, entity.getStatus());
        assertTrue(entity.getManaged());
        assertEquals(8080, entity.getPort());
        assertEquals(CapabilityServiceClass.SINGLE_HOST, entity.getServiceClass());

        Mockito.when(capabilityJpaRepository.findById(capability.getId()))
                .thenReturn(Optional.of(capability));
        SingleHostCapabilityService back = mapper.toSingleHostDomain(entity);
        assertEquals(serviceId, back.getServiceId());
        assertEquals(resourceId, back.getResourceId());
        assertEquals(capability.getId(), back.getCapability().getId());
        assertEquals(CapabilityServiceStatus.READY, back.getStatus());
        assertTrue(back.getManaged());
        assertEquals(8080, back.getPort());
    }
}
