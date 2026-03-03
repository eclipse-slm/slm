package org.eclipse.slm.resource_management.features.capabilities;


import org.eclipse.slm.common.consul.client.ConsulClient;
import org.eclipse.slm.common.consul.model.acl.roles.PolicyLink;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.consul.model.exceptions.ConsulPolicyNotFoundException;
import org.eclipse.slm.common.consul.testing.containers.ConsulTestContainer;
import org.eclipse.slm.common.consul.testing.utils.ConsulTestClientFactory;
import org.eclipse.slm.common.consul.testing.utils.ConsulTestContainerInitializer;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClientFactory;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.resources.BasicResource;
import org.eclipse.slm.resource_management.features.capabilities.model.*;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
import org.eclipse.slm.resource_management.features.capabilities.persistence.SingleHostCapabilitiesConsulClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Testcontainers
@ExtendWith(MockitoExtension.class)
public class SingleHostCapabilitiesConsulClientTest {
    private final static Logger LOG = LoggerFactory.getLogger(SingleHostCapabilitiesConsulClientTest.class);

    @Container
    private final static ConsulTestContainer consulContainer = new ConsulTestContainer();

    private static final String TEST_USER_ID = UUID.randomUUID().toString();
    private static final String TEST_GROUP_ID = "/users/" + TEST_USER_ID;

    private static ConsulClient adminConsulClient;
    private static CapabilitiesConsulClient capabilitiesConsulClient;
    private static SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient;
    private static ResourcesConsulClient resourcesConsulAdminClient;

    private static CapabilityJpaRepository capabilityJpaRepository;

