package org.eclipse.slm.resource_management.common.adapters;

import org.eclipse.slm.common.consul.client.ConsulClient;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulPolicyNotFoundException;
import org.eclipse.slm.common.consul.testing.containers.ConsulTestContainer;
import org.eclipse.slm.common.consul.testing.utils.ConsulTestClientFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.eclipse.slm.resource_management.common.remote_access.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class RemoteAccessConsulClientTest {

    @Container
    private final static ConsulTestContainer consulContainer = new ConsulTestContainer();
    private static ConsulClient adminConsulClient;

    private static RemoteAccessConsulClient remoteAccessConsulAdminClient;

    private static final String TEST_USER_GROUP_ID = "/users/" + UUID.randomUUID();
    private static final UUID TEST_RESOURCE_ID = UUID.randomUUID();
    private static RemoteAccessDTOReadMinimal testRemoteAccess;

    @BeforeAll
    public static void setUp() throws ConsulLoginFailedException {
        var consulClientFactory = ConsulTestClientFactory.getConsulClientFactory(consulContainer);
        adminConsulClient = consulClientFactory.createAdminClient();
        var remoteAccessConsulClientFactory = new RemoteAccessConsulClientFactory(consulClientFactory);
        remoteAccessConsulAdminClient = remoteAccessConsulClientFactory.createAdminClient();

        // Create test resource node
        var node = Node.builder(RemoteAccessConsulClientTest.TEST_RESOURCE_ID.toString())
                        .id(RemoteAccessConsulClientTest.TEST_RESOURCE_ID)
                        .address("test-resource-address.local")
                    .build();
        adminConsulClient.nodes().registerNode(node);
        // Create user group role
        adminConsulClient.acl().createRole(
                RemoteAccessConsulClientTest.TEST_USER_GROUP_ID,
                "Role for user group " + RemoteAccessConsulClientTest.TEST_USER_GROUP_ID,
                List.of()
        );
    }

    @Nested
    @Order(10)
    public class AddRemoteAccess {
        @Test
        public void shouldAddRemoteAccess() throws Exception {
            // Arrange
            var credentialId = UUID.randomUUID();
            var connectionType = ConnectionType.ssh;
            var createDTO = new RemoteAccessCreateDTO(RemoteAccessConsulClientTest.TEST_USER_GROUP_ID, credentialId, "testuser", 22, connectionType);
            // Act
            var remoteAccessCreated = remoteAccessConsulAdminClient.addRemoteAccess(createDTO, RemoteAccessConsulClientTest.TEST_RESOURCE_ID);
            testRemoteAccess = remoteAccessCreated;
            // Assert
            var consulRemoteAccessNodeServices = adminConsulClient.services().getNodeServicesByNodeIdAndServiceTag(
                    RemoteAccessConsulClientTest.TEST_RESOURCE_ID,
                    RemoteAccessConsulService.class.getSimpleName()
            );
            assertThat(consulRemoteAccessNodeServices)
                .filteredOn(nodeService -> nodeService.getId().equals(remoteAccessCreated.getId().toString()))
                .anySatisfy(nodeService -> {
                    assertThat(nodeService.getPort()).isEqualTo(22);
                    assertThat(nodeService.getMeta().get(RemoteAccessConsulService.CONNECTION_TYPE_META_DATA_KEY)).isEqualTo(connectionType.toString());
                    assertThat(nodeService.getMeta().get(RemoteAccessConsulService.CREDENTIAL_ID_META_DATA_KEY)).isEqualTo(credentialId.toString());
                    assertThat(nodeService.getTags()).contains(RemoteAccessConsulService.class.getSimpleName());
                });
            var remoteAccessConsulServiceName = RemoteAccessConsulService.convertIdToServiceName(testRemoteAccess.getId(), testRemoteAccess.getConnectionType());
            var userGroupPolicy = adminConsulClient.acl()
                    .getPolicyByNameOrThrow(RemoteAccessConsulClient.getRemoteAccessServicePolicyName(testRemoteAccess.getId()));
            assertThat(userGroupPolicy).isNotNull();
            assertThat(userGroupPolicy.getRules()).contains("service \"" + remoteAccessConsulServiceName +  "\" { policy = \"read\" }");
        }
    }

    @Nested
    @Order(20)
    public class GetRemoteAccesses {
        @Test
        public void shouldReturnOne() {
            List<RemoteAccessDTOReadMinimal> result = remoteAccessConsulAdminClient.getRemoteAccesses(RemoteAccessConsulClientTest.TEST_RESOURCE_ID);
            assertThat(result).hasSize(1);
        }

        @Test
        public void shouldReturnEmptyListIfNoneFound() {
            // Create new node without remote access services
            var nodeId = UUID.randomUUID();
            var catalogNode = Node.builder(nodeId.toString())
                    .id(nodeId)
                    .address("random-test-resource-address.local")
                .build();
            adminConsulClient.nodes().registerNode(catalogNode);

            List<RemoteAccessDTOReadMinimal> result = remoteAccessConsulAdminClient.getRemoteAccesses(nodeId);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @Order(21)
    public class GetRemoteAccessById {
        @Test
        public void shouldReturnExisting() {

            Optional<RemoteAccessDTOReadMinimal> result = remoteAccessConsulAdminClient.getRemoteAccessById(RemoteAccessConsulClientTest.TEST_RESOURCE_ID, testRemoteAccess.getId());
            assertThat(result)
                .isPresent()
                .get().satisfies(ra -> {
                    assertThat(ra.getId()).isEqualTo(testRemoteAccess.getId());
                    assertThat(ra.getConnectionPort()).isEqualTo(testRemoteAccess.getConnectionPort());
                    assertThat(ra.getCredentialId()).isEqualTo(testRemoteAccess.getCredentialId());
                    assertThat(ra.getConnectionType()).isEqualTo(testRemoteAccess.getConnectionType());
                });
        }

        @Test
        public void shouldReturnEmptyIfNotFound() {
            var remoteAccessId = UUID.randomUUID();

            Optional<RemoteAccessDTOReadMinimal> result = remoteAccessConsulAdminClient.getRemoteAccessById(RemoteAccessConsulClientTest.TEST_RESOURCE_ID, remoteAccessId);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @Order(30)
    public class RemoveRemoteAccess {
        @Test
        public void shouldRemoveExisting() {
            // Act
            remoteAccessConsulAdminClient.removeRemoteAccess(RemoteAccessConsulClientTest.TEST_RESOURCE_ID, testRemoteAccess.getId());
            // Assert | Check that no remote access services exist anymore for the resource node
            var consulRemoteAccessNodeServices = adminConsulClient.services().getNodeServicesByNodeIdAndServiceTag(
                    RemoteAccessConsulClientTest.TEST_RESOURCE_ID,
                    RemoteAccessConsulService.class.getSimpleName()
            );
            assertThat(consulRemoteAccessNodeServices).hasSize(0);
            // Assert | Check that remote access policy is removed
            var remoteAccessPolicyId = RemoteAccessConsulClient.getRemoteAccessServicePolicyName(testRemoteAccess.getId());
            assertThatThrownBy(() -> adminConsulClient.acl().getPolicyByNameOrThrow(remoteAccessPolicyId))
                    .isInstanceOf(ConsulPolicyNotFoundException.class);
        }

        @Test
        public void shouldThrowIfNotFound() {
            UUID remoteAccessId = UUID.randomUUID();

            assertThatThrownBy(() -> remoteAccessConsulAdminClient.removeRemoteAccess(RemoteAccessConsulClientTest.TEST_RESOURCE_ID, remoteAccessId))
                    .isInstanceOf(RemoteAccessNotFoundException.class)
                    .hasMessageContaining(remoteAccessId.toString());
        }
    }
}
