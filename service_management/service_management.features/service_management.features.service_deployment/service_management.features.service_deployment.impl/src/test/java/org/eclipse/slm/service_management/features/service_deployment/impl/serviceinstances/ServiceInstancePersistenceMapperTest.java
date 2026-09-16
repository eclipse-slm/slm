package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances;

import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceInstancePersistenceMapperTest {

    private final ServiceInstancePersistenceMapper mapper = new ServiceInstancePersistenceMapper();

    @Test
    public void roundTripsAllFields() {
        var id = UUID.randomUUID();
        var resourceId = UUID.randomUUID();
        var capabilityServiceId = UUID.randomUUID();
        var offeringId = UUID.randomUUID();
        var offeringVersionId = UUID.randomUUID();
        var groupId = UUID.randomUUID();

        var domain = new ServiceInstance(
                id, List.of("alpha"), Map.of("key", "value"),
                resourceId, capabilityServiceId, offeringId, offeringVersionId,
                List.of(8080), List.of(groupId));

        var entity = mapper.toEntity(domain);
        var back = mapper.toDomain(entity);

        assertThat(back.getId()).isEqualTo(id);
        assertThat(back.getResourceId()).isEqualTo(resourceId);
        assertThat(back.getCapabilityServiceId()).isEqualTo(capabilityServiceId);
        assertThat(back.getServiceOfferingId()).isEqualTo(offeringId);
        assertThat(back.getServiceOfferingVersionId()).isEqualTo(offeringVersionId);
        assertThat(back.getPorts()).containsExactly(8080);
        assertThat(back.getGroupIds()).containsExactly(groupId);
    }

    @Test
    public void derivedTagsIncludePredefinedTag() {
        var domain = new ServiceInstance(
                UUID.randomUUID(), List.of("alpha"), Map.of(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                List.of(), List.of());

        var back = mapper.toDomain(mapper.toEntity(domain));

        assertThat(back.getTags()).contains("alpha", "service");
    }

    @Test
    public void derivedMetaDataIncludesPredefinedKeys() {
        var id = UUID.randomUUID();
        var domain = new ServiceInstance(
                id, List.of(), Map.of("custom", "x"),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                List.of(), List.of());

        var back = mapper.toDomain(mapper.toEntity(domain));

        assertThat(back.getMetaData())
                .containsEntry(ServiceInstance.META_DATA_KEY_SERVICE_INSTANCE_ID, id.toString())
                .containsEntry("custom", "x");
    }

    @Test
    public void entityStoresOnlyCustomTagsAndMeta() {
        var domain = new ServiceInstance(
                UUID.randomUUID(), List.of("alpha"), Map.of("custom", "x"),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                List.of(8080), List.of());

        var entity = mapper.toEntity(domain);

        assertThat(entity.getCustomTags()).containsExactly("alpha");
        assertThat(entity.getCustomMetaData()).containsOnlyKeys("custom");
    }
}
