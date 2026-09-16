package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.common.access.AccessControlObjectType;
import org.eclipse.slm.common.access.AccessControlService;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MultiHostCapabilityServicePersistenceTest {

    private final CapabilityServiceJpaRepository repository = Mockito.mock(CapabilityServiceJpaRepository.class);
    private final CapabilityJpaRepository capabilityJpaRepository = Mockito.mock(CapabilityJpaRepository.class);
    private final AccessControlService accessControlService = Mockito.mock(AccessControlService.class);
    private final CapabilityServicePersistenceMapper mapper = new CapabilityServicePersistenceMapper(capabilityJpaRepository);
    private final MultiHostCapabilityServicePersistence persistence =
            new MultiHostCapabilityServicePersistence(repository, mapper, accessControlService);

    private MultiHostCapabilityService mhcs(UUID serviceId, UUID resourceId, UUID memberA) {
        Capability capability = new DeploymentCapability();
        capability.setId(UUID.randomUUID());
        capability.setName("Kubernetes");
        HashMap<UUID, String> members = new HashMap<>();
        members.put(memberA, "control-plane");
        return new MultiHostCapabilityService(resourceId, serviceId, capability, members,
                CapabilityServiceStatus.READY, true, new HashMap<>());
    }

    @Test
    public void saveCreatesOwnerPolicyWhenNew() {
        UUID serviceId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID memberA = UUID.randomUUID();
        MultiHostCapabilityService domain = mhcs(serviceId, resourceId, memberA);
        when(repository.findById(serviceId)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        persistence.save(domain, "/Org/CustomerA");

        verify(repository).save(any(CapabilityServiceEntity.class));
        verify(accessControlService).createSingleObjectPolicy(
                anyString(), eq("/Org/CustomerA"), eq(AccessControlObjectType.CAPABILITY_SERVICE), eq(serviceId));
    }

    @Test
    public void getAllReturnsOnlyMultiHost() {
        UUID serviceId = UUID.randomUUID();
        CapabilityServiceEntity entity = mapper.toEntity(mhcs(serviceId, UUID.randomUUID(), UUID.randomUUID()));
        when(repository.findByServiceClass(CapabilityServiceClass.MULTI_HOST)).thenReturn(List.of(entity));
        when(capabilityJpaRepository.findById(entity.getCapabilityId()))
                .thenReturn(Optional.of(new DeploymentCapability()));

        var all = persistence.getAll();

        assertEquals(1, all.size());
        assertEquals(serviceId, all.get(0).getServiceId());
    }

    @Test
    public void getServicesOfResourceMatchesMemberOrOwner() {
        UUID serviceId = UUID.randomUUID();
        UUID ownerResource = UUID.randomUUID();
        UUID memberA = UUID.randomUUID();
        CapabilityServiceEntity entity = mapper.toEntity(mhcs(serviceId, ownerResource, memberA));
        when(repository.findByServiceClass(CapabilityServiceClass.MULTI_HOST)).thenReturn(List.of(entity));
        when(capabilityJpaRepository.findById(entity.getCapabilityId()))
                .thenReturn(Optional.of(new DeploymentCapability()));

        assertEquals(1, persistence.getServicesOfResource(memberA).size());
        assertEquals(1, persistence.getServicesOfResource(ownerResource).size());
        assertEquals(0, persistence.getServicesOfResource(UUID.randomUUID()).size());
    }

    @Test
    public void deleteRemovesEntityAndPolicy() {
        UUID serviceId = UUID.randomUUID();
        persistence.delete(serviceId);
        verify(repository).deleteById(serviceId);
        verify(accessControlService).removeObjectFromAllPolicies(AccessControlObjectType.CAPABILITY_SERVICE, serviceId);
    }
}
