package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CapabilityServicePersistenceMapperMultiHostTest {

    private final CapabilityJpaRepository capabilityJpaRepository = Mockito.mock(CapabilityJpaRepository.class);
    private final CapabilityServicePersistenceMapper mapper =
            new CapabilityServicePersistenceMapper(capabilityJpaRepository);

    @Test
    public void roundTripMultiHostPreservesMemberMapping() {
        UUID serviceId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID memberA = UUID.randomUUID();
        Capability capability = new DeploymentCapability();
        capability.setId(UUID.randomUUID());
        capability.setName("Kubernetes");

        Map<UUID, String> members = new HashMap<>();
        members.put(memberA, "control-plane");
        MultiHostCapabilityService domain = new MultiHostCapabilityService(
                resourceId, serviceId, capability, new HashMap<>(members),
                CapabilityServiceStatus.READY, true, new HashMap<>());

        CapabilityServiceEntity entity = mapper.toEntity(domain);
        assertEquals(CapabilityServiceClass.MULTI_HOST, entity.getServiceClass());
        assertEquals("control-plane", entity.getMemberMapping().get(memberA));

        Mockito.when(capabilityJpaRepository.findById(capability.getId())).thenReturn(Optional.of(capability));
        MultiHostCapabilityService back = mapper.toMultiHostDomain(entity);
        assertEquals(serviceId, back.getServiceId());
        assertEquals("control-plane", back.getMemberMapping().get(memberA));
        assertTrue(back.getManaged());
    }
}
