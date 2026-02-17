package org.eclipse.slm.common.vault.client;

import org.eclipse.slm.common.restclient.feign.FeignClientFactory;
import org.eclipse.slm.common.restclient.feign.auth.BearerTokenAuthRequestInterceptor;
import org.eclipse.slm.common.vault.client.apiclients.VaultApiClientSys;
import org.eclipse.slm.common.vault.client.exceptions.VaultKvSecretsNotFoundException;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class VaultClientKvTest {

    @Container
    private static final VaultTestContainer vaultContainer = new VaultTestContainer();

    private VaultClient vaultAdminClient;

    private final String secretsEngineName = "test-kv";

    private static VaultApiClientSys vaultApiClientSys;

    private static final String testSecretsPath = "test/path";
    private static final Map<String, String> testSecrets = Map.of(
            "username", "testuser",
            "password", "testpassword"
    );

    @BeforeAll
    void setUp() {
        vaultAdminClient = vaultContainer.getVaultClient();
        vaultApiClientSys = FeignClientFactory.createClient(
                VaultApiClientSys.class,
                vaultContainer.getVaultUrl(),
                new BearerTokenAuthRequestInterceptor(vaultContainer.getRootToken()));
    }

    @Nested
    @Order(10)
    class CreateKvSecretEngine {
        @Test
        void shouldCreateKvSecretEngineSuccessfully() {
            // Act
            vaultAdminClient.kv(secretsEngineName).createKvSecretEngine();
            // Assert
            var mountsResponse = vaultApiClientSys.getMounts();
            assertThat(mountsResponse.getData())
                .containsKey(secretsEngineName + "/")
                .extractingByKey(secretsEngineName + "/")
                .extracting("type")
                .isEqualTo("kv");
        }

        @Test
        void shouldNotFailWhenSecretEngineAlreadyExists() {
            // Arrange
            var randomSecretEngineName = "existing-kv-" + UUID.randomUUID();
            vaultAdminClient.kv(randomSecretEngineName).createKvSecretEngine();
            // Act + Assert
            assertThatCode(() -> vaultAdminClient.kv(randomSecretEngineName).createKvSecretEngine())
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @Order(20)
    class AddSecretsToKvEngine {
        @Test
        void shouldAddSecretsSuccessfully() {
            // Act
            vaultAdminClient.kv(secretsEngineName).addSecretsToKvEngine(testSecretsPath, testSecrets);
            // Assert
            var secrets = vaultAdminClient.kv(secretsEngineName).getSecretsOfPathOrThrow(testSecretsPath);
            assertThat(secrets.getData()).isNotEmpty();
        }
    }

    @Order(30)
    @Nested
    class GetSecretKeysOfPath {
        @Test
        void shouldReturnKeysList() {
            // Act
            var secretKeys = vaultAdminClient.kv(secretsEngineName).getSecretKeysOfPath(testSecretsPath);
            // Assert
            assertThat(secretKeys.getData().keySet()).containsAll(testSecrets.keySet());
        }

        @Test
        void shouldThrowVaultKvSecretsNotFoundExceptionForNonExistingSecretPath() {
            // Act & Assert
            assertThatThrownBy(() -> {
                vaultAdminClient.kv(secretsEngineName).getSecretKeysOfPath("non/existing/path");
            }).isInstanceOf(VaultKvSecretsNotFoundException.class);
        }
    }

    @Nested
    @Order(31)
    class GetKvContent {
        @Test
        void shouldReturnSecretsMap() {
            // Act
            var secrets = vaultAdminClient.kv(secretsEngineName).getSecretsOfPathOrThrow(testSecretsPath);
            // Assert
            assertThat(secrets.getData()).containsAllEntriesOf(testSecrets);
        }

        @Test
        void shouldThrowVaultKvSecretsNotFoundExceptionForNonExistingSecretPath() {
            // Act & Assert
            assertThatThrownBy(() -> {
                vaultAdminClient.kv(secretsEngineName).getSecretsOfPathOrThrow("non/existing/path");
            }).isInstanceOf(VaultKvSecretsNotFoundException.class);
        }
    }

    @Nested
    @Order(32)
    class GetSecretKeysOfPathRecursive {
        @Test
        void shouldReturnAllKeysRecursively() {
            // Arrange
            var nestedSecretTopLevel = "my";
            var nestedSecretSecondLevel = nestedSecretTopLevel + "/nested";
            var nestedSecretPath1 = nestedSecretSecondLevel + "/secret1";
            var nestedSecretPath2 = nestedSecretSecondLevel + "/secret2";
            vaultAdminClient.kv(secretsEngineName).addSecretsToKvEngine(nestedSecretPath1, testSecrets);
            vaultAdminClient.kv(secretsEngineName).addSecretsToKvEngine(nestedSecretPath2, testSecrets);

            var resultTopLevel = vaultAdminClient.kv(secretsEngineName).listSecretKeysOfPath(nestedSecretTopLevel);
            assertThat(resultTopLevel).containsExactly("nested/");
            var resultSecondLevel = vaultAdminClient.kv(secretsEngineName).listSecretKeysOfPath(nestedSecretSecondLevel);
            assertThat(resultSecondLevel).containsExactly("secret1", "secret2");
        }
    }

    @Nested
    @Order(33)
    class GetSecretSubkeys {
        @Test
        void shouldReturnSubkeysForExistingSecret() {
            // Arrange
            var secretPath = "my/secret1";
            vaultAdminClient.kv(secretsEngineName).addSecretsToKvEngine(secretPath, testSecrets);
            // Act
            var subkeys = vaultAdminClient.kv(secretsEngineName).getSecretKeysOfPath(secretPath);
            // Assert
            assertThat(subkeys.getData().keySet()).contains("username", "password");
        }

        @Test
        void shouldReturnEmptyForNonExistingPath() {
            // Act & Assert
            assertThatThrownBy(() -> {
                vaultAdminClient.kv(secretsEngineName).getSecretKeysOfPath("does/not/exist");
            }).isInstanceOf(VaultKvSecretsNotFoundException.class);
        }
    }

    @Nested
    @Order(40)
    class AddMetadataToKvEngine {
        @Test
        void shouldAddMetadataSuccessfully() {
            // Arrange
            Map<String, String> metadata = Map.of(
                    "owner", "unit-test",
                    "environment", "test"
            );
            // Act
            vaultAdminClient.kv(secretsEngineName).addMetadataToKvEngine(testSecretsPath, metadata);
            // Assert
            var secrets = vaultAdminClient.kv(secretsEngineName).getSecretsOfPathOrThrow(testSecretsPath);
            assertThat(secrets.getMetadata()).isNotNull();
            assertThat(secrets.getMetadata().getCustomMetadata()).containsAllEntriesOf(metadata);
        }
    }

    @Nested
    @Order(50)
    class DeleteSecretFromKvEngine {
        @Test
        void shouldDeleteSecretSuccessfully() {
            // Act
            vaultAdminClient.kv(secretsEngineName).deleteSecretFromKvEngine(testSecretsPath);
            // Assert
            assertThatThrownBy(() -> {
                vaultAdminClient.kv(secretsEngineName).getSecretsOfPathOrThrow(testSecretsPath);
            }).isInstanceOf(VaultKvSecretsNotFoundException.class);
        }

        @Test
        void shouldNotFailWhenDeletingNonExistingMetadata() {
            // Act & Assert
            assertThatCode(() -> vaultAdminClient.kv(secretsEngineName).deleteSecretFromKvEngine("does/not/exist"))
                    .doesNotThrowAnyException();
        }
    }


}
