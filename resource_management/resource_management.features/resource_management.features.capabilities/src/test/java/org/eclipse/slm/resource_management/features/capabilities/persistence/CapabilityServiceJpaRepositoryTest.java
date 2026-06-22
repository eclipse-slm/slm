package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CapabilityServiceJpaRepositoryTest {

    @Autowired
    private CapabilityServiceJpaRepository repository;

    private CapabilityServiceEntity singleHost(UUID id, UUID resourceId, UUID capabilityId) {
        CapabilityServiceEntity e = new CapabilityServiceEntity();
        e.setId(id);
        e.setResourceId(resourceId);
        e.setCapabilityId(capabilityId);
        e.setStatus(CapabilityServiceStatus.READY);
        e.setManaged(true);
        e.setPort(8080);
        e.setCustomMeta(new java.util.HashMap<>(Map.of("k", "v")));
        e.setServiceClass(CapabilityServiceClass.SINGLE_HOST);
        return e;
    }

    @Test
    public void saveAndLoadPreservesFieldsIncludingJsonMeta() {
        UUID id = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID capabilityId = UUID.randomUUID();
        repository.save(singleHost(id, resourceId, capabilityId));

        Optional<CapabilityServiceEntity> loaded = repository.findById(id);
        assertTrue(loaded.isPresent());
        assertEquals(resourceId, loaded.get().getResourceId());
        assertEquals(capabilityId, loaded.get().getCapabilityId());
        assertEquals(CapabilityServiceStatus.READY, loaded.get().getStatus());
        assertTrue(loaded.get().getManaged());
        assertEquals(8080, loaded.get().getPort());
        assertEquals("v", loaded.get().getCustomMeta().get("k"));
        assertEquals(CapabilityServiceClass.SINGLE_HOST, loaded.get().getServiceClass());
    }

    @Test
    public void findByResourceIdAndCapabilityId() {
        UUID resourceId = UUID.randomUUID();
        UUID capabilityId = UUID.randomUUID();
        repository.save(singleHost(UUID.randomUUID(), resourceId, capabilityId));
        repository.save(singleHost(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));

        List<CapabilityServiceEntity> byResource = repository.findByResourceId(resourceId);
        Optional<CapabilityServiceEntity> byBoth =
                repository.findByResourceIdAndCapabilityId(resourceId, capabilityId);

        assertEquals(1, byResource.size());
        assertTrue(byBoth.isPresent());
        assertEquals(capabilityId, byBoth.get().getCapabilityId());
    }

    @Test
    public void findByServiceClassFiltersDiscriminator() {
        repository.save(singleHost(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        assertEquals(1, repository.findByServiceClass(CapabilityServiceClass.SINGLE_HOST).size());
        assertEquals(0, repository.findByServiceClass(CapabilityServiceClass.MULTI_HOST).size());
    }
}
