package org.eclipse.slm.resource_management.common.remote_access;

import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.consul.testing.containers.ConsulTestContainer;
import org.eclipse.slm.common.consul.testing.utils.ConsulTestClientFactory;
import org.eclipse.slm.common.consul.testing.utils.ConsulTestContainerInitializer;
import org.eclipse.slm.common.credentials.model.CredentialDataUsernamePasswordReadDTO;
import org.eclipse.slm.common.credentials.persistence.VaultCredentialRepository;
import org.eclipse.slm.common.keycloak.testing.KeycloakTestContainer;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.common.vault.model.acl.GroupType;
import org.eclipse.slm.common.vault.testing.VaultTestContainer;
import org.eclipse.slm.resource_management.common.adapters.RemoteAccessConsulClientFactory;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.credentials.ResourceCredentialReadDTO;
import org.eclipse.slm.resource_management.common.credentials.ResourceCredentialScope;
import org.eclipse.slm.resource_management.common.credentials.ResourceCredentialsManager;
import org.eclipse.slm.resource_management.common.resources.BasicResource;
import org.junit.jupiter.api.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class RemoteAccessManagerImplIT {

    @Container
    static ConsulTestContainer consulContainer = new ConsulTestContainer();

    @Container
    static VaultTestContainer vaultContainer = new VaultTestContainer();

    @Container
    static KeycloakTestContainer keycloakContainer = new KeycloakTestContainer();

    static ResourcesConsulClient resourcesConsulAdminClient;

    private static ResourceCredentialsManager resourceCredentialsManager;
    private static RemoteAccessManager remoteAccessManager;

    private static final UUID TEST_RESOURCE_ID = UUID.randomUUID();
    private static JwtAuthenticationToken jwtAuthenticationToken;

    @BeforeAll
    static void setUp() throws ConsulLoginFailedException {
        var consulClientFactory = ConsulTestClientFactory.getConsulClientFactory(consulContainer);
        var remoteAccessConsulClientFactory = new RemoteAccessConsulClientFactory(consulClientFactory);
        resourcesConsulAdminClient = new ResourcesConsulClient(consulClientFactory.createAdminClient());

        var vaultAdminClient = vaultContainer.getVaultAdminClient();

        resourceCredentialsManager = mock(ResourceCredentialsManager.class);
        remoteAccessManager = new RemoteAccessManagerImpl(remoteAccessConsulClientFactory, resourceCredentialsManager);

        // Create KV secret engine for testing
        vaultAdminClient.kv(VaultCredentialRepository.VAULT_SECRET_ENGINE_NAME).createKvSecretEngine();
        // Add policy and group for testing
        vaultAdminClient.acl().createOrUpdatePolicy(KeycloakTestContainer.TEST_USER1_GROUP_ID, "path \"dummy-test-path\" { capabilities = [\"create\", \"read\", \"list\", \"update\"] }");
        vaultAdminClient.acl().createOrUpdateGroup(KeycloakTestContainer.TEST_USER1_GROUP_ID, GroupType.EXTERNAL, List.of(KeycloakTestContainer.TEST_USER1_GROUP_ID));

        var consulTestInitializer = new ConsulTestContainerInitializer(consulContainer, true);
        consulTestInitializer.initKeycloakJwtAuth(keycloakContainer.getTestRealmIssuerUri());
        consulTestInitializer.initUserGroup(KeycloakTestContainer.TEST_USER1_GROUP_ID);

        // Create JWT authentication token for tests
        var accessToken = keycloakContainer.getTestRealmAccessTokenForUser1();
        var jwt = new Jwt(accessToken, null, null, Map.of("dummy-header", "dummy-header-value"), Map.of("dummy-header", "dummy-header-value"));
        jwtAuthenticationToken = new JwtAuthenticationToken(jwt, new ArrayList<>(), KeycloakTestContainer.TEST_USER1_USERNAME);

        // Create node for test resource
        var basicResource = new BasicResource(TEST_RESOURCE_ID, "test-hostname", "1.2.3.4");
        resourcesConsulAdminClient.addResource(basicResource, KeycloakTestContainer.TEST_USER1_GROUP_ID);
    }

    protected ResourceCredentialReadDTO getResourceCredential(UUID credentialId) {
        return new ResourceCredentialReadDTO(credentialId,
                "test-credential",
                List.of(ResourceCredentialScope.REMOTE_ACCESS.toString()),
                new CredentialDataUsernamePasswordReadDTO("testuser"));
    }

    @Nested
    @Order(10)
    class CreateRemoteAccessTests {
        @Test
        void shouldCreateRemoteAccessSuccessfully() {
            // Arrange
            var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
            var username = "testuser";
            var credentialId = UUID.randomUUID();
            var testResourceCredential = getResourceCredential(credentialId);
            doReturn(testResourceCredential).when(resourceCredentialsManager).getCredentialByIdForUser(credentialId, accessToken);
            var remoteAccessCreateDTO = new RemoteAccessCreateDTO(KeycloakTestContainer.TEST_USER1_GROUP_ID, credentialId, username, 22, ConnectionType.ssh);

            // Act
            var remoteAccessDTO = remoteAccessManager.addRemoteAccessForResource(RemoteAccessManagerImplIT.TEST_RESOURCE_ID, remoteAccessCreateDTO, accessToken);

            // Assert
            assertThat(remoteAccessDTO).isNotNull();
            assertThat(remoteAccessDTO.getConnectionPort()).isEqualTo(22);
            assertThat(remoteAccessDTO.getConnectionType()).isEqualTo(ConnectionType.ssh);
            assertThat(remoteAccessDTO.getCredential().getId()).isEqualTo(credentialId);
        }

        @Test
        void shouldFailToCreateRemoteAccessWithInvalidData() {
            // Arrange
            var remoteAccessCreateDTO = new RemoteAccessCreateDTO(
                    "",
                    UUID.randomUUID(),
                    "dummy-user",
                    -1,
                    ConnectionType.ssh
            );
            var resourceId = UUID.randomUUID();
            var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);

            // Act & Assert
            assertThatThrownBy(() ->
                remoteAccessManager.addRemoteAccessForResource(resourceId, remoteAccessCreateDTO, accessToken)
            ).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @Order(20)
    class GetRemoteAccessByIdTests {
        @Test
        void shouldReturnRemoteAccessById() {
            // Arrange
            var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
            var credentialId = UUID.randomUUID();
            var testResourceCredential = getResourceCredential(credentialId);
            doReturn(testResourceCredential).when(resourceCredentialsManager).getCredentialByIdForUser(credentialId, accessToken);
            var remoteAccessCreateDTO = new RemoteAccessCreateDTO(
                    KeycloakTestContainer.TEST_USER1_GROUP_ID,
                    credentialId,
                    "dummy-user",
                    2222,
                    ConnectionType.ssh
            );
            var created = remoteAccessManager.addRemoteAccessForResource(RemoteAccessManagerImplIT.TEST_RESOURCE_ID, remoteAccessCreateDTO, accessToken);

            // Act
            var found = remoteAccessManager.getRemoteAccessByIdOrThrow(RemoteAccessManagerImplIT.TEST_RESOURCE_ID, created.getId(), accessToken);

            // Assert
            assertThat(found).isNotNull();
            assertThat(found.getId()).isEqualTo(created.getId());
            assertThat(found.getConnectionPort()).isEqualTo(2222);
            assertThat(found.getConnectionType()).isEqualTo(org.eclipse.slm.resource_management.common.remote_access.ConnectionType.ssh);
            assertThat(found.getCredential().getId()).isEqualTo(credentialId);
        }

        @Test
        void shouldThrowExceptionIfRemoteAccessNotFound() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);

            // Act & Assert
            assertThatThrownBy(() -> remoteAccessManager.getRemoteAccessByIdOrThrow(TEST_RESOURCE_ID, nonExistentId, accessToken))
                .isInstanceOf(RemoteAccessRuntimeException.class);
        }
    }

    @Nested
    @Order(30)
    class ListRemoteAccessTests {
        @Test
        void shouldReturnAllRemoteAccesses() {
            // Arrange
            var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
            var credentialId1 = UUID.randomUUID();
            var credentialId2 = UUID.randomUUID();
            var testResourceCredential1 = getResourceCredential(credentialId1);
            var testResourceCredential2 = getResourceCredential(credentialId2);
            doReturn(testResourceCredential1).when(resourceCredentialsManager).getCredentialByIdForUser(credentialId1, accessToken);
            doReturn(testResourceCredential2).when(resourceCredentialsManager).getCredentialByIdForUser(credentialId2, accessToken);
            var remoteAccessCreateDTO1 = new RemoteAccessCreateDTO(
                    KeycloakTestContainer.TEST_USER1_GROUP_ID,
                    credentialId1,
                    "dummy-user",
                    2224,
                    ConnectionType.ssh
            );
            var remoteAccessCreateDTO2 = new RemoteAccessCreateDTO(
                    KeycloakTestContainer.TEST_USER1_GROUP_ID,
                    credentialId2,
                    "dummy-user",
                    2225,
                    ConnectionType.ssh
            );
            var created1 = remoteAccessManager.addRemoteAccessForResource(RemoteAccessManagerImplIT.TEST_RESOURCE_ID, remoteAccessCreateDTO1, accessToken);
            var created2 = remoteAccessManager.addRemoteAccessForResource(RemoteAccessManagerImplIT.TEST_RESOURCE_ID, remoteAccessCreateDTO2, accessToken);

            // Act
            var allRemoteAccesses = remoteAccessManager.getRemoteAccessesOfResource(RemoteAccessManagerImplIT.TEST_RESOURCE_ID, accessToken);

            // Assert
            assertThat(allRemoteAccesses)
                    .extracting("id")
                    .contains(created1.getId(), created2.getId());
            assertThat(allRemoteAccesses)
                    .filteredOn(r -> r.getId().equals(created1.getId()))
                    .first()
                    .satisfies(r -> {
                        assertThat(r.getConnectionPort()).isEqualTo(2224);
                        assertThat(r.getConnectionType()).isEqualTo(org.eclipse.slm.resource_management.common.remote_access.ConnectionType.ssh);
                        assertThat(r.getCredentialId()).isEqualTo(credentialId1);
                    });
            assertThat(allRemoteAccesses)
                    .filteredOn(r -> r.getId().equals(created2.getId()))
                    .first()
                    .satisfies(r -> {
                        assertThat(r.getConnectionPort()).isEqualTo(2225);
                        assertThat(r.getConnectionType()).isEqualTo(org.eclipse.slm.resource_management.common.remote_access.ConnectionType.ssh);
                        assertThat(r.getCredentialId()).isEqualTo(credentialId2);
                    });
        }
    }

    @Nested
    @Order(40)
    class DeleteRemoteAccessTests {
        @Test
        void shouldDeleteRemoteAccessSuccessfully() {
            // Arrange
            var accessToken = KeycloakTokenUtil.getToken(jwtAuthenticationToken);
            var credentialId = UUID.randomUUID();
            var testResourceCredential = getResourceCredential(credentialId);
            doReturn(testResourceCredential).when(resourceCredentialsManager).getCredentialByIdForUser(credentialId, accessToken);
            var remoteAccessCreateDTO = new RemoteAccessCreateDTO(
                    KeycloakTestContainer.TEST_USER1_GROUP_ID,
                    credentialId,
                    "dummy-user",
                    +
                    2223,
                    ConnectionType.ssh
            );
            var created = remoteAccessManager.addRemoteAccessForResource(RemoteAccessManagerImplIT.TEST_RESOURCE_ID, remoteAccessCreateDTO, accessToken);

            // Act
            remoteAccessManager.deleteRemoteAccessById(RemoteAccessManagerImplIT.TEST_RESOURCE_ID, created.getId(), accessToken, false);

            // Assert
            assertThatThrownBy(() -> remoteAccessManager.getRemoteAccessByIdOrThrow(TEST_RESOURCE_ID, created.getId(), accessToken))
                    .isInstanceOf(RemoteAccessRuntimeException.class);
        }
    }
}
