package org.eclipse.slm.common.credentials;

import org.eclipse.slm.common.credentials.exceptions.CredentialNotFoundException;
import org.eclipse.slm.common.credentials.exceptions.CredentialPermissionDeniedException;
import org.eclipse.slm.common.credentials.model.*;
import org.eclipse.slm.common.credentials.persistence.CredentialEntityLink;
import org.eclipse.slm.common.credentials.persistence.CredentialLinkJpaRepository;
import org.eclipse.slm.common.credentials.persistence.VaultCredentialRepository;
import org.eclipse.slm.common.keycloak.testing.KeycloakTestContainer;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultClientFactory;
import org.eclipse.slm.common.vault.testing.VaultTestContainer;
import org.eclipse.slm.common.vault.testing.VaultTestContainerInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CredentialManagerImplTest {

    @Container
    private static final VaultTestContainer vaultContainer = new VaultTestContainer();

    @Container
    static KeycloakTestContainer keycloakContainer = new KeycloakTestContainer();

    private VaultClient vaultAdminClient;
    private VaultClient vaultClientUser1;
    private VaultClient vaultClientUser2;
    private VaultCredentialRepository vaultCredentialRepository;

    private CredentialLinkJpaRepository credentialLinkJpaRepository;
    private VaultClientFactory vaultClientFactory;
    private CredentialsManagerImpl credentialManager;

    private UUID entityId;
    private final String TEST_ENTITY_TYPE = "TEST_ENTITY";
    private final String TEST_CREDENTIAL_SCOPE1 = "CRED_SCOPE_1";
    private final String TEST_CREDENTIAL_SCOPE2 = "CRED_SCOPE_2";

    @BeforeAll
    void beforeAll() {
        vaultAdminClient = vaultContainer.getVaultAdminClient();
        vaultAdminClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).createKvSecretEngine();

        vaultCredentialRepository = new VaultCredentialRepository(vaultAdminClient);

        var vaultTestContainerInitializer = new VaultTestContainerInitializer(vaultContainer, true);
        vaultTestContainerInitializer.initKeycloakJwtAuth(keycloakContainer.getTestRealmIssuerUri(), "testclient");
        vaultTestContainerInitializer.initUserGroup(KeycloakTestContainer.TEST_USER1_GROUP_ID);
        vaultTestContainerInitializer.initUserGroup(KeycloakTestContainer.TEST_USER2_GROUP_ID);

        var accessTokenUser1 = keycloakContainer.getTestRealmAccessTokenForUser1();
        vaultClientUser1 = vaultContainer.getVaultUserClient(accessTokenUser1);
        var accessTokenUser2 = keycloakContainer.getTestRealmAccessTokenForUser2();
        vaultClientUser2 = vaultContainer.getVaultUserClient(accessTokenUser2);
    }

    @BeforeEach
    void setup() {
        credentialLinkJpaRepository = mock(CredentialLinkJpaRepository.class);
        vaultClientFactory = mock(VaultClientFactory.class);
        when(vaultClientFactory.createAdminClient()).thenReturn(vaultAdminClient);
        when(vaultClientFactory.createClient(any())).thenReturn(vaultClientUser1);
        when(vaultClientFactory.getVaultUrl()).thenReturn(vaultContainer.getVaultUrl());

        credentialManager = new CredentialsManagerImpl(vaultClientFactory, credentialLinkJpaRepository);
        entityId = UUID.randomUUID();
    }

    private Credential newUserPassCredential() {
        var credentialData = new CredentialDataUsernamePassword("alice", "secret");
        return new Credential(null, "", List.of(TEST_CREDENTIAL_SCOPE1), credentialData);
    }

    private Credential newKeyPairCredential() {
        var  credentialData = new CredentialDataKeyPair("priv", "pub");
        return new Credential(null, "", List.of(TEST_CREDENTIAL_SCOPE2), credentialData);
    }

    @Nested
    class GetCredentialByIdForCurrentUser {
        @Test
        void returnsDtoForAccessibleCredential() {
            var credential = newUserPassCredential();
            vaultCredentialRepository.saveCredential(credential, KeycloakTestContainer.TEST_USER1_GROUP_ID);

            var result = credentialManager.getCredentialByIdForCurrentUser(credential.getId(), keycloakContainer.getTestRealmAccessTokenForUser1());

            // Top-level DTO has id and scopes; the actual credential data is nested in `data`
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(credential.getId());
            assertThat(result.getScopesRaw()).isEqualTo(List.of(TEST_CREDENTIAL_SCOPE1));
            assertThat(result.getData())
                    .isInstanceOf(CredentialDataUsernamePasswordReadDTO.class)
                    .satisfies(d -> {
                        var cast = (CredentialDataUsernamePasswordReadDTO) d;
                        assertThat(cast.getCredentialDataType()).isEqualTo(CredentialDataType.USERNAME_PASSWORD);
                        assertThat(cast.getUsername()).isEqualTo("alice");
                    });
        }

        @Test
        void throwsWhenCredentialMissing() {
            var missingId = UUID.randomUUID();

            assertThatThrownBy(() -> credentialManager.getCredentialByIdForCurrentUser(missingId, keycloakContainer.getTestRealmAccessTokenForUser1()))
                    .isInstanceOf(CredentialPermissionDeniedException.class)
                    .hasMessageContaining(missingId.toString());
        }
    }

    @Nested
    class CreateCredential {
        @Test
        void storesCredentialAndLink() {
            // Arrange
            var credential = newUserPassCredential();
            var captor = ArgumentCaptor.forClass(CredentialEntityLink.class);
            // Act
            credentialManager.createCredential(credential, List.of(new CredentialEntityLinkCreateDTO(TEST_ENTITY_TYPE, entityId.toString())), KeycloakTestContainer.TEST_USER1_GROUP_ID);
            // Assert
            var stored = vaultCredentialRepository.findCredential(credential.getId()).orElseThrow();
            assertThat(stored.getId()).isEqualTo(credential.getId());
            // Expect two save calls (one for the credential link itself and possibly one for an audit/other link)
            verify(credentialLinkJpaRepository, times(2)).save(captor.capture());
            List<CredentialEntityLink> savedLinks = captor.getAllValues();
            assertThat(savedLinks).hasSize(2);
            assertThat(savedLinks).anyMatch(link ->
                    Objects.equals(link.getEntityId(), KeycloakTestContainer.TEST_USER1_GROUP_ID)
                            && Objects.equals(link.getEntityType(), CredentialsManagerImpl.USER_GROUP_ENTITY_TYPE));
            assertThat(savedLinks).anyMatch(link ->
                    Objects.equals(link.getEntityId(), entityId.toString())
                            && Objects.equals(link.getEntityType(), TEST_ENTITY_TYPE));
        }
    }

    @Nested
    class DeleteCredential {
        @Test
        void deletesCredentialAndLinks() {
            var credential = newKeyPairCredential();
            vaultCredentialRepository.saveCredential(credential, KeycloakTestContainer.TEST_USER1_GROUP_ID);
            var link = new CredentialEntityLink(1L, credential.getId(), TEST_ENTITY_TYPE, entityId.toString());
            when(credentialLinkJpaRepository.findByCredentialId(credential.getId())).thenReturn(List.of(link));

            credentialManager.deleteCredential(credential.getId());

            verify(credentialLinkJpaRepository).deleteAll(List.of(link));
            assertThatThrownBy(() -> vaultCredentialRepository.findCredential(credential.getId()))
                    .isInstanceOf(CredentialNotFoundException.class);
        }
    }

    @Nested
    class DeleteCredentialForUser {
        @Test
        void deletesWhenUserHasAccess() {
            var credential = newUserPassCredential();
            vaultCredentialRepository.saveCredential(credential, KeycloakTestContainer.TEST_USER1_GROUP_ID);
            var link = new CredentialEntityLink(2L, credential.getId(), TEST_ENTITY_TYPE, entityId.toString());
            when(credentialLinkJpaRepository.findByCredentialId(credential.getId())).thenReturn(List.of(link));

            credentialManager.deleteCredentialForUser(credential.getId(), keycloakContainer.getTestRealmAccessTokenForUser1());

            verify(credentialLinkJpaRepository).deleteAll(List.of(link));
            assertThatThrownBy(() -> vaultCredentialRepository.findCredential(credential.getId()))
                    .isInstanceOf(CredentialNotFoundException.class);
        }

        @Test
        void rejectsWhenUserHasNoAccess() {
            var credential = newUserPassCredential();
            vaultCredentialRepository.saveCredential(credential, KeycloakTestContainer.TEST_USER1_GROUP_ID);

            when(vaultClientFactory.createClient(any())).thenReturn(vaultClientUser2);

            assertThatThrownBy(() -> credentialManager.deleteCredentialForUser(credential.getId(), keycloakContainer.getTestRealmAccessTokenForUser2()))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    class DeleteOrUnlinkCredentialsOfEntity {
        @Test
        void deletesCredentialWhenOnlyLinkedToEntity() {
            var credential = newUserPassCredential();
            var link = new CredentialEntityLink(3L, credential.getId(), TEST_ENTITY_TYPE, entityId.toString());
            when(credentialLinkJpaRepository.findByEntityIdAndEntityType(entityId.toString(), TEST_ENTITY_TYPE)).thenReturn(List.of(link));
            when(credentialLinkJpaRepository.findByCredentialId(credential.getId())).thenReturn(List.of(link));

            var managerSpy = Mockito.spy(credentialManager);
            doNothing().when(managerSpy).deleteCredential(credential.getId());

            managerSpy.deleteOrUnlinkCredentialsOfEntity(entityId, TEST_ENTITY_TYPE);

            verify(managerSpy).deleteCredential(credential.getId());
            verify(credentialLinkJpaRepository).delete(link);
        }

        @Test
        void onlyUnlinksWhenCredentialHasMultipleLinks() {
            var credential = newUserPassCredential();
            var link1 = new CredentialEntityLink(4L, credential.getId(), TEST_ENTITY_TYPE, entityId.toString());
            var anotherEntity = UUID.randomUUID();
            var link2 = new CredentialEntityLink(5L, credential.getId(), TEST_ENTITY_TYPE, anotherEntity.toString());

            when(credentialLinkJpaRepository.findByEntityIdAndEntityType(entityId.toString(), TEST_ENTITY_TYPE)).thenReturn(List.of(link1));
            when(credentialLinkJpaRepository.findByCredentialId(credential.getId())).thenReturn(List.of(link1, link2));

            var managerSpy = Mockito.spy(credentialManager);
            doNothing().when(managerSpy).deleteCredential(any());

            managerSpy.deleteOrUnlinkCredentialsOfEntity(entityId, TEST_ENTITY_TYPE);

            verify(managerSpy, never()).deleteCredential(credential.getId());
            verify(credentialLinkJpaRepository).delete(link1);
        }
    }

    @Nested
    class UpdateCredentialScopes {
        @Test
        void addsScopesToExistingCredential() {
            var credential = newUserPassCredential();
            vaultCredentialRepository.saveCredential(credential, KeycloakTestContainer.TEST_USER1_GROUP_ID);

            credentialManager.addCredentialScopes(credential.getId(), List.of(TEST_CREDENTIAL_SCOPE2));

            var updated = vaultCredentialRepository.findCredentialOrThrow(credential.getId());
            assertThat(updated.getScopesRaw())
                    .containsExactlyInAnyOrder(TEST_CREDENTIAL_SCOPE1, TEST_CREDENTIAL_SCOPE2);
        }

        @Test
        void removesScopesFromExistingCredential() {
            var credentialData = new CredentialDataUsernamePassword("alice", "secret");
            var credential = new Credential(null, "", List.of(TEST_CREDENTIAL_SCOPE1, TEST_CREDENTIAL_SCOPE2), credentialData);
            vaultCredentialRepository.saveCredential(credential, KeycloakTestContainer.TEST_USER1_GROUP_ID);

            credentialManager.removeCredentialScopes(credential.getId(), List.of(TEST_CREDENTIAL_SCOPE1));

            var updated = vaultCredentialRepository.findCredentialOrThrow(credential.getId());
            assertThat(updated.getScopesRaw()).containsExactly(TEST_CREDENTIAL_SCOPE2);
        }

        @Test
        void noOpWhenScopesEmpty() {
            var credential = newUserPassCredential();
            vaultCredentialRepository.saveCredential(credential, KeycloakTestContainer.TEST_USER1_GROUP_ID);

            credentialManager.addCredentialScopes(credential.getId(), List.of());
            credentialManager.removeCredentialScopes(credential.getId(), List.of());

            var updated = vaultCredentialRepository.findCredentialOrThrow(credential.getId());
            assertThat(updated.getScopesRaw()).containsExactly(TEST_CREDENTIAL_SCOPE1);
        }
    }

    @Nested
    class LinkCredentialToEntity {
        @Test
        void createsLinkWhenNotAlreadyLinked() {
            var credentialId = UUID.randomUUID();
            var link = new CredentialEntityLinkCreateDTO(TEST_ENTITY_TYPE, entityId.toString());
            when(credentialLinkJpaRepository.findByEntityIdAndEntityTypeAndCredentialId(entityId.toString(), TEST_ENTITY_TYPE, credentialId))
                    .thenReturn(List.of());

            credentialManager.linkCredentialToEntity(credentialId, link);

            verify(credentialLinkJpaRepository).save(any(CredentialEntityLink.class));
        }

        @Test
        void noOpWhenAlreadyLinked() {
            var credentialId = UUID.randomUUID();
            var link = new CredentialEntityLinkCreateDTO(TEST_ENTITY_TYPE, entityId.toString());
            var existing = new CredentialEntityLink(10L, credentialId, TEST_ENTITY_TYPE, entityId.toString());
            when(credentialLinkJpaRepository.findByEntityIdAndEntityTypeAndCredentialId(entityId.toString(), TEST_ENTITY_TYPE, credentialId))
                    .thenReturn(List.of(existing));

            credentialManager.linkCredentialToEntity(credentialId, link);

            verify(credentialLinkJpaRepository, never()).save(any(CredentialEntityLink.class));
        }
    }

    @Nested
    class DeleteCredentialEntityLink {
        @Test
        void deletesLinkWhenPresent() {
            var credentialId = UUID.randomUUID();
            var credential = new Credential(credentialId, "dummy", List.of(TEST_CREDENTIAL_SCOPE1), new CredentialDataUsernamePassword("user", "pw"));
            vaultCredentialRepository.saveCredential(credential, KeycloakTestContainer.TEST_USER1_GROUP_ID);
            var link = new CredentialEntityLink(11L, credentialId, TEST_ENTITY_TYPE, entityId.toString());
            when(credentialLinkJpaRepository.findByEntityIdAndEntityTypeAndCredentialId(entityId.toString(), TEST_ENTITY_TYPE, credentialId))
                    .thenReturn(List.of(link));

            credentialManager.deleteCredentialEntityLink(credentialId, TEST_ENTITY_TYPE, entityId.toString(), false);

            verify(credentialLinkJpaRepository).deleteAll(List.of(link));
        }

        @Test
        void deletesCredentialWhenOnlyUserGroupLinksRemainAndFlagEnabled() {
            // Arrange
            var credentialId = UUID.randomUUID();
            var credential = new Credential(credentialId, "dummy", List.of(), new CredentialDataUsernamePassword("user", "pw"));
            vaultCredentialRepository.saveCredential(credential, KeycloakTestContainer.TEST_USER1_GROUP_ID);
            var link = new CredentialEntityLink(12L, credentialId, TEST_ENTITY_TYPE, entityId.toString());
            var userGroupLink = new CredentialEntityLink(13L, credentialId, CredentialsManagerImpl.USER_GROUP_ENTITY_TYPE, KeycloakTestContainer.TEST_USER1_GROUP_ID);
            when(credentialLinkJpaRepository.findByEntityIdAndEntityTypeAndCredentialId(entityId.toString(), TEST_ENTITY_TYPE, credentialId))
                    .thenReturn(List.of(link));
            when(credentialLinkJpaRepository.findByCredentialId(credentialId)).thenReturn(List.of(userGroupLink));
            // Act
            credentialManager.deleteCredentialEntityLink(credentialId, TEST_ENTITY_TYPE, entityId.toString(), true);
            // Assert
            verify(credentialLinkJpaRepository).deleteAll(List.of(link));
            assertThatThrownBy(() -> vaultCredentialRepository.findCredential(credentialId))
                    .isInstanceOf(CredentialNotFoundException.class);

        }

        @Test
        void doesNotDeleteCredentialWhenNonUserGroupLinksRemain() {
            var credentialId = UUID.randomUUID();
            var credential = new Credential(credentialId, "dummy", List.of(TEST_CREDENTIAL_SCOPE1), new CredentialDataUsernamePassword("user", "pw"));
            vaultCredentialRepository.saveCredential(credential, KeycloakTestContainer.TEST_USER1_GROUP_ID);
            var link = new CredentialEntityLink(14L, credentialId, TEST_ENTITY_TYPE, entityId.toString());
            var otherLink = new CredentialEntityLink(15L, credentialId, "OTHER_ENTITY", UUID.randomUUID().toString());
            when(credentialLinkJpaRepository.findByEntityIdAndEntityTypeAndCredentialId(entityId.toString(), TEST_ENTITY_TYPE, credentialId))
                    .thenReturn(List.of(link));
            when(credentialLinkJpaRepository.findByCredentialId(credentialId)).thenReturn(List.of(otherLink));

            var managerSpy = Mockito.spy(credentialManager);
            doNothing().when(managerSpy).deleteCredential(any());

            managerSpy.deleteCredentialEntityLink(credentialId, TEST_ENTITY_TYPE, entityId.toString(), true);

            verify(credentialLinkJpaRepository).deleteAll(List.of(link));
            verify(managerSpy, never()).deleteCredential(credentialId);
        }
    }
}
