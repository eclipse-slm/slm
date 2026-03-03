package org.eclipse.slm.common.credentials.persistence;

import org.eclipse.slm.common.credentials.model.Credential;
import org.eclipse.slm.common.credentials.model.CredentialDataKeyPair;
import org.eclipse.slm.common.credentials.model.CredentialDataUsernamePassword;
import org.eclipse.slm.common.keycloak.testing.KeycloakTestContainer;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.exceptions.VaultPermissionDeniedException;
import org.eclipse.slm.common.vault.testing.VaultTestContainer;
import org.eclipse.slm.common.vault.testing.VaultTestContainerInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VaultCredentialRepositoryIT {

    @Container
    private static final VaultTestContainer vaultContainer = new VaultTestContainer();

    @Container
    static KeycloakTestContainer keycloakContainer = new KeycloakTestContainer();

    private static VaultClient vaultAdminClient;
    private static VaultClient vaultClientUser1;
    private static VaultClient vaultClientUser2;
    private static VaultCredentialRepository vaultCredentialRepository;

    private final List<String> TEST_CREDENTIAL_SCOPES = List.of("CRED_SCOPE_1");
    
    @BeforeAll
    void beforeAll() {
        vaultAdminClient = vaultContainer.getVaultAdminClient();
        // Create KV secret engine for testing
        vaultAdminClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).createKvSecretEngine();

        vaultCredentialRepository = new VaultCredentialRepository(vaultAdminClient);

        // Init Vault with Keycloak JWT Auth and user group
        var vaultTestContainerInitializer = new VaultTestContainerInitializer(vaultContainer, true);
        vaultTestContainerInitializer.initKeycloakJwtAuth(keycloakContainer.getTestRealmIssuerUri(), "testclient");
        vaultTestContainerInitializer.initUserGroup(KeycloakTestContainer.TEST_USER1_GROUP_ID);
        vaultTestContainerInitializer.initUserGroup(KeycloakTestContainer.TEST_USER2_GROUP_ID);

        // Create JWT authentication token for tests
        var accessTokenUser1 = keycloakContainer.getTestRealmAccessTokenForUser1();
        vaultClientUser1 = vaultContainer.getVaultUserClient(accessTokenUser1);
        var accessTokenUser2 = keycloakContainer.getTestRealmAccessTokenForUser2();
        vaultClientUser2 = vaultContainer.getVaultUserClient(accessTokenUser2);
    }

    @Nested
    class StoreCredential {
        @Test
        void shouldStoreCredentialUsernamePassword() {
            // Arrange
            var credentialId = UUID.randomUUID();
            var credentialVaultKvPath = credentialId.toString();
            var cred = new Credential(credentialId, "", TEST_CREDENTIAL_SCOPES, new CredentialDataUsernamePassword("testuser", "testpass"));
            // Act
            vaultCredentialRepository.saveCredential(cred, KeycloakTestContainer.TEST_USER1_GROUP_ID);
            // Assert that user 1 group has the correct policy assigned to access the credential
            var vaultCredentialPolicyName = VaultCredentialRepository.getCredentialPolicyNameById(credentialId);
            var vaultGroup = vaultAdminClient.acl().getGroupByName(KeycloakTestContainer.TEST_USER1_GROUP_ID);
            assertThat(vaultGroup.getPolicies()).contains(vaultCredentialPolicyName);
            // Assert that user 1 can access the stored credential
            var secrets = vaultClientUser1.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).getSecretsOfPathOrThrow(credentialVaultKvPath);
            assertThat(secrets.getData().get("username")).isEqualTo("testuser");
            assertThat(secrets.getData().get("password")).isEqualTo("testpass");
            // Assert that user 2 cannot access the stored credential
            assertThatThrownBy(() -> {
                vaultClientUser2.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).getSecretsOfPathOrThrow(credentialVaultKvPath);

            }).isInstanceOf(VaultPermissionDeniedException.class);
        }

        @Test
        void shouldStoreCredentialKeyPair() {
            // Arrange
            var credentialId = UUID.randomUUID();
            var credentialVaultKvPath = credentialId.toString();
            var cred = new Credential(credentialId, "", TEST_CREDENTIAL_SCOPES, new CredentialDataKeyPair("privkey", "pubkey"));
            // Act
            vaultCredentialRepository.saveCredential(cred, KeycloakTestContainer.TEST_USER1_GROUP_ID);
            // Assert that user 1 group has the correct policy assigned to access the credential
            var vaultCredentialPolicyName = VaultCredentialRepository.getCredentialPolicyNameById(credentialId);
            var vaultGroup = vaultAdminClient.acl().getGroupByName(KeycloakTestContainer.TEST_USER1_GROUP_ID);
            assertThat(vaultGroup.getPolicies()).contains(vaultCredentialPolicyName);
            // Assert that user 1 can access the stored credential
            var secrets = vaultAdminClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).getSecretsOfPathOrThrow(credentialVaultKvPath);
            assertThat(secrets.getData().get("privateKey")).isEqualTo("privkey");
            assertThat(secrets.getData().get("publicKey")).isEqualTo("pubkey");
            // Assert that user 2 cannot access the stored credential
            assertThatThrownBy(() -> {
                vaultClientUser2.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).getSecretsOfPathOrThrow(credentialVaultKvPath);

            }).isInstanceOf(VaultPermissionDeniedException.class);

        }
    }

    @Nested
    class DeleteCredential {
        @Test
        void shouldDeleteCredentialUsernamePassword() {
            // Arrange
            var credentialId = UUID.randomUUID();
            var cred = new Credential(credentialId, "", TEST_CREDENTIAL_SCOPES, new CredentialDataUsernamePassword("testuser", "testpass"));
            vaultCredentialRepository.saveCredential(cred, KeycloakTestContainer.TEST_USER1_GROUP_ID);
            // Act
            vaultCredentialRepository.deleteCredential(cred.getId());
            // Assert
            var secretsOptional = vaultAdminClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).getSecretsOfPath(credentialId.toString());
            assertThat(secretsOptional).isEmpty();
            var vaultCredentialPolicyName = VaultCredentialRepository.getCredentialPolicyNameById(credentialId);
            var vaultGroup = vaultAdminClient.acl().getGroupByName(KeycloakTestContainer.TEST_USER1_GROUP_ID);
            assertThat(vaultGroup.getPolicies()).doesNotContain(vaultCredentialPolicyName);
        }
    }

}

