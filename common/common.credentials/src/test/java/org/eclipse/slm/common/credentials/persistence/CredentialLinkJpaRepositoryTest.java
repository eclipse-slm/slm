package org.eclipse.slm.common.credentials.persistence;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EnableAutoConfiguration
@ContextConfiguration(classes = {
        CredentialLinkJpaRepository.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class CredentialLinkJpaRepositoryTest {


    @Autowired
    private CredentialLinkJpaRepository repository;

    private UUID credentialA;
    private UUID credentialB;
    private UUID entityX;
    private UUID entityY;
    private final String TEST_ENTITY_TYPE = "TEST_ENTITY_TYPE";

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        credentialA = UUID.randomUUID();
        credentialB = UUID.randomUUID();
        entityX = UUID.randomUUID();
        entityY = UUID.randomUUID();
    }

    private CredentialEntityLink saveLink(UUID credentialId, UUID entityId) {
        var response = repository.saveAndFlush(new CredentialEntityLink(null, credentialId, TEST_ENTITY_TYPE, entityId.toString()));
        return response;

    }

    @Nested
    class FindByCredentialId {
        @Test
        void returnsAllLinksForGivenCredential() {
            var link1 = saveLink(credentialA, entityX);
            var link2 = saveLink(credentialA, entityY);
            saveLink(credentialB, entityX); // other credential

            var result = repository.findByCredentialId(credentialA);

            assertThat(result)
                    .hasSize(2)
                    .containsExactlyInAnyOrder(link1, link2);
        }

        @Test
        void returnsEmptyWhenNoLinksExist() {
            saveLink(credentialB, entityX);

            var result = repository.findByCredentialId(credentialA);

            assertThat(result).isEmpty();
        }
    }

    @Test
    void returnsAllLinksForEntity() {
        var link1 = saveLink(credentialA, entityX);
        var link2 = saveLink(credentialB, entityX);
        saveLink(credentialA, entityY); // other entity

        var result = repository.findByEntityIdAndEntityType(entityX.toString(), TEST_ENTITY_TYPE);

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(link1, link2);
    }

    @Nested
    class FindByEntityId {
        @Test
        void returnsAllLinksForEntity() {
            var link1 = saveLink(credentialA, entityX);
            var link2 = saveLink(credentialB, entityX);
            saveLink(credentialA, entityY); // other entity


            var result = repository.findByEntityIdAndEntityType(entityX.toString(), TEST_ENTITY_TYPE);

            assertThat(result)
                    .hasSize(2)
                    .containsExactlyInAnyOrder(link1, link2);
        }

        @Test
        void returnsEmptyWhenEntityHasNoLinks() {
            saveLink(credentialA, entityY);

            var result = repository.findByEntityIdAndEntityType(entityX.toString(), TEST_ENTITY_TYPE);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class DeleteByCredentialId {
        @Test
        void deletesOnlyLinksForCredential() {
            saveLink(credentialA, entityX);
            saveLink(credentialA, entityY);
            var other = saveLink(credentialB, entityX);

            repository.deleteByCredentialId(credentialA);

            var remaining = repository.findAll();
            assertThat(remaining)
                    .hasSize(1)
                    .containsExactly(other);
            assertThat(repository.findByCredentialId(credentialA)).isEmpty();
        }

        @Test
        void noOpWhenCredentialUnknown() {
            var existing = saveLink(credentialB, entityX);

            repository.deleteByCredentialId(credentialA);

            assertThat(repository.findAll()).containsExactly(existing);
        }
    }
}
