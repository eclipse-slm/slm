package org.eclipse.slm.common.consul.client;

import org.eclipse.slm.common.consul.client.testutils.ConsulTestClientFactory;
import org.eclipse.slm.common.consul.model.catalog.CatalogRegistration;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.exceptions.ConsulServiceNotFoundException;
import org.eclipse.slm.common.consul.testing.containers.ConsulTestContainer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Testcontainers
public class ConsulServicesClientTest {
    private final static Logger LOG = LoggerFactory.getLogger(ConsulServicesClientTest.class);

    @Container
    private final static ConsulTestContainer consulContainer = new ConsulTestContainer();

    public static ConsulClient consulClient;
    public static ConsulServicesClient consulServicesClient;
    public static ConsulNodesClient consulNodesClient;

    @BeforeAll
    public static void beforeAll() {
        consulClient = ConsulTestClientFactory.getConsulClient(consulContainer);
        consulNodesClient = consulClient.nodes();
        consulServicesClient = consulClient.services();
    }

    @Nested
    @Order(10)
    class GetServices {
        @Test
        void shouldReturnEmptyInitially() {
            var services = consulServicesClient.getServices();
            assertThat(services).isNotNull();
            // By default, only 'consul' service should exist
            assertThat(services).containsKey("consul");
        }
    }

    @Nested
    @Order(20)
    class RegisterService {
        @Test
        void shouldRegisterServiceSuccessfully() {
            // Arrange
            Node node = Node.builder("service-node")
                .address("127.0.0.10")
                .id(UUID.randomUUID())
                .build();
            consulNodesClient.registerNode(node);
            var serviceId = UUID.randomUUID().toString();
            String serviceName = "test-service";
            List<String> tags = List.of("tag1", "tag2");
            Map<String, String> meta = Map.of("metaKey", "metaValue");

            var service = CatalogRegistration.Service.builder(serviceName)
                .id(serviceId)
                .tags(tags)
                .meta(meta)
                .port(8080)
                .build();

            // Act
            var registered = consulServicesClient.registerService(node.getId(), service);

            // Assert
            assertThat(registered.getServiceName()).isEqualTo(serviceName);
            assertThat(registered.getServiceId().toString()).isEqualTo(serviceId);
            assertThat(registered.getServiceTags()).containsAll(tags);
            assertThat(registered.getServiceMeta()).containsAllEntriesOf(meta);
        }
    }

    @Nested
    @Order(30)
    class GetServiceById {
        @Test
        void shouldReturnServiceByIdIfExists() {
            // Arrange
            Node node = Node.builder("service-by-id-node")
                .address("127.0.0.11")
                .id(UUID.randomUUID())
                .build();
            consulNodesClient.registerNode(node);
            var serviceId = UUID.randomUUID().toString();
            String serviceName = "service-by-id";

            var service = CatalogRegistration.Service.builder(serviceName)
                .id(serviceId)
                .port(8081)
                .build();
            consulServicesClient.registerService(node.getId(), service);

            // Act
            var found = consulServicesClient.getServiceById(UUID.fromString(serviceId));

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getServiceName()).isEqualTo(serviceName);
        }

