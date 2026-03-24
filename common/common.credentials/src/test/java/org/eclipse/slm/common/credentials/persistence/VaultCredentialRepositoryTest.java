package org.eclipse.slm.common.credentials.persistence;

import org.eclipse.slm.common.credentials.model.Credential;
import org.eclipse.slm.common.credentials.model.CredentialDataKeyPair;
import org.eclipse.slm.common.credentials.model.CredentialDataUsernamePassword;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.model.acl.GroupType;
import org.eclipse.slm.common.vault.client.exceptions.VaultPolicyNotFoundException;
import org.eclipse.slm.common.vault.testing.VaultTestContainer;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import org.eclipse.slm.common.credentials.exceptions.CredentialNotFoundException;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VaultCredentialRepositoryTest {

    @Container
    private static final VaultTestContainer vaultContainer = new VaultTestContainer();

    private static final String TEST_GROUP_ID = "/users/" + UUID.randomUUID();
    private VaultClient vaultAdminClient;
    private VaultCredentialRepository vaultCredentialRepository;

    private final List<String> TEST_CREDENTIAL_SCOPES = List.of("CRED_SCOPE_1");

    @BeforeAll
    void beforeAll() {
        vaultAdminClient = vaultContainer.getVaultAdminClient();
        // Create KV secret engine for testing
        vaultAdminClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).createKvSecretEngine();
        // Add user group for testing
        vaultAdminClient.acl().createOrUpdateGroup(TEST_GROUP_ID, GroupType.EXTERNAL, List.of());

        vaultCredentialRepository = new VaultCredentialRepository(vaultAdminClient);
    }

    @Nested
    class StoreCredential {
        @Test
        void shouldStoreCredentialUsernamePassword() {
            // Arrange
            var credentialId = UUID.randomUUID();
            var cred = new Credential(credentialId, "", TEST_CREDENTIAL_SCOPES, new CredentialDataUsernamePassword("testuser","testpass"));
            // Act
            vaultCredentialRepository.saveCredential(cred, VaultCredentialRepositoryTest.TEST_GROUP_ID);
            // Assert | Secrets stored correctly
            var secrets = vaultAdminClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).getSecretsOfPathOrThrow(credentialId.toString());
            assertThat(secrets.getData().get("username")).isEqualTo("testuser");
            assertThat(secrets.getData().get("password")).isEqualTo("testpass");
            // Assert | Policy created and assigned to group
            var vaultCredentialPolicyName = VaultCredentialRepository.getCredentialPolicyNameById(credentialId);
            var vaultCredentialPolicy = vaultAdminClient.acl().getPolicy(vaultCredentialPolicyName);
            assertThat(vaultCredentialPolicy).isNotNull();
            var vaultGroup = vaultAdminClient.acl().getGroupByName(VaultCredentialRepositoryTest.TEST_GROUP_ID);
            assertThat(vaultGroup.getPolicies()).contains(vaultCredentialPolicyName);
        }

        @Test
        void shouldStoreCredentialKeyPair() {
            // Arrange
            var credentialId = UUID.randomUUID();
            var cred = new Credential(credentialId, "", TEST_CREDENTIAL_SCOPES, new CredentialDataKeyPair("privkey", "pubkey"));
            // Act
            vaultCredentialRepository.saveCredential(cred, VaultCredentialRepositoryTest.TEST_GROUP_ID);
            // Assert | Secrets stored correctly
            var secrets = vaultAdminClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).getSecretsOfPathOrThrow(credentialId.toString());
            assertThat(secrets.getData().get("privateKey")).isEqualTo("privkey");
            assertThat(secrets.getData().get("publicKey")).isEqualTo("pubkey");
            // Assert | Policy created and assigned to group
            var vaultCredentialPolicyName = VaultCredentialRepository.getCredentialPolicyNameById(credentialId);
            var vaultCredentialPolicy = vaultAdminClient.acl().getPolicy(vaultCredentialPolicyName);
            assertThat(vaultCredentialPolicy).isNotNull();
            var vaultGroup = vaultAdminClient.acl().getGroupByName(VaultCredentialRepositoryTest.TEST_GROUP_ID);
            assertThat(vaultGroup.getPolicies()).contains(vaultCredentialPolicyName);
        }
    }

    @Nested
    class DeleteCredential {
        @Test
        void shouldDeleteCredentialUsernamePassword() {
            // Arrange
            var credentialId = UUID.randomUUID();
            var cred = new Credential(credentialId, "", TEST_CREDENTIAL_SCOPES, new CredentialDataUsernamePassword("testuser", "testpass"));
            vaultCredentialRepository.saveCredential(cred, VaultCredentialRepositoryTest.TEST_GROUP_ID);
            // Act
            vaultCredentialRepository.deleteCredential(cred.getId());
            // Assert | Secrets deleted
            var secretsOptional = vaultAdminClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).getSecretsOfPath(credentialId.toString());
            assertThat(secretsOptional).isEmpty();
            // Assert | Policy deleted and removed from group
            var vaultCredentialPolicyName = VaultCredentialRepository.getCredentialPolicyNameById(credentialId);
            assertThatThrownBy(() -> vaultAdminClient.acl().getPolicy(vaultCredentialPolicyName))
                    .isInstanceOf(VaultPolicyNotFoundException.class);
            var vaultGroup = vaultAdminClient.acl().getGroupByName(VaultCredentialRepositoryTest.TEST_GROUP_ID);
            assertThat(vaultGroup.getPolicies()).doesNotContain(vaultCredentialPolicyName);
        }
    }

    @Nested
    class FindCredentialOrThrow {
        @Test
        void returnsCredentialWhenPresent() {
            var credentialId = UUID.randomUUID();
            var cred = new Credential(credentialId, "", TEST_CREDENTIAL_SCOPES, new CredentialDataUsernamePassword("testuser", "testpass"));
            vaultCredentialRepository.saveCredential(cred, VaultCredentialRepositoryTest.TEST_GROUP_ID);

            var result = vaultCredentialRepository.findCredentialOrThrow(credentialId);

            assertThat(result.getId()).isEqualTo(credentialId);
            assertThat(result.getScopesRaw()).containsExactlyElementsOf(TEST_CREDENTIAL_SCOPES);
        }

        @Test
        void throwsWhenMissing() {
            var credentialId = UUID.randomUUID();

            assertThatThrownBy(() -> vaultCredentialRepository.findCredentialOrThrow(credentialId))
                    .isInstanceOf(CredentialNotFoundException.class);
        }
    }

    @Nested
    class UpdateCredentialScopes {
        @Test
        void updatesScopesWithoutAffectingOtherMetadata() {
            var credentialId = UUID.randomUUID();
            var cred = new Credential(credentialId, "", TEST_CREDENTIAL_SCOPES, new CredentialDataUsernamePassword("testuser", "testpass"));
            vaultCredentialRepository.saveCredential(cred, VaultCredentialRepositoryTest.TEST_GROUP_ID);

            var updatedScopes = List.of("CRED_SCOPE_1", "CRED_SCOPE_2");
            assertThatCode(() -> vaultCredentialRepository.updateCredentialScopes(credentialId, updatedScopes))
                    .doesNotThrowAnyException();

            var updated = vaultCredentialRepository.findCredentialOrThrow(credentialId);
            assertThat(updated.getScopesRaw()).containsExactlyInAnyOrderElementsOf(updatedScopes);
        }
    }

}
