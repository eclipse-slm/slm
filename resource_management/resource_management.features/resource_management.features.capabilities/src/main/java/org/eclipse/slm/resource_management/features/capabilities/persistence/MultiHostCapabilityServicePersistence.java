package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.common.access.AccessControlObjectType;
import org.eclipse.slm.common.access.AccessControlService;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MultiHostCapabilityServicePersistence {

    public static final String CAPABILITY_SERVICE_POLICY_PREFIX = "capability-service_";

    private final CapabilityServiceJpaRepository repository;
    private final CapabilityServicePersistenceMapper mapper;
    private final AccessControlService accessControlService;

    public MultiHostCapabilityServicePersistence(
            CapabilityServiceJpaRepository repository,
            CapabilityServicePersistenceMapper mapper,
            AccessControlService accessControlService) {
        this.repository = repository;
        this.mapper = mapper;
        this.accessControlService = accessControlService;
    }

    public MultiHostCapabilityService save(MultiHostCapabilityService service, String fullPathOwnerGroupId) {
        boolean isNew = repository.findById(service.getServiceId()).isEmpty();
        repository.save(mapper.toEntity(service));
        if (isNew && fullPathOwnerGroupId != null) {
            accessControlService.createSingleObjectPolicy(
                    CAPABILITY_SERVICE_POLICY_PREFIX + service.getServiceId(),
                    fullPathOwnerGroupId,
                    AccessControlObjectType.CAPABILITY_SERVICE,
                    service.getServiceId());
        }
        return service;
    }

    public void update(MultiHostCapabilityService service) {
        repository.save(mapper.toEntity(service));
    }

    public Optional<MultiHostCapabilityService> getById(UUID serviceId) {
        return repository.findById(serviceId)
                .filter(e -> e.getServiceClass() == CapabilityServiceClass.MULTI_HOST)
                .map(mapper::toMultiHostDomain);
    }

    public List<MultiHostCapabilityService> getAll() {
        return repository.findByServiceClass(CapabilityServiceClass.MULTI_HOST).stream()
                .map(mapper::toMultiHostDomain)
                .toList();
    }

    public List<MultiHostCapabilityService> getServicesOfResource(UUID resourceId) {
        return repository.findByServiceClass(CapabilityServiceClass.MULTI_HOST).stream()
                .filter(e -> resourceId.equals(e.getResourceId())
                        || (e.getMemberMapping() != null && e.getMemberMapping().containsKey(resourceId)))
                .map(mapper::toMultiHostDomain)
                .toList();
    }

    public void delete(UUID serviceId) {
        repository.deleteById(serviceId);
        accessControlService.removeObjectFromAllPolicies(AccessControlObjectType.CAPABILITY_SERVICE, serviceId);
    }
}
