package org.eclipse.slm.resource_management.features.capabilities;


import org.eclipse.slm.common.consul.model.exceptions.ConsulNodeNotFoundException;
import org.eclipse.slm.common.consul.testing.containers.ConsulTestContainer;
import org.eclipse.slm.common.consul.testing.utils.ConsulTestClientFactory;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Testcontainers
public class CapabilitiesConsulClientTest {
    private final static Logger LOG = LoggerFactory.getLogger(CapabilitiesConsulClientTest.class);

    @Container
    private final static ConsulTestContainer consulContainer = new ConsulTestContainer();

    private static final String TEST_USER_ID = UUID.randomUUID().toString();
    private static final String TEST_GROUP_ID = "/users/" + TEST_USER_ID;

    private static CapabilitiesConsulClient capabilitiesConsulClient;

    @Mock
    private static CapabilityJpaRepository capabilityJpaRepository;

    @BeforeAll
    public static void beforeAll(){
        var consulAdminClient = ConsulTestClientFactory.getConsulClient(consulContainer);

        capabilitiesConsulClient = new CapabilitiesConsulClient(consulAdminClient, capabilityJpaRepository);
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetCapabilityServicesTests {
        @Test
        @Order(1)
        void shouldReturnEmptyListIfNoCapabilities() {
            var result = capabilitiesConsulClient.getCapabilityServices();
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetCapabilityServicesByCapabilityClassTests {
        @Test
        @Order(1)
        void shouldReturnEmptyListIfNoCapabilitiesForClass() {
            var result = capabilitiesConsulClient.getCapabilityServicesByCapabilityClass(DummyCapability.class);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
        static class DummyCapability {}
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetCapabilityServicesOfResourceTests {
        @Test
        @Order(1)
        void shouldThrowResourceNotFoundExceptionIfResourceDoesNotExist() {
            UUID resourceId = UUID.randomUUID();
            assertThrows(ConsulNodeNotFoundException.class, () -> {
                capabilitiesConsulClient.getCapabilityServicesOfResource(resourceId);
            });
        }
    }
}