        @Test
        void shouldReturnEmptyIfServiceNotExists() {
            var found = consulServicesClient.getServiceById(UUID.randomUUID());
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @Order(40)
    class GetServicesByTag {
        @Test
        void shouldReturnServicesWithTag() {
            // Arrange
            Node node = Node.builder("tag-node")
                .address("127.0.0.12")
                .id(UUID.randomUUID())
                .build();
            consulNodesClient.registerNode(node);
            var serviceId = UUID.randomUUID().toString();
            String serviceName = "tagged-service";
            List<String> tags = List.of("special-tag");

            var service = CatalogRegistration.Service.builder(serviceName)
                .id(serviceId)
                .tags(tags)
                .port(8082)
                .build();
            consulServicesClient.registerService(node.getId(), service);

            // Act
            var services = consulServicesClient.getServicesByTag("special-tag");

            // Assert
            assertThat(services).containsKey(serviceName);
            assertThat(services.get(serviceName)).contains("special-tag");
        }
    }

    @Nested
    @Order(50)
    class GetServicesByName {
        @Test
        void shouldReturnServicesByName() {
            // Arrange
            Node node = Node.builder("by-name-node")
                .address("127.0.0.13")
                .id(UUID.randomUUID())
                .build();
            consulNodesClient.registerNode(node);
            var serviceId = UUID.randomUUID().toString();
            String serviceName = "by-name-service";

            var service = CatalogRegistration.Service.builder(serviceName)
                .id(serviceId)
                .port(8083)
                .build();
            consulServicesClient.registerService(node.getId(), service);

            // Act
            var services = consulServicesClient.getServicesByName(List.of(serviceName));

            // Assert
            assertThat(services).containsKey(serviceName);
            assertThat(services.get(serviceName)).isNotEmpty();
        }
    }

    @Nested
    @Order(60)
    class GetServiceByName {
        @Test
        void shouldReturnServiceByNameIfExists() {
            // Arrange
            Node node = Node.builder("service-by-name-node")
                .address("127.0.0.14")
                .id(UUID.randomUUID())
                .build();
            consulNodesClient.registerNode(node);
            var serviceId = UUID.randomUUID().toString();
            String serviceName = "service-by-name";

            var service = CatalogRegistration.Service.builder(serviceName)
                .id(serviceId)
                .port(8084)
                .build();
            consulServicesClient.registerService(node.getId(), service);

            // Act
            var found = consulServicesClient.getServiceByName(serviceName);

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().get(0).getServiceName()).isEqualTo(serviceName);
        }

        @Test
        void shouldReturnEmptyIfServiceNameNotExists() {
            var found = consulServicesClient.getServiceByName("not-existing-service");
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @Order(70)
    class GetNodeServices {
        @Test
        void shouldReturnNodeServices() {
            // Arrange
            var node = Node
                    .builder("node-services-node")
                    .id(UUID.randomUUID())
                    .address("127.0.0.15")
                .build();
            consulNodesClient.registerNode(node);
            var serviceId = UUID.randomUUID().toString();
            String serviceName = "node-services-service";
            var service = CatalogRegistration.Service.builder(serviceName)
                    .id(serviceId)
                    .build();
            consulServicesClient.registerService(node.getId(), service);
            // Act
            var nodeServices = consulServicesClient.getNodeServices(node.getNodeName());
            // Assert
            assertThat(nodeServices).isNotEmpty();
            assertThat(nodeServices).anyMatch(ns -> ns.getServiceName().equals(serviceName));
        }
    }

    @Nested
    @Order(80)
    class RemoveServiceByName {
        @Test
        void shouldRemoveServiceByName() {
            // Arrange
            Node node = Node.builder("remove-service-node")
                .address("127.0.0.16")
                .id(UUID.randomUUID())
                .build();
            consulNodesClient.registerNode(node);
            var serviceId = UUID.randomUUID().toString();
            String serviceName = "remove-service";

            var service = CatalogRegistration.Service.builder(serviceName)
                .id(serviceId)
                .port(8086)
                .build();
            consulServicesClient.registerService(node.getId(), service);

            // Act
            consulServicesClient.removeServiceByName(node.getId(), serviceName);

            // Assert
            var found = consulServicesClient.getServiceByName(serviceName);
            assertThat(found).isEmpty();
        }

        @Test
        void shouldThrowIfServiceNotFound() {
            Node node = Node.builder("remove-service-notfound-node")
                .address("127.0.0.17")
                .id(UUID.randomUUID())
                .build();
            consulNodesClient.registerNode(node);

            // Act & Assert
            assertThatThrownBy(() -> {
                consulServicesClient.removeServiceByName(node.getId(), "not-existing-service");
            }).isInstanceOf(ConsulServiceNotFoundException.class);
        }
    }
}
