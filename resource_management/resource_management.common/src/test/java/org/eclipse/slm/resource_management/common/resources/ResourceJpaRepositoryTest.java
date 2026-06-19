package org.eclipse.slm.resource_management.common.resources;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ResourceJpaRepositoryTest {

    @Autowired
    private ResourceJpaRepository resourceJpaRepository;

    @Test
    public void saveAndLoadResourcePreservesFields() {
        UUID id = UUID.randomUUID();
        BasicResource resource = new BasicResource(id, "host-1", "10.0.0.1");
        resource.setAssetId("asset-1");
        resource.setFirmwareVersion("1.2.3");
        resource.setDriverId("driver-1");

        resourceJpaRepository.save(resource);

        Optional<BasicResource> loaded = resourceJpaRepository.findById(id);
        assertTrue(loaded.isPresent());
        assertEquals("host-1", loaded.get().getHostname());
        assertEquals("10.0.0.1", loaded.get().getIp());
        assertEquals("asset-1", loaded.get().getAssetId());
        assertEquals("1.2.3", loaded.get().getFirmwareVersion());
        assertEquals("driver-1", loaded.get().getDriverId());
    }

    @Test
    public void findByIdInReturnsOnlyRequested() {
        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();
        resourceJpaRepository.save(new BasicResource(idA, "a", "10.0.0.1"));
        resourceJpaRepository.save(new BasicResource(idB, "b", "10.0.0.2"));

        List<BasicResource> result = resourceJpaRepository.findByIdIn(Set.of(idA));

        assertEquals(1, result.size());
        assertEquals(idA, result.get(0).getId());
    }
}
