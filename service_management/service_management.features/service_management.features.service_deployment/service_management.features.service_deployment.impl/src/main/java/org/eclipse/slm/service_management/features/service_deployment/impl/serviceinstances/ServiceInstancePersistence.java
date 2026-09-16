package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances;

import org.eclipse.slm.common.access.AccessControlService;
import org.eclipse.slm.common.access.UserContext;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class ServiceInstancePersistence {

    public static final String SERVICE_INSTANCE_POLICY_PREFIX = "service-instance_";

    private final ServiceInstanceJpaRepository repository;
    private final ServiceInstancePersistenceMapper mapper;
    private final AccessControlService accessControlService;

    public ServiceInstancePersistence(ServiceInstanceJpaRepository repository,
                                      ServiceInstancePersistenceMapper mapper,
                                      AccessControlService accessControlService) {
        this.repository = repository;
        this.mapper = mapper;
        this.accessControlService = accessControlService;
    }

    public ServiceInstance create(ServiceInstance serviceInstance, String fullPathOwnerGroupId) {
        var entity = mapper.toEntity(serviceInstance);
        repository.save(entity);
        accessControlService.createSingleObjectPolicy(
                SERVICE_INSTANCE_POLICY_PREFIX + serviceInstance.getId(),
                fullPathOwnerGroupId,
                ServiceInstanceObjectTypes.SERVICE_INSTANCE,
                serviceInstance.getId());
        return mapper.toDomain(entity);
    }

    public void update(ServiceInstance serviceInstance) {
        repository.save(mapper.toEntity(serviceInstance));
    }

    public void delete(UUID serviceInstanceId) {
        repository.deleteById(serviceInstanceId);
        accessControlService.removeObjectFromAllPolicies(
                ServiceInstanceObjectTypes.SERVICE_INSTANCE, serviceInstanceId);
    }

    public List<ServiceInstance> getAccessible(UserContext userContext) {
        Optional<Set<UUID>> accessibleIds = accessControlService.getAccessibleObjectIds(
                userContext, ServiceInstanceObjectTypes.SERVICE_INSTANCE);

        List<ServiceInstanceEntity> entities;
        if (accessibleIds.isPresent()) {
            Set<UUID> ids = accessibleIds.get();
            entities = ids.isEmpty() ? List.of() : repository.findByIdIn(ids);
        } else {
            entities = repository.findAll();
        }

        return entities.stream().map(mapper::toDomain).toList();
    }

    public Optional<ServiceInstance> getById(UUID serviceInstanceId, UserContext userContext) {
        if (!accessControlService.hasAccess(
                ServiceInstanceObjectTypes.SERVICE_INSTANCE, serviceInstanceId, userContext)) {
            return Optional.empty();
        }
        return repository.findById(serviceInstanceId).map(mapper::toDomain);
    }

    public Optional<ServiceInstance> getByIdUnfiltered(UUID serviceInstanceId) {
        return repository.findById(serviceInstanceId).map(mapper::toDomain);
    }

    public Set<String> getOwnerGroups(UUID serviceInstanceId) {
        return accessControlService.getSubjectsForObject(
                ServiceInstanceObjectTypes.SERVICE_INSTANCE, serviceInstanceId);
    }
}
