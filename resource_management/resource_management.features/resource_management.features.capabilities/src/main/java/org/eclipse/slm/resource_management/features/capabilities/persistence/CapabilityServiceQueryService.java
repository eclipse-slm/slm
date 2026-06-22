package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CapabilityServiceQueryService {

    private final CapabilityServiceJpaRepository repository;
    private final CapabilityServicePersistenceMapper mapper;

    public CapabilityServiceQueryService(
            CapabilityServiceJpaRepository repository,
            CapabilityServicePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CapabilityService> getCapabilityServices() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    public List<CapabilityService> getCapabilityServicesOfResource(UUID resourceId) {
        return repository.findByResourceId(resourceId).stream().map(this::toDomain).toList();
    }

    public List<CapabilityService> getCapabilityServicesByCapabilityClass(Class capabilityClass) {
        return repository.findByCapabilityClass(capabilityClass.getSimpleName()).stream()
                .map(this::toDomain).toList();
    }

    private CapabilityService toDomain(CapabilityServiceEntity entity) {
        if (entity.getServiceClass() == CapabilityServiceClass.MULTI_HOST) {
            return mapper.toMultiHostDomain(entity);
        }
        return mapper.toSingleHostDomain(entity);
    }
}
