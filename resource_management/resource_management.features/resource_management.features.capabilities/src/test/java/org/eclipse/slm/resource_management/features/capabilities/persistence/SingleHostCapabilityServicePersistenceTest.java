package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.common.access.AccessControlObjectType;
import org.eclipse.slm.resource_management.common.access.AccessControlService;
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

public class SingleHostCapabilityServicePersistenceTest {

    private final CapabilityServiceJpaRepository repository = Mockito.mock(CapabilityServiceJpaRepository.class);
    private final CapabilityJpaRepository capabilityJpaRepository = Mockito.mock(CapabilityJpaRepository.class);
    private final AccessControlService accessControlService = Mockito.mock(AccessControlService.class);
    private final CapabilityServicePersistenceMapper mapper = new CapabilityServicePersistenceMapper(capabilityJpaRepository);

    private final SingleHostCapabilityServicePersistence persistence =
            new SingleHostCapabilityServicePersistence(repository, capabilityJpaRepository, mapper, accessControlService);

    private Capability capability() {
        Capability c = new DeploymentCapability();
        c.setId(UUID.randomUUID());
        c.setName("Docker");
        return c;
    }

    @Test
    public void addPersistsEntityAndCreatesPolicy() {
        Capability capability = capability();
        UUID resourceId = UUID.randomUUID();
        when(repository.save(any(CapabilityServiceEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(capabilityJpaRepository.findById(capability.getId())).thenReturn(Optional.of(capability));

        var result = persistence.addSingleHostCapability(
                capability, resourceId, CapabilityServiceStatus.INSTALL, true,
                new HashMap<>(), "/Org/CustomerA");

        assertEquals(resourceId, result.getResourceId());
        assertEquals(capability.getId(), result.getCapability().getId());
        verify(repository).save(any(CapabilityServiceEntity.class));
        verify(accessControlService).createSingleObjectPolicy(
                anyString(), eq("/Org/CustomerA"), eq(AccessControlObjectType.CAPABILITY_SERVICE), any(UUID.class));
    }

    @Test
    public void removeDeletesEntityAndCleansPolicy() {
        Capability capability = capability();
        UUID resourceId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        CapabilityServiceEntity entity = new CapabilityServiceEntity(serviceId);
        entity.setResourceId(resourceId);
        entity.setCapabilityId(capability.getId());
        entity.setServiceClass(CapabilityServiceClass.SINGLE_HOST);
        when(repository.findByResourceIdAndCapabilityId(resourceId, capability.getId()))
                .thenReturn(Optional.of(entity));

        persistence.removeSingleHostCapability(capability, resourceId);

        verify(repository).deleteById(serviceId);
        verify(accessControlService).removeObjectFromAllPolicies(AccessControlObjectType.CAPABILITY_SERVICE, serviceId);
    }

    @Test
    public void removeIsNoOpWhenAbsent() {
        Capability capability = capability();
        UUID resourceId = UUID.randomUUID();
        when(repository.findByResourceIdAndCapabilityId(resourceId, capability.getId()))
                .thenReturn(Optional.empty());

        persistence.removeSingleHostCapability(capability, resourceId);

        verify(repository, never()).deleteById(any());
    }

    @Test
    public void getServicesOfResourceReturnsOnlySingleHost() {
        Capability capability = capability();
        UUID resourceId = UUID.randomUUID();
        CapabilityServiceEntity entity = new CapabilityServiceEntity(UUID.randomUUID());
        entity.setResourceId(resourceId);
        entity.setCapabilityId(capability.getId());
        entity.setServiceClass(CapabilityServiceClass.SINGLE_HOST);
        when(repository.findByResourceId(resourceId)).thenReturn(List.of(entity));
        when(capabilityJpaRepository.findById(capability.getId())).thenReturn(Optional.of(capability));

        var services = persistence.getSingleHostCapabilityServicesOfResource(resourceId);

        assertEquals(1, services.size());
        assertEquals(resourceId, services.get(0).getResourceId());
    }

    @Test
    public void removeCapabilityServiceFromAllResourcesDeletesMatching() {
        Capability capability = capability();
        UUID serviceId = UUID.randomUUID();
        CapabilityServiceEntity entity = new CapabilityServiceEntity(serviceId);
        entity.setResourceId(UUID.randomUUID());
        entity.setCapabilityId(capability.getId());
        entity.setServiceClass(CapabilityServiceClass.SINGLE_HOST);
        when(repository.findAll()).thenReturn(List.of(entity));

        persistence.removeCapabilityServiceFromAllResources(capability);

        verify(repository).deleteById(serviceId);
        verify(accessControlService).removeObjectFromAllPolicies(AccessControlObjectType.CAPABILITY_SERVICE, serviceId);
    }
}