    @BeforeAll
    public static void beforeAll() {
        capabilityJpaRepository = Mockito.mock(CapabilityJpaRepository.class);

        var consulTestInitializer = new ConsulTestContainerInitializer(consulContainer, false);
        consulTestInitializer.initUserGroup(TEST_GROUP_ID);

        var consulClientFactory = ConsulTestClientFactory.getConsulClientFactory(consulContainer);
        adminConsulClient = consulClientFactory.createAdminClient();
        var resourceConsulClientFactory = new ResourcesConsulClientFactory(consulClientFactory);

        capabilitiesConsulClient = new CapabilitiesConsulClient(consulClientFactory, capabilityJpaRepository);
        resourcesConsulAdminClient = new ResourcesConsulClient(consulClientFactory.createAdminClient());

        singleHostCapabilitiesConsulClient = new SingleHostCapabilitiesConsulClient(
                consulClientFactory,
                resourceConsulClientFactory,
                capabilityJpaRepository);

        resourcesConsulAdminClient.addResource(SingleHostCapabilitiesConsulClientTestData.testResource1, TEST_GROUP_ID);
        resourcesConsulAdminClient.addResource(SingleHostCapabilitiesConsulClientTestData.testResource2, TEST_GROUP_ID);

        Mockito.lenient()
                .when(capabilityJpaRepository.findById(SingleHostCapabilitiesConsulClientTestData.testSingleHostDeploymentCapability.getId()))
                .thenReturn(Optional.ofNullable(SingleHostCapabilitiesConsulClientTestData.testSingleHostDeploymentCapability));
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Order(10)
    class AddSingleHostCapabilityToNode {
        @Test
        @Order(10)
        void shouldAddCapability() {
            // Act
            var capabilityService = singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                SingleHostCapabilitiesConsulClientTestData.testSingleHostDeploymentCapability,
                SingleHostCapabilitiesConsulClientTestData.testResource1.getId(),
                CapabilityServiceStatus.INSTALL,
                false,
                new HashMap<>(),
                TEST_GROUP_ID
            );
            // Assert | Created Consul service
            var consulService = adminConsulClient.services().getServiceByName(capabilityService.getServiceName());
            assertThat(consulService).isPresent();
            assertThat(consulService.get().get(0).getServiceMeta().keySet())
                .contains(
                    CapabilityService.META_KEY_CAPABILITY_SERVICE_CLASS,
                    CapabilityService.META_KEY_CAPABILITY_CLASS,
                    CapabilityService.META_KEY_CAPABILITY_ID,
                    CapabilityService.META_KEY_CONNECTION_TYPE,
                    CapabilityService.META_KEY_STATUS,
                    CapabilityService.META_KEY_MANAGED
                );
            // Assert | Created Consul ACL Policy
            var capabilityServicePolicyName = CapabilitiesConsulClient.getCapabilityServicePolicyName(capabilityService.getId());
            var capabilityServicePolicy = adminConsulClient.acl().getPolicyByNameOrThrow(capabilityServicePolicyName);
            assertThat(capabilityServicePolicy).isNotNull();
            // Assert | Assigned Policy to Role of User Group
            var userRole = adminConsulClient.acl().getRoleByName(TEST_GROUP_ID);
            assertThat(userRole.getPolicies())
                    .extracting(PolicyLink::getName)
                    .contains(capabilityServicePolicyName);
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Order(20)
    class UpdateCapabilityService {
        @Test
        @Order(10)
        void shouldUpdateStatus() throws ConsulLoginFailedException {
            // Arrange
            var nodeId = SingleHostCapabilitiesConsulClientTestData.testResource1.getId();
            var capabilityServiceToUpdate = singleHostCapabilitiesConsulClient.getCapabilityServiceForCapabilityOfResource(
                SingleHostCapabilitiesConsulClientTestData.testSingleHostDeploymentCapability,
                nodeId
            );
            capabilityServiceToUpdate.setStatus(CapabilityServiceStatus.READY);
            // Act
            singleHostCapabilitiesConsulClient.updateCapabilityService(nodeId, capabilityServiceToUpdate);

            var capabilityServiceAfterUpdate = singleHostCapabilitiesConsulClient.getCapabilityServiceForCapabilityOfResource(
                SingleHostCapabilitiesConsulClientTestData.testSingleHostDeploymentCapability,
                nodeId
            );
            assertThat(capabilityServiceAfterUpdate.getStatus()).isEqualTo(CapabilityServiceStatus.READY);
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Order(30)
    class GetSingleHostCapabilityServicesOfResource {
        @Test
        @Order(10)
        void shouldReturnOneService() {
            var singleHostCapabilityServices = singleHostCapabilitiesConsulClient.getSingleHostCapabilityServicesOfResource(
                SingleHostCapabilitiesConsulClientTestData.testResource1.getId()
            );
            assertThat(singleHostCapabilityServices).hasSize(1);
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Order(40)
    class GetCapabilityServicesByCapabilityClass {
        @Test
        @Order(10)
        void shouldReturnDeploymentCapabilityServices() {
            var deploymentCapabilityServices = capabilitiesConsulClient.getCapabilityServicesByCapabilityClass(DeploymentCapability.class);
            // Prüfen, ob die Test-Capability in den Ergebnissen enthalten ist (nach Capability-ID)
            assertThat(deploymentCapabilityServices)
                .anySatisfy(cs -> assertThat(cs.getCapability().getId())
                    .isEqualTo(SingleHostCapabilitiesConsulClientTestData.testSingleHostDeploymentCapability.getId()));
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Order(50)
    class RemoveSingleHostCapabilityFromNode {
        @Test
        @Order(40)
        void shouldRemove() throws ConsulLoginFailedException, ResourceNotFoundException {
            // Arrange
            var nodeId = SingleHostCapabilitiesConsulClientTestData.testResource1.getId();
            var before = capabilitiesConsulClient.getCapabilityServicesOfResource(nodeId);
            var capabilityServiceId = before.get(0).getId();
            // Act
            singleHostCapabilitiesConsulClient.removeSingleHostCapabilityFromNode(
                    SingleHostCapabilitiesConsulClientTestData.testSingleHostDeploymentCapability,
                    nodeId
            );
            // Assert | Consul Service removed
            var after = capabilitiesConsulClient.getCapabilityServicesOfResource(nodeId);
            assertThat(after.size()).isEqualTo(before.size() - 1);
            // Assert | Consul ACL Policy removed
            var capabilityServicePolicyName = CapabilitiesConsulClient.getCapabilityServicePolicyName(capabilityServiceId);
            assertThatThrownBy(() -> adminConsulClient.acl().getPolicyByNameOrThrow(capabilityServicePolicyName))
                    .isInstanceOf(ConsulPolicyNotFoundException.class);
            // Assert | Policy unassigned from Role of User Group
            var userRole = adminConsulClient.acl().getRoleByName(TEST_GROUP_ID);
            assertThat(userRole.getPolicies())
                    .extracting(PolicyLink::getName)
                    .doesNotContain(capabilityServicePolicyName);
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Order(60)
    class BatchRemoveFromAllResources {
        @Test
        @Order(10)
        void shouldRemoveAll() {
            // Arrange
            var capabilityServiceOnResource1 = singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                    SingleHostCapabilitiesConsulClientTestData.testSingleHostDeploymentCapability,
                    SingleHostCapabilitiesConsulClientTestData.testResource1.getId(),
                    CapabilityServiceStatus.INSTALL,
                    false,
                    new HashMap<>(),
                    TEST_GROUP_ID
            );
            var capabilityServiceOnResource2 = singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                    SingleHostCapabilitiesConsulClientTestData.testSingleHostDeploymentCapability,
                    SingleHostCapabilitiesConsulClientTestData.testResource2.getId(),
                    CapabilityServiceStatus.INSTALL,
                    false,
                    new HashMap<>(),
                    TEST_GROUP_ID
            );
            var batchResources = Arrays.asList(
                SingleHostCapabilitiesConsulClientTestData.testResource1,
                SingleHostCapabilitiesConsulClientTestData.testResource2
            );
            for (BasicResource resource : batchResources) {
                var nodeServicesAfter = adminConsulClient.services().getNodeServicesByNodeId(resource.getId());
                assertThat(nodeServicesAfter).isNotEmpty();
            }
            // Act
            singleHostCapabilitiesConsulClient.removeCapabilityServiceFromAllConsulNodes(
                SingleHostCapabilitiesConsulClientTestData.testSingleHostDeploymentCapability
            );
            // Assert
            for (BasicResource resource : batchResources) {
                var nodeServicesAfter = adminConsulClient.services().getNodeServicesByNodeId(resource.getId());
                assertThat(nodeServicesAfter).isEmpty();
            }
        }
    }
}
