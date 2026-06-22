package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.common.access.AccessControlObjectType;
import org.eclipse.slm.resource_management.common.access.AccessControlService;
import org.eclipse.slm.resource_management.features.capabilities.exceptions.CapabilityServiceNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.SingleHostCapabilityService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class SingleHostCapabilityServicePersistence {

    public static final String CAPABILITY_SERVICE_POLICY_PREFIX = "capability-service_";

    private final CapabilityServiceJpaRepository repository;
    private final CapabilityJpaRepository capabilityJpaRepository;
    private final CapabilityServicePersistenceMapper mapper;
    private final AccessControlService accessControlService;

    public SingleHostCapabilityServicePersistence(
            CapabilityServiceJpaRepository repository,
            CapabilityJpaRepository capabilityJpaRepository,
            CapabilityServicePersistenceMapper mapper,
            AccessControlService accessControlService) {
        this.repository = repository;
        this.capabilityJpaRepository = capabilityJpaRepository;
        this.mapper = mapper;
        this.accessControlService = accessControlService;
    }

    public SingleHostCapabilityService addSingleHostCapability(
            Capability capability, UUID resourceId, CapabilityServiceStatus status,
            Boolean isManaged, Map<String, String> configParameter, String fullPathOwnerGroupId) {
        UUID serviceId = UUID.randomUUID();
        SingleHostCapabilityService service = new SingleHostCapabilityService(
                resourceId, serviceId, capability, status, isManaged, new HashMap<>(configParameter));
        CapabilityServiceEntity entity = mapper.toEntity(service);
        repository.save(entity);
        accessControlService.createSingleObjectPolicy(
                CAPABILITY_SERVICE_POLICY_PREFIX + serviceId, fullPathOwnerGroupId,
                AccessControlObjectType.CAPABILITY_SERVICE, serviceId);
        return mapper.toSingleHostDomain(entity);
    }

    public void updateCapabilityService(SingleHostCapabilityService service) {
        repository.save(mapper.toEntity(service));
    }

    public void removeSingleHostCapability(Capability capability, UUID resourceId) {
        repository.findByResourceIdAndCapabilityId(resourceId, capability.getId())
                .ifPresent(entity -> {
                    repository.deleteById(entity.getId());
                    accessControlService.removeObjectFromAllPolicies(
                            AccessControlObjectType.CAPABILITY_SERVICE, entity.getId());
                });
    }

    public void removeCapabilityServiceFromAllResources(Capability capability) {
        repository.findAll().stream()
                .filter(e -> capability.getId().equals(e.getCapabilityId()))
                .forEach(e -> {
                    repository.deleteById(e.getId());
                    accessControlService.removeObjectFromAllPolicies(
                            AccessControlObjectType.CAPABILITY_SERVICE, e.getId());
                });
    }

    public List<SingleHostCapabilityService> getSingleHostCapabilityServicesOfResource(UUID resourceId) {
        return repository.findByResourceId(resourceId).stream()
                .filter(e -> e.getServiceClass() == CapabilityServiceClass.SINGLE_HOST)
                .map(mapper::toSingleHostDomain)
                .toList();
    }

    public SingleHostCapabilityService getCapabilityServiceOfResourceByCapabilityId(UUID capabilityId, UUID resourceId) {
        var entity = repository.findByResourceIdAndCapabilityId(resourceId, capabilityId)
                .orElseThrow(() -> new CapabilityServiceNotFoundException(
                        "Resource[id='" + resourceId + "'] has no capability service for capability[id='" + capabilityId + "']"));
        return mapper.toSingleHostDomain(entity);
    }
}
