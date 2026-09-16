package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ServiceInstanceJpaRepositoryTest {

    @Autowired
    private ServiceInstanceJpaRepository repository;

    @Test
    public void savesAndReadsBackAllFields() {
        var id = UUID.randomUUID();
        var resourceId = UUID.randomUUID();
        var capabilityServiceId = UUID.randomUUID();
        var offeringId = UUID.randomUUID();
        var offeringVersionId = UUID.randomUUID();
        var groupId = UUID.randomUUID();

        var entity = new ServiceInstanceEntity(id);
        entity.setResourceId(resourceId);
        entity.setCapabilityServiceId(capabilityServiceId);
        entity.setServiceOfferingId(offeringId);
        entity.setServiceOfferingVersionId(offeringVersionId);
        entity.setPorts(List.of(8080, 8443));
        entity.setGroupIds(List.of(groupId));
        entity.setCustomTags(List.of("alpha"));
        entity.setCustomMetaData(Map.of("key", "value"));

        repository.save(entity);

        var found = repository.findById(id).orElseThrow();
        assertThat(found.getResourceId()).isEqualTo(resourceId);
        assertThat(found.getCapabilityServiceId()).isEqualTo(capabilityServiceId);
        assertThat(found.getServiceOfferingId()).isEqualTo(offeringId);
        assertThat(found.getServiceOfferingVersionId()).isEqualTo(offeringVersionId);
        assertThat(found.getPorts()).containsExactly(8080, 8443);
        assertThat(found.getGroupIds()).containsExactly(groupId);
        assertThat(found.getCustomTags()).containsExactly("alpha");
        assertThat(found.getCustomMetaData()).containsEntry("key", "value");
    }

    @Test
    public void findsByIdIn() {
        var idA = UUID.randomUUID();
        var idB = UUID.randomUUID();
        repository.save(new ServiceInstanceEntity(idA));
        repository.save(new ServiceInstanceEntity(idB));

        var found = repository.findByIdIn(Set.of(idA));

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(idA);
    }
}
