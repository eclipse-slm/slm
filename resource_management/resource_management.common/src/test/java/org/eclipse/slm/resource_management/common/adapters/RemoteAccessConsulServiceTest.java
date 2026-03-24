package org.eclipse.slm.resource_management.common.adapters;

import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.resource_management.common.remote_access.ConnectionType;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessCreateDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteAccessConsulServiceTest {

    @Nested
    @DisplayName("constructor")
    class Constructor {
        @Test
        @DisplayName("should initialize fields from RemoteAccessCreateDTO")
        void shouldInitializeFieldsFromDTO() {
            // Arrange
            String ownerId = "/user/123";
            int port = 2222;
            var connectionType = ConnectionType.ssh;
            var credentialId = UUID.randomUUID();
            var username = "user";
            RemoteAccessCreateDTO createDTO = new RemoteAccessCreateDTO(ownerId, credentialId, username, port, connectionType);

            // Act
            RemoteAccessConsulService service = new RemoteAccessConsulService(UUID.randomUUID(), createDTO);

            // Assert
            assertThat(service.getConnectionType()).isEqualTo(connectionType);
            assertThat(service.getCredentialId()).isEqualTo(credentialId);
            assertThat(service.getPort()).isEqualTo(port);
            assertThat(service.getUsername()).isEqualTo(username);
        }
    }

    @Nested
    @DisplayName("serviceMeta")
    class ServiceMeta {
        @Test
        @DisplayName("should contain all required meta fields")
        void shouldContainAllRequiredMetaFields() {
            // Arrange
            String ownerId = "/user/456";
            int port = 2223;
            var connectionType = ConnectionType.ssh;
            var credentialId = UUID.randomUUID();
            var username = "user2";
            RemoteAccessCreateDTO createDTO = new RemoteAccessCreateDTO(ownerId, credentialId, username, port, connectionType);
            RemoteAccessConsulService service = new RemoteAccessConsulService(UUID.randomUUID(), createDTO);

            // Act
            var meta = service.getMeta();

            // Assert
            assertThat(meta).containsEntry(RemoteAccessConsulService.CONNECTION_TYPE_META_DATA_KEY, connectionType.name());
            assertThat(meta).containsEntry(RemoteAccessConsulService.CREDENTIAL_ID_META_DATA_KEY, credentialId.toString());
            assertThat(meta).containsEntry(RemoteAccessConsulService.USERNAME_META_DATA_KEY, username);
        }
    }

    @Nested
    @DisplayName("createFromNodeService")
    class CreateFromNodeService {
        @Test
        @DisplayName("should create RemoteAccessConsulService from NodeService meta")
        void shouldCreateFromNodeService() {
            // Arrange
            String ownerId = "/user/789";
            ConnectionType connectionType = ConnectionType.ssh;
            UUID credentialId = UUID.randomUUID();
            int port = 2224;
            var serviceId = UUID.randomUUID();

            Map<String, String> meta = new HashMap<>();
            meta.put(RemoteAccessConsulService.CONNECTION_TYPE_META_DATA_KEY, connectionType.name());
            meta.put(RemoteAccessConsulService.CREDENTIAL_ID_META_DATA_KEY, credentialId.toString());

            var nodeService = new NodeService(
                    serviceId.toString(),
                    RemoteAccessConsulService.convertIdToServiceName(serviceId, connectionType),
                    null,
                    List.of(),
                    meta,
                    port,
                    null);
            // Act
            RemoteAccessConsulService service = RemoteAccessConsulService.createFromNodeService(nodeService);

            // Assert
            assertThat(service.getConnectionType()).isEqualTo(connectionType);
            assertThat(service.getCredentialId()).isEqualTo(credentialId);
            assertThat(service.getPort()).isEqualTo(port);
            assertThat(service.getId()).isEqualTo(serviceId.toString());
        }
    }
}
