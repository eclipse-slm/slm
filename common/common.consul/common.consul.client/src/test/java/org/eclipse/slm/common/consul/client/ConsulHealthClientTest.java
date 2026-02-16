package org.eclipse.slm.common.consul.client;

import org.eclipse.slm.common.consul.client.testutils.ConsulTestClientFactory;
import org.eclipse.slm.common.consul.model.catalog.CatalogRegistration;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.exceptions.ConsulNodeNotFoundException;
import org.eclipse.slm.common.consul.model.health.Check;
import org.eclipse.slm.common.consul.testing.containers.ConsulTestContainer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Testcontainers
public class ConsulHealthClientTest {
    private final static Logger LOG = LoggerFactory.getLogger(ConsulHealthClientTest.class);

    @Container
    private final static ConsulTestContainer consulContainer = new ConsulTestContainer();

    public static ConsulClient consulClient;
    public static ConsulHealthClient consulHealthClient;
    public static ConsulNodesClient consulNodesClient;

    @BeforeAll
    public static void beforeAll() {
        consulClient = ConsulTestClientFactory.getConsulClient(consulContainer);
        consulHealthClient = consulClient.health();
        consulNodesClient = consulClient.nodes();
    }

    private UUID registerNode(String name) {
        Node node = Node.builder(name)
            .address("127.0.0.1")
            .id(UUID.randomUUID())
            .build();
        consulNodesClient.registerNode(node);
        return node.getId();
    }

    private void addSimpleCheck(String nodeName, String checkId) {
        CatalogRegistration.Check check = new CatalogRegistration.Check("sample-check", checkId, "", "passing", null, null);
        consulHealthClient.addCheckToService(nodeName, check);
    }

    @Nested
    @Order(10)
    class GetChecksOfNode {
        @Test
        void shouldReturnEmptyListForNewNode() {
            UUID nodeId = registerNode("health-node-empty");

            var checks = consulHealthClient.getChecksOfNode(nodeId);

            assertThat(checks).isEmpty();
        }

        @Test
        void shouldThrowWhenNodeDoesNotExist() {
            assertThatThrownBy(() -> consulHealthClient.getChecksOfNode(UUID.randomUUID()))
                .isInstanceOf(ConsulNodeNotFoundException.class);
        }
    }

    @Nested
    @Order(20)
    class AddCheckToService {
        @Test
        void shouldAddCheckToExistingNode() {
            String nodeName = "health-node-with-check";
            UUID nodeId = registerNode(nodeName);

            addSimpleCheck(nodeName, "custom-check-id");

            var checks = consulHealthClient.getChecksOfNode(nodeId);
            assertThat(checks)
                .extracting(Check::getCheckId)
                .contains("custom-check-id");
        }
    }

    @Nested
    @Order(30)
    class RemoveCheckFromNodeByName {
        @Test
        void shouldRemoveCheckByName() {
            String nodeName = "health-node-remove-name";
            UUID nodeId = registerNode(nodeName);
            String checkId = "remove-by-name-check";
            addSimpleCheck(nodeName, checkId);

            consulHealthClient.removeCheckFromNode(nodeName, checkId);

            var checks = consulHealthClient.getChecksOfNode(nodeId);
            assertThat(checks)
                .extracting(Check::getCheckId)
                .doesNotContain(checkId);
        }
    }

    @Nested
    @Order(40)
    class RemoveCheckFromNodeById {
        @Test
        void shouldRemoveCheckById() {
            String nodeName = "health-node-remove-id";
            UUID nodeId = registerNode(nodeName);
            String checkId = "remove-by-id-check";
            addSimpleCheck(nodeName, checkId);

            consulHealthClient.removeCheckFromNode(nodeId, checkId);

            var checks = consulHealthClient.getChecksOfNode(nodeId);
            assertThat(checks)
                .extracting(Check::getCheckId)
                .doesNotContain(checkId);
        }
    }

    @Nested
    @Order(50)
    class GetSerfHealthChecksOfNode {
        @Test
        void shouldReturnEmptyForRegisteredCatalogNode() {
            UUID nodeId = registerNode("health-node-serf");

            var serfChecks = consulHealthClient.getSerfHealthChecksOfNode(nodeId);

            assertThat(serfChecks).isEmpty();
        }
    }
}
