package org.eclipse.slm.resource_management.common.remote_access;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RemoteAccessJpaRepositoryTest {

    @Autowired
    private RemoteAccessJpaRepository repository;

    @Test
    public void saveAndFindById() {
        UUID resourceId = UUID.randomUUID();
        RemoteAccessEntity entity = new RemoteAccessEntity();
        entity.setResourceId(resourceId);
        entity.setConnectionType(ConnectionType.ssh);
        entity.setCredentialId(UUID.randomUUID());
        entity.setConnectionPort(22);

        repository.save(entity);
        var found = repository.findById(entity.getId());

        assertTrue(found.isPresent());
        assertEquals(resourceId, found.get().getResourceId());
        assertEquals(ConnectionType.ssh, found.get().getConnectionType());
        assertEquals(22, found.get().getConnectionPort());
    }

    @Test
    public void findByResourceIdReturnsOnlyMatchingEntries() {
        UUID resourceA = UUID.randomUUID();
        UUID resourceB = UUID.randomUUID();

        RemoteAccessEntity entityA = new RemoteAccessEntity();
        entityA.setResourceId(resourceA);
        entityA.setConnectionType(ConnectionType.ssh);
        entityA.setCredentialId(UUID.randomUUID());
        entityA.setConnectionPort(22);
        repository.save(entityA);

        RemoteAccessEntity entityB = new RemoteAccessEntity();
        entityB.setResourceId(resourceB);
        entityB.setConnectionType(ConnectionType.WinRM);
        entityB.setCredentialId(UUID.randomUUID());
        entityB.setConnectionPort(5985);
        repository.save(entityB);

        var result = repository.findByResourceId(resourceA);

        assertEquals(1, result.size());
        assertEquals(entityA.getId(), result.get(0).getId());
    }

    @Test
    public void usernameIsPersistedAndReadBack() {
        RemoteAccessEntity entity = new RemoteAccessEntity();
        entity.setResourceId(UUID.randomUUID());
        entity.setConnectionType(ConnectionType.ssh);
        entity.setCredentialId(UUID.randomUUID());
        entity.setConnectionPort(22);
        entity.setUsername("admin");
        repository.save(entity);

        var found = repository.findById(entity.getId()).orElseThrow();
        assertEquals("admin", found.getUsername());
    }
}
