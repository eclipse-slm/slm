package org.eclipse.slm.resource_management.common.adapters;

import org.eclipse.slm.common.consul.model.catalog.CatalogRegistration;
import org.eclipse.slm.resource_management.common.remote_access.ConnectionType;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessCreateDTO;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessDTOReadMinimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteAccessConsulMapperTest {

    @Nested
    @DisplayName("toDto")
    class ToDto {
        @Test
        @DisplayName("should map all fields correctly")
        void shouldMapAllFieldsCorrectly() {
            // Arrange
            var username = "user123";
            String ownerId = "/user/" + username;
            int port = 2222;
            ConnectionType connectionType = ConnectionType.ssh;
            var credentialId = UUID.randomUUID();
            var createDTO = new RemoteAccessCreateDTO(ownerId, credentialId, username, port, connectionType);
            UUID serviceId = UUID.randomUUID();
            var service = new RemoteAccessConsulService(serviceId, createDTO);

            // Act
            RemoteAccessDTOReadMinimal dto = RemoteAccessConsulMapper.INSTANCE.toDto(service);

            // Assert
            assertThat(dto.getId()).isEqualTo(serviceId);
            assertThat(dto.getConnectionPort()).isEqualTo(port);
            assertThat(dto.getCredentialId()).isEqualTo(credentialId);
            assertThat(dto.getConnectionType()).isEqualTo(connectionType);
        }
    }

    @Nested
    @DisplayName("toCatalogRegistrationService")
    class ToCatalogRegistrationService {
        @Test
        @DisplayName("should map service fields to CatalogRegistration.Service")
        void shouldMapServiceFields() {
            // Arrange
            var username = "user456";
            String ownerId = "/user/" + username;
            int port = 2200;
            ConnectionType connectionType = ConnectionType.ssh;
            var credentialId = UUID.randomUUID();
            var createDTO = new RemoteAccessCreateDTO(ownerId, credentialId, username, port, connectionType);
            var serviceId = UUID.randomUUID();
            var service = new RemoteAccessConsulService(serviceId, createDTO);

            // Act
            CatalogRegistration.Service catalogService = RemoteAccessConsulMapper.INSTANCE.toCatalogRegistrationService(service);

            // Assert
            assertThat(catalogService.getId()).isEqualTo(serviceId.toString());
            assertThat(catalogService.getServiceName()).isEqualTo(service.getServiceName());
            assertThat(catalogService.getPort()).isEqualTo(port);
            assertThat(catalogService.getTags()).containsExactlyInAnyOrderElementsOf(service.getTags());
            assertThat(catalogService.getMeta()).containsAllEntriesOf(service.getMeta());
        }
    }
}
