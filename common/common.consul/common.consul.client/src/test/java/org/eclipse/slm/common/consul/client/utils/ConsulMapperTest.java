package org.eclipse.slm.common.consul.client.utils;

import org.eclipse.slm.common.consul.model.acl.Policy;
import org.eclipse.slm.common.consul.model.acl.PolicyCreateRequest;
import org.eclipse.slm.common.consul.model.acl.PolicyUpdateRequest;
import org.eclipse.slm.common.consul.model.acl.Role;
import org.eclipse.slm.common.consul.model.acl.RoleUpdateRequest;
import org.eclipse.slm.common.consul.model.catalog.CatalogRegistration;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConsulMapperTest {

    private final ConsulMapper mapper = ConsulMapper.INSTANCE;

    @Nested
    @DisplayName("toCatalogRegistration")
    class ToCatalogRegistration {
        @Test
        @DisplayName("should map Node to CatalogRegistration and copy meta")
        void shouldMapNodeAndMeta() {
            // Arrange
            UUID nodeId = UUID.randomUUID();
            Node node = new Node(nodeId, "node-1");
            node.setAddress("127.0.0.1");
            node.setDatacenter("dc1");
            node.setMeta(Map.of("k", "v"));

            // Act
            CatalogRegistration registration = mapper.toCatalogRegistration(node);

            // Assert
            assertThat(registration.getId()).isEqualTo(nodeId.toString());
            assertThat(registration.getNodeName()).isEqualTo("node-1");
            assertThat(registration.getAddress()).isEqualTo("127.0.0.1");
            assertThat(registration.getDatacenter()).isEqualTo("dc1");
            assertThat(registration.getNodeMeta()).containsEntry("k", "v");
        }
    }

    @Nested
    @DisplayName("toCreateRequest")
    class ToCreateRequest {
        @Test
        @DisplayName("should map Policy to PolicyCreateRequest")
        void shouldMapPolicyToCreateRequest() {
            // Arrange
            Policy policy = Policy.builder("policy-name")
                .description("desc")
                .rules("rules")
                .datacenters(List.of("dc1", "dc2"))
                .build();

            // Act
            PolicyCreateRequest request = mapper.toCreateRequest(policy);

            // Assert
            assertThat(request.getName()).isEqualTo("policy-name");
            assertThat(request.getDescription()).isEqualTo("desc");
            assertThat(request.getRules()).isEqualTo("rules");
            assertThat(request.getDatacenters()).containsExactly("dc1", "dc2");
        }
    }

    @Nested
    @DisplayName("toUpdateRequest (Policy)")
    class ToUpdateRequestPolicy {
        @Test
        @DisplayName("should map Policy to PolicyUpdateRequest")
        void shouldMapPolicyToUpdateRequest() {
            // Arrange
            Policy policy = Policy.builder("policy-name")
                .id("id-1")
                .description("desc")
                .rules("rules")
                .datacenters(List.of("dc1"))
                .build();

            // Act
            PolicyUpdateRequest request = mapper.tuUpdateRequest(policy);

            // Assert
            assertThat(request.getId()).isEqualTo("id-1");
            assertThat(request.getName()).isEqualTo("policy-name");
            assertThat(request.getDescription()).isEqualTo("desc");
            assertThat(request.getRules()).isEqualTo("rules");
            assertThat(request.getDatacenters()).containsExactly("dc1");
        }
    }

    @Nested
    @DisplayName("toUpdateRequest (Role)")
    class ToUpdateRequestRole {
        @Test
        @DisplayName("should map Role to RoleUpdateRequest")
        void shouldMapRoleToUpdateRequest() {
            // Arrange
            Role role = new Role("id-1", "role-name", "desc", List.of());

            // Act
            RoleUpdateRequest request = mapper.toUpdateRequest(role);

            // Assert
            assertThat(request.getId()).isEqualTo("id-1");
            assertThat(request.getName()).isEqualTo("role-name");
            assertThat(request.getDescription()).isEqualTo("desc");
            assertThat(request.getPolicies()).isEmpty();
        }
    }
}

