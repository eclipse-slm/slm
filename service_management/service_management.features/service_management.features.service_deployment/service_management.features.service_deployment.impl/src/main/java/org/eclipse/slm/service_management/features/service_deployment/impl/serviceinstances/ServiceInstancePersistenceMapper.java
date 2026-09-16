package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances;

import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ServiceInstancePersistenceMapper {

    public ServiceInstanceEntity toEntity(ServiceInstance domain) {
        var entity = new ServiceInstanceEntity(domain.getId());
        entity.setResourceId(domain.getResourceId());
        entity.setCapabilityServiceId(domain.getCapabilityServiceId());
        entity.setServiceOfferingId(domain.getServiceOfferingId());
        entity.setServiceOfferingVersionId(domain.getServiceOfferingVersionId());
        entity.setPorts(new ArrayList<>(domain.getPorts()));
        entity.setGroupIds(new ArrayList<>(domain.getGroupIds()));

        List<String> customTags = new ArrayList<>(domain.getTags());
        customTags.removeAll(ServiceInstance.PREDEFINED_TAGS);
        entity.setCustomTags(customTags);

        Map<String, String> customMeta = new HashMap<>(domain.getMetaData());
        ServiceInstance.PREDEFINED_META_DATA_KEYS.forEach(customMeta::remove);
        entity.setCustomMetaData(customMeta);

        return entity;
    }

    public ServiceInstance toDomain(ServiceInstanceEntity entity) {
        return new ServiceInstance(
                entity.getId(),
                new ArrayList<>(entity.getCustomTags()),
                new HashMap<>(entity.getCustomMetaData()),
                entity.getResourceId(),
                entity.getCapabilityServiceId(),
                entity.getServiceOfferingId(),
                entity.getServiceOfferingVersionId(),
                new ArrayList<>(entity.getPorts()),
                new ArrayList<>(entity.getGroupIds()));
    }
}
