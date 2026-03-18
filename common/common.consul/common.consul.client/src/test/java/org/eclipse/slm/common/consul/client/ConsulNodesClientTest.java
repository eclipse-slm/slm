package org.eclipse.slm.common.consul.client;

import org.eclipse.slm.common.consul.client.testutils.ConsulTestClientFactory;
import org.eclipse.slm.common.consul.model.catalog.*;
import org.eclipse.slm.common.consul.model.exceptions.ConsulNodeNotFoundException;
import org.eclipse.slm.common.consul.testing.containers.ConsulTestContainer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Testcontainers
public class ConsulNodesClientTest {
    private final static Logger LOG = LoggerFactory.getLogger(ConsulNodesClientTest.class);

    @Container
    private final static ConsulTestContainer consulContainer = new ConsulTestContainer();

    public static ConsulClient consulClient;
    public static ConsulNodesClient consulNodesClient;

    @BeforeAll
    public static void beforeAll() {
        consulClient = ConsulTestClientFactory.getConsulClient(consulContainer);
        consulNodesClient = consulClient.nodes();
    }

    @Nested
    @Order(10)
    class GetNodes {
        @Test
        void shouldReturnEmptyListInitially() {
            // Act
            List<Node> nodes = consulNodesClient.getNodes();
            // Assert
            // One node exists by default (the agent itself) --> Allow size between 0 and 1, because of timing issues, when agent node is not available
            // immediately after container startup, which leads to flaxy test execution
            assertThat(nodes).hasSizeBetween(0, 1);;
        }
    }

    @Nested
    @Order(20)
    class RegisterNode {
        @Test
        void shouldRegisterNodeSuccessfully() {
            // Arrange
            Node node = Node.builder("test-node")
                .address("127.0.0.1")
                .meta(new HashMap<>())
                .build();
            // Act
            consulNodesClient.registerNode(node);
            // Assert
            List<Node> nodes = consulNodesClient.getNodes();
            assertThat(nodes).extracting(Node::getNodeName).contains("test-node");
        }
    }

    @Nested
    @Order(30)
    class GetNodeById {
        @Test
        void shouldReturnNodeByIdIfExists() {
            // Arrange
            Node node = Node.builder("node-by-id")
                .address("127.0.0.2")
                .id(UUID.randomUUID())
                .build();
            consulNodesClient.registerNode(node);
            // Act
            Optional<Node> found = consulNodesClient.getNodeById(node.getId());
            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getNodeName()).isEqualTo("node-by-id");
        }

        @Test
        void shouldReturnEmptyIfNodeNotExists() {
            // Act
            Optional<Node> found = consulNodesClient.getNodeById(UUID.randomUUID());
            // Assert
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @Order(30)
    class GetNodeByIdOrThrow {
        @Test
        void shouldReturnNodeByIdIfExists() {
            // Arrange
            Node node = Node.builder("node-by-id-or-throw")
                .address("127.0.0.2")
                .id(UUID.randomUUID())
                .build();
            consulNodesClient.registerNode(node);
            // Act
            var foundNode = consulNodesClient.getNodeByIdOrThrow(node.getId());
            // Assert
            assertThat(foundNode).isNotNull();
            assertThat(foundNode.getNodeName()).isEqualTo("node-by-id-or-throw");
        }

        @Test
        void shouldThrowIfNodeNotExists() {
            // Act & Assert
            assertThatThrownBy(() -> {
                consulNodesClient.getNodeByIdOrThrow(UUID.randomUUID());
            }).isInstanceOf(ConsulNodeNotFoundException.class);
        }
    }

    @Nested
    @Order(40)
    class AddMetaDataToNode {
        @Test
        void shouldAddMetaDataToNode() {
            // Arrange
            Node node = Node.builder("meta-node")
                .address("127.0.0.4")
                .id(UUID.randomUUID())
                .build();
            consulNodesClient.registerNode(node);
            // Act
            Map<String, String> meta = new HashMap<>();
            meta.put("key", "value");
            consulNodesClient.addMetaDataToNode(node.getId(), meta);
            // Assert
            Optional<Node> updatedNode = consulNodesClient.getNodeById(node.getId());
            assertThat(updatedNode).isPresent();
            assertThat(updatedNode.get().getMeta()).containsEntry("key", "value");
        }

        @Test
        void addToNonExistingNodeShouldThrow() {
            // Act & Assert
            assertThatThrownBy(() -> {
                consulNodesClient.addMetaDataToNode(UUID.randomUUID(), Map.of("key", "value"));
            }).isInstanceOf(ConsulNodeNotFoundException.class);
        }
    }

    @Nested
    @Order(50)
    class DeleteNode {
        @Test
        void shouldDeleteNodeSuccessfully() {
            // Arrange
            Node node = Node.builder("delete-node")
                .address("127.0.0.3")
                .build();
            consulNodesClient.registerNode(node);
            // Act
            consulNodesClient.deleteNodeByName("delete-node");
            // Act
            List<Node> nodes = consulNodesClient.getNodes();
            assertThat(nodes).extracting(Node::getNodeName).doesNotContain("delete-node");
        }

        @Test
        void deletingNonExistingNodeShouldNotThrow() {
            // Act & Assert
            Assertions.assertDoesNotThrow(() -> {
                consulNodesClient.deleteNodeByName("non-existing-node");
            });
        }
    }

}
