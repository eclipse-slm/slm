package org.eclipse.slm.resource_management.common.adapters;

import org.eclipse.slm.common.consul.client.ConsulClient;

import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulPolicyNotFoundException;
import org.eclipse.slm.common.consul.testing.containers.ConsulTestContainer;
import org.eclipse.slm.common.consul.testing.utils.ConsulTestClientFactory;
import org.eclipse.slm.common.consul.testing.utils.ConsulTestContainerInitializer;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.resources.BasicResource;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class ResourcesConsulClientTest {
    private final static Logger LOG = LoggerFactory.getLogger(ResourcesConsulClientTest.class);

    private static final String TEST_USER_ID = UUID.randomUUID().toString();
    private static final String TEST_GROUP_ID = "/users/" + TEST_USER_ID;

    @Container
    private final static ConsulTestContainer consulContainer = new ConsulTestContainer();

    private static ResourcesConsulClient resourcesConsulClient;
    private static ConsulClient consulClient;

    private static BasicResource testResource = new BasicResource(UUID.randomUUID(), "test-hostname", "192.168.0.1");

    @BeforeAll
    public static void beforeAll() throws InterruptedException {
        consulClient = ConsulTestClientFactory.getConsulClient(consulContainer);
        resourcesConsulClient = new ResourcesConsulClient(consulClient);

        var consulTestInitializer = new ConsulTestContainerInitializer(consulContainer, false);
        consulTestInitializer.initUserGroup(TEST_GROUP_ID);
    }

    @AfterAll
    public static void afterAll(){
        consulContainer.stop();
    }

    @Test
    @Order(10)
    public void testGetResources() throws ConsulLoginFailedException {
        var resources = resourcesConsulClient.getResources();
        assertEquals(0, resources.size());
    }

    @Test
    @Order(20)
    public void testCreateResource() throws ConsulLoginFailedException {
        // Act
        resourcesConsulClient.addResource(testResource, TEST_GROUP_ID);
        // Assert | Resource is created
        var createdResource = resourcesConsulClient.getResourceById(testResource.getId());
        assertThat(createdResource).isPresent();
        // Assert | Consul Policy is created and assigned to user group role
        var consulPolicyName = ResourcesConsulClient.getResourcePolicyName(testResource.getId());
        var consulPolicy = consulClient.acl().getPolicyByNameOrThrow(consulPolicyName);
        assertThat(consulPolicy).isNotNull();
        var groupRole = consulClient.acl().getRoleByName(TEST_GROUP_ID);
        assertThat(groupRole.getPolicies())
            .extracting("name")
            .anyMatch(consulPolicyName::equals);
    }

    @Test
    @Order(30)
    public void testGetResourceById() {
        // Act
        var foundResource = resourcesConsulClient.getResourceByIdOrThrow(testResource.getId());
        // Assert
        assertThat(foundResource.getHostname()).isEqualTo(testResource.getHostname());
        assertThat(foundResource.getIp()).isEqualTo(testResource.getIp());
    }

    @Test
    @Order(40)
    public void testDeleteResource() throws ConsulLoginFailedException {
        var resource = resourcesConsulClient.getResourceByIdOrThrow(testResource.getId());
        resourcesConsulClient.deleteResource(resource);

        assertThatThrownBy(() -> {
            resourcesConsulClient.getResourceByIdOrThrow(testResource.getId());
        }).isInstanceOf(ResourceNotFoundException.class);
        var consulPolicyName = ResourcesConsulClient.getResourcePolicyName(testResource.getId());
        assertThatThrownBy(() ->
                consulClient.acl().getPolicyByNameOrThrow(consulPolicyName))
                .isInstanceOf(ConsulPolicyNotFoundException.class);
        var groupRole = consulClient.acl().getRoleByName(TEST_GROUP_ID);
        assertThat(groupRole.getPolicies())
                .extracting("name")
                .noneMatch(consulPolicyName::equals);
    }
}
