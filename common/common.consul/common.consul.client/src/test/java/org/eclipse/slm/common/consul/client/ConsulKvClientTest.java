package org.eclipse.slm.common.consul.client;

import org.eclipse.slm.common.consul.client.testutils.ConsulTestClientFactory;
import org.eclipse.slm.common.consul.model.exceptions.ConsulKvEntryNotFoundException;
import org.eclipse.slm.common.consul.model.kv.KeyValueData;
import org.eclipse.slm.common.consul.testing.containers.ConsulTestContainer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Testcontainers
public class ConsulKvClientTest {
    private final static Logger LOG = LoggerFactory.getLogger(ConsulKvClientTest.class);

    @Container
    private final static ConsulTestContainer consulContainer = new ConsulTestContainer();

    public static ConsulClient consulClient;
    public static ConsulKvClient consulKvClient;

    @BeforeAll
    public static void beforeAll() {
        consulClient = ConsulTestClientFactory.getConsulClient(consulContainer);
        consulKvClient = consulClient.kv();
    }

    @Nested
    @Order(10)
    class PutKey {
        @Test
        void shouldStoreValue() {
            // Arrange
            var key = "kv/" + UUID.randomUUID();
            var payload = "hello-world";
            // Act
            var result = consulKvClient.putKey(key, payload);
            // Assert
            assertThat(result).isTrue();
        }
    }

    @Nested
    @Order(20)
    class ReadKey {
        @Test
        void shouldReturnStoredValue() {
            // Arrange
            var key = "kv-read/" + UUID.randomUUID();
            var payload = "value-to-read";
            consulKvClient.putKey(key, payload);
            // Act
            List<KeyValueData> result = consulKvClient.readKey(key, false, false, false, null);
            // Assert
            assertThat(result).hasSize(1);
            var decoded = new String(Base64.getDecoder().decode(result.get(0).getValueBase64Encoded()), StandardCharsets.UTF_8);
            if (decoded.startsWith("\"") && decoded.endsWith("\"") && decoded.length() >= 2) {
                decoded = decoded.substring(1, decoded.length() - 1);
            }
            assertThat(decoded).isEqualTo(payload);
        }

        @Test
        void readingMissingKeyShouldThrow() {
            // Arrange
            var key = "kv/missing/" + UUID.randomUUID();
            // Act & Assert
            assertThatThrownBy(() -> consulKvClient.readKey(key, false, false, false, null))
                .isInstanceOf(ConsulKvEntryNotFoundException.class);
        }
    }

    @Nested
    @Order(30)
    class DeleteKey {
        @Test
        void shouldDeleteExistingKey() {
            // Arrange
            var key = "kv/delete/" + UUID.randomUUID();
            consulKvClient.putKey(key, "to-delete");
            // Act
            boolean deleted = consulKvClient.deleteKey(key, false);
            // Assert
            assertThat(deleted).isTrue();
            assertThatThrownBy(() -> consulKvClient.readKey(key, false, false, false, null))
                .isInstanceOf(ConsulKvEntryNotFoundException.class);
        }

        @Test
        void deletingMissingKeyShouldNotThrow() {
            // Arrange
            var key = "kv/delete-missing/" + UUID.randomUUID();
            // Act & Assert
            assertThat(consulKvClient.deleteKey(key, false)).isTrue();
        }
    }
}
