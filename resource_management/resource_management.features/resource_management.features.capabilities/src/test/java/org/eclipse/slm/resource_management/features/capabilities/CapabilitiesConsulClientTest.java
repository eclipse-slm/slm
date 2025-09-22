package org.eclipse.slm.resource_management.features.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.*;
import org.eclipse.slm.common.consul.model.catalog.CatalogNode;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.location.LocationJpaRepository;
import org.eclipse.slm.resource_management.common.remote_access.ConnectionType;
import org.eclipse.slm.resource_management.common.resources.BasicResource;
import org.eclipse.slm.resource_management.features.capabilities.CapabilityUtil;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.clusters.ScaleDownOperation;
import org.eclipse.slm.resource_management.features.capabilities.clusters.ScaleUpOperation;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.ClusterMemberType;
import org.eclipse.slm.resource_management.features.capabilities.model.*;
import org.eclipse.slm.resource_management.features.capabilities.model.*;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionConfigParameter;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionConfigParameterRequiredType;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionConfigParameterValueType;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
import org.eclipse.slm.resource_management.features.capabilities.persistence.SingleHostCapabilitiesConsulClient;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.*;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        ConsulServicesApiClient.class,
        ConsulGenericServicesClient.class,
        ConsulGenericNodeRemoveClient.class,
        ConsulAgentApiClient.class,
        ConsulNodesApiClient.class,
        ConsulAclApiClient.class,
        ConsulHealthApiClient.class,
        ConsulKeyValueApiClient.class,
        CapabilitiesConsulClient.class,
        SingleHostCapabilitiesConsulClient.class,
        MultiHostCapabilitiesConsulClient.class,
        ResourcesConsulClient.class,
        CapabilityJpaRepository.class,
        CapabilityUtil.class,
        RestTemplate.class,
        ObjectMapper.class
})
@ContextConfiguration(initializers = {ConfigDataApplicationContextInitializer.class} )
@ActiveProfiles("test")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class CapabilitiesConsulClientTest {
    public final static Logger LOG = LoggerFactory.getLogger(CapabilitiesConsulClientTest.class);

    public final static GenericContainer<?> consulDockerContainer;
    private static int CONSUL_PORT = 8500;
    @DynamicPropertySource
    static void consulProperties(DynamicPropertyRegistry registry){
        registry.add("consul.port", consulDockerContainer::getFirstMappedPort);
    }

    @Autowired
    ConsulServicesApiClient consulServicesApiClient;
    @Autowired
    ConsulNodesApiClient consulNodesApiClient;
    @Autowired
    ConsulAclApiClient consulAclApiClient;
    @Autowired
    ConsulHealthApiClient consulHealthApiClient;
    @Autowired
    ConsulKeyValueApiClient consulKeyValueApiClient;
    @Autowired
    ResourcesConsulClient resourcesConsulClient;
    @Autowired
    CapabilitiesConsulClient capabilitiesConsulClient;
    @MockBean
    CapabilityJpaRepository capabilityJpaRepository;
    @MockBean
    LocationJpaRepository locationJpaRepository;
    @Autowired
    CapabilityUtil capabilityUtil;

    static {
        consulDockerContainer = new GenericContainer<>(DockerImageName.parse("consul:1.14"))
                .withExposedPorts(CONSUL_PORT)
                .withEnv("CONSUL_LOCAL_CONFIG", "{\"datacenter\": \"fabos\", \"domain\": \".fabos\", \"bind_addr\": \"0.0.0.0\", \"retry_join\": [\"0.0.0.0\"], \"acl\":{\"enabled\": true, \"default_policy\": \"allow\", \"tokens\":{\"master\": \"myroot\"}}}");
        consulDockerContainer.start();
    }

    @AfterAll
    public static void afterAll(){
        consulDockerContainer.stop();
    }

    @Nested
    @Order(10)
    public class preTests {
        @Test
        @Order(10)
        public void testInjectedConsulInstances() {
            assertNotEquals(null, consulServicesApiClient);
            assertNotEquals(null, consulNodesApiClient);
            assertNotEquals(null, consulAclApiClient);
            assertNotEquals(null, consulHealthApiClient);
            assertNotEquals(null, resourcesConsulClient);
            assertNotEquals(null, capabilitiesConsulClient);
        }
    }

    @Nested
    @Order(20)
    @TestClassOrder(ClassOrderer.OrderAnnotation.class)
    public class testSingleHostCapabilityServices {

        @Autowired
        SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient;

        @Nested
        @Order(10)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        public class testDeploymentCapabilityServices{
            //region Variables - testDeploymentCapabilityServices
            private static ResourcesConsulClient staticResourcesConsulClient = null;
            private static BasicResource basicResource = new BasicResource(
                    UUID.randomUUID(),
                    "Test-Host",
                    "192.168.0.1"
            );

            private static BasicResource basicResourceFromConsul = null;
            public static DeploymentCapability singleHostDeploymentCapabilityWithoutHealthcheck = null;

            public static DeploymentCapability singleHostDeploymentCapabilityWithoutHealthcheckWithConfigParameter = null;
            public static DeploymentCapability singleHostDeploymentCapabilityWithoutHealthcheckWithConnection = null;
            public static DeploymentCapability singleHostDeploymentCapabilityWithHealthcheck = null;

            public static Map<String, String> configParameter = Map.of(
                    "username", "user",
                    "password", "pass"
            );
            public static int resourceCount = 10;
            public static List<BasicResource> batchResources = new ArrayList<>(resourceCount);
            //endregion

            @Autowired
            public testDeploymentCapabilityServices(
                    ResourcesConsulClient resourcesConsulClient
            ) throws ConsulLoginFailedException {
                staticResourcesConsulClient = resourcesConsulClient;

                resourcesConsulClient.addResource(
                        basicResource
                );

                basicResourceFromConsul = resourcesConsulClient.getResourceByHostname(
                        new ConsulCredential(),
                        basicResource.getHostname()
                ).get();
            }

            @BeforeAll
            public static void beforeAll() {
                List<DeploymentCapability> deploymentCapabilities = new ArrayList<>(4);
                singleHostDeploymentCapabilityWithHealthcheck = new DeploymentCapability(UUID.randomUUID());
                singleHostDeploymentCapabilityWithoutHealthcheck = new DeploymentCapability(UUID.randomUUID());
                singleHostDeploymentCapabilityWithoutHealthcheckWithConnection = new DeploymentCapability(UUID.randomUUID());
                singleHostDeploymentCapabilityWithoutHealthcheckWithConfigParameter = new DeploymentCapability(UUID.randomUUID());

                deploymentCapabilities.add(singleHostDeploymentCapabilityWithHealthcheck);
                deploymentCapabilities.add(singleHostDeploymentCapabilityWithoutHealthcheck);
                deploymentCapabilities.add(singleHostDeploymentCapabilityWithoutHealthcheckWithConnection);
                deploymentCapabilities.add(singleHostDeploymentCapabilityWithoutHealthcheckWithConfigParameter);

                //region init single host capability
                for(int i = 0; i < deploymentCapabilities.size(); i++) {
                    deploymentCapabilities.get(i).setName("Dummy");
                    deploymentCapabilities.get(i).setLogo("mdi-dummy");
                    deploymentCapabilities.get(i).setType(Arrays.asList(
                            CapabilityType.SETUP,
                            CapabilityType.DEPLOY
                    ));

                    deploymentCapabilities.get(i).setCapabilityClass("DeploymentCapability");

                    deploymentCapabilities.get(i).setSupportedDeploymentTypes(Arrays.asList(
                            DeploymentType.DOCKER_CONTAINER,
                            DeploymentType.DOCKER_COMPOSE
                    ));

                    // Set AWX Capability Actions
                    var deploymentCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-dc-dummy";
                    var deploymentCapabilityBranch = "main";
                    deploymentCapabilities.get(i).getActions()
                            .put(ActionType.INSTALL, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "install.yml"));
                    deploymentCapabilities.get(i).getActions()
                            .put(ActionType.UNINSTALL, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "uninstall.yml"));
                    deploymentCapabilities.get(i).getActions()
                            .put(ActionType.DEPLOY, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "deploy.yml"));
                    deploymentCapabilities.get(i).getActions()
                            .put(ActionType.UNDEPLOY, new AwxAction(deploymentCapabilityRepo, deploymentCapabilityBranch, "undeploy.yml"));
                }

                List<ActionConfigParameter> configParamters = Arrays.asList(
                        new ActionConfigParameter(
                                "username",
                                "Username",
                                "",
                                ActionConfigParameterValueType.STRING,
                                "",
                                ActionConfigParameterRequiredType.ALWAYS,
                                false
                        ),
                        new ActionConfigParameter(
                                "password",
                                "Password",
                                "",
                                ActionConfigParameterValueType.STRING,
                                "",
                                ActionConfigParameterRequiredType.ALWAYS,
                                true
                        )
                );


                // Set Health Check for singleHostDeploymentCapabilityWithHealthcheck
                CapabilityHealthCheck capabilityHealthCheck = new CapabilityHealthCheck(
                        CapabilityHealthCheckType.HTTP,
                        8080,
                        "/health",
                        60
                );
                singleHostDeploymentCapabilityWithHealthcheck.setHealthCheck(capabilityHealthCheck);

                //Set Connection for singleHostDeploymentCapabilityWithoutHealthcheckWithConnection
                singleHostDeploymentCapabilityWithoutHealthcheckWithConnection.setConnection(ConnectionType.http);

                //Set ConfigParameter
                singleHostDeploymentCapabilityWithoutHealthcheckWithConfigParameter
                        .getActions()
                        .get(ActionType.INSTALL)
                        .setConfigParameters(configParamters);
                //endregion
            }

            @BeforeEach
            public void beforeEach() {
                Mockito
                        .when(capabilityJpaRepository.findById(singleHostDeploymentCapabilityWithoutHealthcheck.getId()))
                        .thenReturn(Optional.ofNullable(singleHostDeploymentCapabilityWithoutHealthcheck));

                Mockito
                        .when(capabilityJpaRepository.findById(singleHostDeploymentCapabilityWithHealthcheck.getId()))
                        .thenReturn(Optional.ofNullable(singleHostDeploymentCapabilityWithHealthcheck));

                Mockito
                        .when(capabilityJpaRepository.findById(singleHostDeploymentCapabilityWithoutHealthcheckWithConnection.getId()))
                        .thenReturn(Optional.ofNullable(singleHostDeploymentCapabilityWithoutHealthcheckWithConnection));

                Mockito
                        .when(capabilityJpaRepository.findById(singleHostDeploymentCapabilityWithoutHealthcheckWithConfigParameter.getId()))
                        .thenReturn(Optional.ofNullable(singleHostDeploymentCapabilityWithoutHealthcheckWithConfigParameter));
            }

            @AfterAll
            public static void afterAll() throws ConsulLoginFailedException {
                staticResourcesConsulClient.deleteResource(
                        new ConsulCredential(),
                        basicResourceFromConsul
                );
            }

            @Test
            @Order(10)
            public void getSingleHostDeploymentCapabilityOfResourceIfNoCapabilityIsInstalled() throws ConsulLoginFailedException, ResourceNotFoundException {
                var capabilitiesOfResource = capabilitiesConsulClient.getCapabilityServicesOfResource(
                        basicResourceFromConsul.getId()
                );

                assertEquals(0, capabilitiesOfResource.size());
            }

            @Test
            @Order(20)
            public void testAddSingleHostDeploymentCapabilityServiceWithoutHealthCheck() throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
                var capabilitiesOfResourceBefore = capabilitiesConsulClient.getCapabilityServicesOfResource(
                        basicResourceFromConsul.getId()
                );

                CapabilityService capabilityService = singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                        new ConsulCredential(),
                        singleHostDeploymentCapabilityWithoutHealthcheck,
                        basicResourceFromConsul.getId(),
                        CapabilityServiceStatus.INSTALL,
                        false,
                        new HashMap<>()
                );

                var capabilitiesOfResourceAfter = capabilitiesConsulClient.getCapabilityServicesOfResource(
                        basicResourceFromConsul.getId()
                );

                assertEquals(
                        capabilitiesOfResourceBefore.size()+1,
                        capabilitiesOfResourceAfter.size()
                );

                assertEquals(
                        CapabilityServiceStatus.INSTALL,
                        capabilityService.getStatus()
                );
            }

            @Test
            @Order(25)
            public void testUpdateStatusOfSingleHostDeploymentCapabilityServiceWithoutHealthCheck() throws ConsulLoginFailedException, IllegalAccessException {
                CapabilityServiceStatus newStatus = CapabilityServiceStatus.READY;
                CapabilityService capabilityServiceBefore = singleHostCapabilitiesConsulClient.getCapabilityServiceForCapabilityOfResource(
                        new ConsulCredential(),
                        singleHostDeploymentCapabilityWithoutHealthcheck,
                        basicResourceFromConsul.getId()
                );

                capabilityServiceBefore.setStatus(newStatus);
                singleHostCapabilitiesConsulClient.updateCapabilityService(
                        new ConsulCredential(),
                        basicResourceFromConsul.getId(),
                        capabilityServiceBefore
                );

                CapabilityService capabilityServiceAfter = singleHostCapabilitiesConsulClient.getCapabilityServiceForCapabilityOfResource(
                        new ConsulCredential(),
                        singleHostDeploymentCapabilityWithoutHealthcheck,
                        basicResourceFromConsul.getId()
                );

                assertEquals(
                        CapabilityServiceStatus.READY,
                        capabilityServiceAfter.getStatus()
                );
            }

            @Test
            @Order(26)
            public void testGetSingleHostCapabilityServicesOfResourceExpectOneReturn() throws ConsulLoginFailedException {
                var singleHostCapabilityServices = singleHostCapabilitiesConsulClient.getSingleHostCapabilityServicesOfResource(
                        new ConsulCredential(),
                        basicResourceFromConsul.getId()
                );

                assertEquals(1, singleHostCapabilityServices.size());
            }

            @Test
            @Order(30)
            public void testRemoveSingleHostDeploymentCapabilityServiceWithoutHealthCheck() throws ConsulLoginFailedException, ResourceNotFoundException {
                var capabilitiesOfResourceBefore = capabilitiesConsulClient.getCapabilityServicesOfResource(
                        basicResourceFromConsul.getId()
                );

                singleHostCapabilitiesConsulClient.removeSingleHostCapabilityFromNode(
                        new ConsulCredential(),
                        singleHostDeploymentCapabilityWithoutHealthcheck,
                        basicResourceFromConsul.getId()
                );

                var capabilityServicesOfResourceAfter = capabilitiesConsulClient.getCapabilityServicesOfResource(
                        basicResourceFromConsul.getId()
                );

                assertEquals(
                        capabilitiesOfResourceBefore.size()-1,
                        capabilityServicesOfResourceAfter.size()
                );
            }

            @Test
            @Order(40)
            public void testAddSingleHostDeploymentCapabilityServiceWithHealthCheck() throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
                List<NodeService> nodeServicesBefore = consulServicesApiClient.getNodeServicesByNodeId(
                        new ConsulCredential(),
                        basicResourceFromConsul.getId()
                );
                List<CatalogNode.Check> checksOfNodeBefore = consulHealthApiClient.getChecksOfNode(
                        new ConsulCredential(),
                        basicResourceFromConsul.getId()
                );

                singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                        new ConsulCredential(),
                        singleHostDeploymentCapabilityWithHealthcheck,
                        basicResourceFromConsul.getId(),
                        CapabilityServiceStatus.INSTALL,
                        false,
                        new HashMap<>()
                );

                List<NodeService> nodeServicesAfter = consulServicesApiClient.getNodeServicesByNodeId(
                        new ConsulCredential(),
                        basicResourceFromConsul.getId()
                );
                List<CatalogNode.Check> checksOfNodeAfter = consulHealthApiClient.getChecksOfNode(
                        new ConsulCredential(),
                        basicResourceFromConsul.getId()
                );

                assertEquals(
                        nodeServicesBefore.size()+1,
                        nodeServicesAfter.size()
                );

                assertEquals(
                        checksOfNodeBefore.size()+1,
                        checksOfNodeAfter.size()
                );
            }

            @Test
            @Order(50)
            public void testRemoveSingleHostDeploymentCapabilityServiceWithHealthCheck() throws ConsulLoginFailedException, ResourceNotFoundException {
                List<NodeService> nodeServicesBefore = consulServicesApiClient.getNodeServicesByNodeId(
                        new ConsulCredential(),
                        basicResourceFromConsul.getId()
                );
                List<CatalogNode.Check> checksOfNodeBefore = consulHealthApiClient.getChecksOfNode(
                        new ConsulCredential(),
                        basicResourceFromConsul.getId()
                );

                singleHostCapabilitiesConsulClient.removeSingleHostCapabilityFromNode(
                        new ConsulCredential(),
                        singleHostDeploymentCapabilityWithHealthcheck,
                        basicResourceFromConsul.getId()
                );

                List<NodeService> nodeServicesAfter = consulServicesApiClient.getNodeServicesByNodeId(
                        new ConsulCredential(),
                        basicResourceFromConsul.getId()
                );
                List<CatalogNode.Check> checksOfNodeAfter = consulHealthApiClient.getChecksOfNode(
                        new ConsulCredential(),
                        basicResourceFromConsul.getId()
                );

                assertEquals(
                        nodeServicesBefore.size()-1,
                        nodeServicesAfter.size()
                );

                assertEquals(
                        checksOfNodeBefore.size()-1,
                        checksOfNodeAfter.size()
                );
            }

            @Test
            @Order(60)
            public void testBatchAddOfSingleHostDeploymentCapabilityServiceWithHealthCheckToAllResources() throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
                //Create multiple new Resources:
                for(int i = 0; i < resourceCount; i++) {
                    BasicResource resource = new BasicResource(
                            UUID.randomUUID(),
                            "Batch-Test-Host-" + (i + 1),
                            "10.0.0." + (i + 1)
                    );
                    resourcesConsulClient.addResource(resource);

                    BasicResource newResource = resourcesConsulClient.getResourceByIp(new ConsulCredential(), resource.getIp()).get();
                    batchResources.add(newResource);
                }

                //Assert all resources have no DC installed
                for(BasicResource newResource : batchResources) {
                    List<NodeService> nodeServicesBefore = consulServicesApiClient.getNodeServicesByNodeId(
                            new ConsulCredential(),
                            newResource.getId()
                    );

                    assertEquals(0, nodeServicesBefore.size());
                }

                //Install DC on all resources
                singleHostCapabilitiesConsulClient.addSingleHostCapabilityWithHealthCheckToAllConsulNodes(
                        new ConsulCredential(),
                        singleHostDeploymentCapabilityWithHealthcheck,
                        false,
                        new HashMap<>()
                );

                //Assert all resource have one DC installed
                for(BasicResource newResource : batchResources) {
                    List<NodeService> nodeServicesAfter = consulServicesApiClient.getNodeServicesByNodeId(
                            new ConsulCredential(),
                            newResource.getId()
                    );

                    assertEquals(1, nodeServicesAfter.size());
                }
            }

            @Test
            @Order(70)
            public void testGetAllDeploymentCapabilityServicesByCapabilityServiceClass() throws ConsulLoginFailedException {
                List<CapabilityService> deploymentCapabilityServices = capabilitiesConsulClient.getCapabilityServicesByCapabilityClass(
                        new ConsulCredential(),
                        DeploymentCapability.class
                );

                /**
                 * +1 => static basicResource instance
                 */
                assertEquals(
                        resourceCount+1,
                        deploymentCapabilityServices.size()
                );
            }

            @Test
            @Order(80)
            public void testBatchRemoveOfSingleHostDeploymentCapabilityServiceFromAllResources() throws ConsulLoginFailedException, ResourceNotFoundException {
                singleHostCapabilitiesConsulClient.removeCapabilityServiceFromAllConsulNodes(
                        new ConsulCredential(),
                        singleHostDeploymentCapabilityWithHealthcheck
                );

                for(BasicResource resource : batchResources) {
                    List<NodeService> nodeServicesAfter = consulServicesApiClient.getNodeServicesByNodeId(
                            new ConsulCredential(),
                            resource.getId()
                    );

                    assertEquals(0, nodeServicesAfter.size());
                }
            }

            @Test
            @Order(90)
            public void testAddSingleHostDeploymentCapabilityServiceWithoutHealthCheckWithConnection() throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
                var capabilitiesOfResourceBefore = capabilitiesConsulClient.getCapabilityServicesOfResource(
                        basicResourceFromConsul.getId()
                );

                singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                        new ConsulCredential(),
                        singleHostDeploymentCapabilityWithoutHealthcheckWithConnection,
                        basicResourceFromConsul.getId(),
                        CapabilityServiceStatus.INSTALL,
                        false,
                        new HashMap<>()
                );

                var capabilitiesOfResourceAfter = capabilitiesConsulClient.getCapabilityServicesOfResource(
                        basicResourceFromConsul.getId()
                );

                assertEquals(
                        capabilitiesOfResourceBefore.size()+1,
                        capabilitiesOfResourceAfter.size()
                );
            }

            @Test
            @Order(100)
            public void testCapabilityParameterFilteredByNonSecret() {
                Map<String, String> filteredConfigParameter = capabilityUtil.getNonSecretConfigParameter(
                        singleHostDeploymentCapabilityWithoutHealthcheckWithConfigParameter,
                        configParameter
                );

                assertEquals(1, filteredConfigParameter.size());
                assertNotNull(filteredConfigParameter.get("username"));
            }

            @Test
            @Order(110)
            public void testAddSingleHostDeploymentCapabilityServiceWithConfigParameter() throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
                CapabilityService shcsBefore = singleHostCapabilitiesConsulClient.getCapabilityServiceOfResourceByCapabilityId(
                        singleHostDeploymentCapabilityWithoutHealthcheckWithConfigParameter.getId(),
                        basicResourceFromConsul.getId()
                );

                assertNull(shcsBefore);

                singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                        new ConsulCredential(),
                        singleHostDeploymentCapabilityWithoutHealthcheckWithConfigParameter,
                        basicResourceFromConsul.getId(),
                        CapabilityServiceStatus.INSTALL,
                        false,
                        configParameter
                );

                CapabilityService shcsAfter = singleHostCapabilitiesConsulClient.getCapabilityServiceOfResourceByCapabilityId(
                        singleHostDeploymentCapabilityWithoutHealthcheckWithConfigParameter.getId(),
                        basicResourceFromConsul.getId()
                );

                assertNotNull(shcsAfter);
                assertNotNull(shcsAfter.getCustomMeta());
                assertEquals(
                        configParameter.get("username"),
                        shcsAfter.getCustomMeta().get("username")
                );
            }
        }

        @Nested
        @Order(20)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        public class testVirtualizationCapabilityServices {
            //region Variables
            private static ResourcesConsulClient staticResourcesConsulClient = null;
            private static BasicResource basicResource = new BasicResource(
                    UUID.randomUUID(),
                    "Test-Host",
                    "192.168.0.1"
            );

            private static BasicResource basicResourceFromConsul = null;
            public static VirtualizationCapability singleHostVirtualizationCapabilityWithoutHealthcheck = null;
            //endregion

            @Autowired
            public testVirtualizationCapabilityServices(
                    ResourcesConsulClient resourcesConsulClient
            ) throws ConsulLoginFailedException {
                staticResourcesConsulClient = resourcesConsulClient;

                resourcesConsulClient.addResource(
                        basicResource
                );

                basicResourceFromConsul = resourcesConsulClient.getResourceByHostname(
                        new ConsulCredential(),
                        basicResource.getHostname()
                ).get();
            }

            @BeforeAll
            public static void beforeAll() {
                List<VirtualizationCapability> virtualizationCapabilities = new ArrayList<>(2);
                singleHostVirtualizationCapabilityWithoutHealthcheck = new VirtualizationCapability(UUID.randomUUID());

                virtualizationCapabilities.add(singleHostVirtualizationCapabilityWithoutHealthcheck);

                //region init single host virtualization capability
                for(int i = 0; i < virtualizationCapabilities.size(); i++) {
                    virtualizationCapabilities.get(i).setName("Dummy");
                    virtualizationCapabilities.get(i).setLogo("mdi-dummy");
                    virtualizationCapabilities.get(i).setType(Arrays.asList(
                            CapabilityType.SETUP,
                            CapabilityType.VM
                    ));

                    virtualizationCapabilities.get(i).setCapabilityClass("VirtualizationCapability");

                    // Set AWX Capability Actions
                    var virtualizationCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-dc-dummy";
                    var virtualizationCapabilityBranch = "main";
                    virtualizationCapabilities.get(i).getActions()
                            .put(ActionType.INSTALL, new AwxAction(virtualizationCapabilityRepo, virtualizationCapabilityBranch, "install.yml"));
                    virtualizationCapabilities.get(i).getActions()
                            .put(ActionType.UNINSTALL, new AwxAction(virtualizationCapabilityRepo, virtualizationCapabilityBranch, "uninstall.yml"));
                    virtualizationCapabilities.get(i).getActions()
                            .put(ActionType.CREATE_VM, new AwxAction(virtualizationCapabilityRepo, virtualizationCapabilityBranch, "create_vm.yml"));
                    virtualizationCapabilities.get(i).getActions()
                            .put(ActionType.DELETE_VM, new AwxAction(virtualizationCapabilityRepo, virtualizationCapabilityBranch, "delete_vm.yml"));
                }
                //endregion
            }

            @BeforeEach
            public void beforeEach() {
                Mockito
                        .when(capabilityJpaRepository.findById(singleHostVirtualizationCapabilityWithoutHealthcheck.getId()))
                        .thenReturn(Optional.ofNullable(singleHostVirtualizationCapabilityWithoutHealthcheck));
            }

            @Test
            @Order(10)
            public void getSingleHostVirtualizationCapabilityOfResourceIfNoCapabilityIsInstalled() throws ConsulLoginFailedException, ResourceNotFoundException {
                var capabilitiesOfResource = capabilitiesConsulClient.getCapabilityServicesOfResource(
                        basicResourceFromConsul.getId()
                );

                assertEquals(0, capabilitiesOfResource.size());
            }

            @Test
            @Order(20)
            public void testAddSingleHostVirtualizationCapabilityServiceWithoutHealthCheck() throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
                var capabilitiesOfResourceBefore = capabilitiesConsulClient.getCapabilityServicesOfResource(
                        basicResourceFromConsul.getId()
                );

                singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                        new ConsulCredential(),
                        singleHostVirtualizationCapabilityWithoutHealthcheck,
                        basicResourceFromConsul.getId(),
                        CapabilityServiceStatus.INSTALL,
                        false,
                        new HashMap<>()
                );

                var capabilitiesOfResourceAfter = capabilitiesConsulClient.getCapabilityServicesOfResource(
                        basicResourceFromConsul.getId()
                );

                assertEquals(
                        capabilitiesOfResourceBefore.size()+1,
                        capabilitiesOfResourceAfter.size()
                );
            }

            @Test
            @Order(30)
            public void testGetAllVirtualizationCapabilityServices() throws ConsulLoginFailedException {
                List<CapabilityService> virtualizationCapabilityServices = capabilitiesConsulClient.getCapabilityServicesByCapabilityClass(
                        new ConsulCredential(),
                        VirtualizationCapability.class
                );

                assertEquals(
                    1,
                    virtualizationCapabilityServices.size()
                );
            }

            @Test
            @Order(40)
            public void testRemoveSingleHostVirtualizationCapabilityServiceWithoutHealthCheck() throws ConsulLoginFailedException, ResourceNotFoundException {
                var capabilitiesOfResourceBefore = capabilitiesConsulClient.getCapabilityServicesOfResource(
                        basicResourceFromConsul.getId()
                );

                singleHostCapabilitiesConsulClient.removeSingleHostCapabilityFromNode(
                        new ConsulCredential(),
                        singleHostVirtualizationCapabilityWithoutHealthcheck,
                        basicResourceFromConsul.getId()
                );

                var capabilitiesOfResourceAfter = capabilitiesConsulClient.getCapabilityServicesOfResource(
                        basicResourceFromConsul.getId()
                );

                assertEquals(
                        capabilitiesOfResourceBefore.size()-1,
                        capabilitiesOfResourceAfter.size()
                );
            }
        }

    }

    @Nested
    @Order(30)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class testMultiHostCapabilityServices {
        //region Variables
        @Autowired
        MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient;
        public static List<BasicResource> basicResources = new ArrayList<>();
        public static DeploymentCapability multiHostCapabilityWithoutHealthCheck = null;
        public static Map<UUID,String> memberMapping = new HashMap<>();
        public static MultiHostCapabilityService multiHostCapabilityServiceWithoutHealthCheck = null;
        public static UUID uuidOfMultiHostCapabilityService = null;
        public static MultiHostCapabilityService multiHostCapabilityService = null;
        public static BasicResource scaleUpDownResource = null;
        public static ClusterMemberType scaleableClusterMemberType = null;
        //endregion

        @BeforeAll
        public static void beforeAll() {
            multiHostCapabilityWithoutHealthCheck = new DeploymentCapability(
                    UUID.randomUUID()
            );

            multiHostCapabilityWithoutHealthCheck.setName("Docker Swarm");
            multiHostCapabilityWithoutHealthCheck.setLogo("mdi-docker-swarm");
            multiHostCapabilityWithoutHealthCheck.setType(Arrays.asList(
                    CapabilityType.SETUP,
                    CapabilityType.DEPLOY,
                    CapabilityType.SCALE
            ));

            multiHostCapabilityWithoutHealthCheck.setCapabilityClass("DeploymentCapability");

            multiHostCapabilityWithoutHealthCheck.setSupportedDeploymentTypes(Arrays.asList(
                    DeploymentType.DOCKER_CONTAINER,
                    DeploymentType.DOCKER_COMPOSE
            ));

            // Set AWX Capability Actions
            var repo = "https://github.com/FabOS-AI/fabos-slm-dc-docker-swarm.git";
            var branch = "1.0.0";
            multiHostCapabilityWithoutHealthCheck.getActions().put(ActionType.INSTALL,
                    new AwxAction(repo, branch, "install.yml"));
            multiHostCapabilityWithoutHealthCheck.getActions().put(ActionType.UNINSTALL,
                    new AwxAction(repo, branch, "uninstall.yml"));
            multiHostCapabilityWithoutHealthCheck.getActions().put(ActionType.DEPLOY,
                    new AwxAction(repo, branch, "deploy.yml"));
            multiHostCapabilityWithoutHealthCheck.getActions().put(ActionType.UNDEPLOY,
                    new AwxAction(repo, branch, "undeploy.yml"));
            multiHostCapabilityWithoutHealthCheck.getActions().put(ActionType.SCALE_UP,
                    new AwxAction(repo, branch, "scaleup.yml"));
            multiHostCapabilityWithoutHealthCheck.getActions().put(ActionType.SCALE_DOWN,
                    new AwxAction(repo, branch, "scaledown.yml"));

            multiHostCapabilityWithoutHealthCheck.setClusterMemberTypes(Arrays.asList(
                    new ClusterMemberType("Manager", "docker_manager", 3, false),
                    new ClusterMemberType("Worker","docker_worker", 1, true)
            ));

            multiHostCapabilityServiceWithoutHealthCheck = new MultiHostCapabilityService(
                    multiHostCapabilityWithoutHealthCheck,
                    memberMapping,
                    CapabilityServiceStatus.INSTALL,
                    false
            );

            scaleableClusterMemberType = multiHostCapabilityServiceWithoutHealthCheck.getCapability().getClusterMemberTypes()
                    .stream()
                    .filter(type -> type.getName().equals("Worker"))
                    .findFirst()
                    .get();
        }

        @Autowired
        public testMultiHostCapabilityServices() throws ConsulLoginFailedException {
            if(basicResources.isEmpty()) {
                int resourceCount = 5;

                for(int i = 0; i < resourceCount; i++) {
                    BasicResource basicResource = new BasicResource(
                            UUID.randomUUID(),
                            "Test-Host-"+(i+1),
                            "192.168.0."+(i+1)
                    );

                    basicResources.add(
                            resourcesConsulClient.addResource(basicResource)
                    );

                    if(i == resourceCount-2)
                        memberMapping.put(basicResources.get(i).getId(),"Worker");
                    else if(i == resourceCount-1){
                        scaleUpDownResource = resourcesConsulClient.getResourceByHostname(
                                new ConsulCredential(),
                                basicResource.getHostname()
                        ).get();
                    }
                    else
                        memberMapping.put(basicResources.get(i).getId(), "Manager");
                }
            }

            Mockito
                    .when(capabilityJpaRepository.findById(multiHostCapabilityWithoutHealthCheck.getId()))
                    .thenReturn(Optional.ofNullable(multiHostCapabilityWithoutHealthCheck));
        }

        @Test
        @Order(10)
        public void testGetMultiHostCapabilityOfResourceIfNoServiceRegistered() throws ConsulLoginFailedException, ResourceNotFoundException {
            for(BasicResource b : basicResources) {
                var dcs = capabilitiesConsulClient.getCapabilityServicesOfResource(
                        b.getId()
                );

                assertEquals(0, dcs.size());
            }
        }

        @Test
        @Order(20)
        public void testGetMultiHostCapabilityServicesOfUserIfNoServiceRegistered() throws ConsulLoginFailedException {
            List<MultiHostCapabilityService> multiHostCapabilityServices = multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
                    new ConsulCredential()
            );

            assertEquals(0, multiHostCapabilityServices.size());
        }

        @Test
        @Order(30)
        public void testAddMultiHostCapabilityServiceWithoutHealthCheck() throws ConsulLoginFailedException {
            List<MultiHostCapabilityService> multiHostCapabilityServicesBefore = multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
                    new ConsulCredential()
            );

            multiHostCapabilityServiceWithoutHealthCheck.setStatus(CapabilityServiceStatus.INSTALL);

            multiHostCapabilitiesConsulClient.addMultiHostCapabilityService(
                    new ConsulCredential(),
                    multiHostCapabilityServiceWithoutHealthCheck
            );

            List<MultiHostCapabilityService> multiHostCapabilityServicesAfter = multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
                    new ConsulCredential()
            );

            assertEquals(
                    multiHostCapabilityServicesBefore.size()+1,
                    multiHostCapabilityServicesAfter.size()
            );

            MultiHostCapabilityService mhcsAfter = multiHostCapabilityServicesAfter.get(0);

            HashMap<UUID, CatalogNode.Service> nodeServiceMap = mhcsAfter.getMapOfNodeIdsAndCatalogServices();

            assertEquals(
                    CapabilityServiceStatus.INSTALL,
                    multiHostCapabilityServicesAfter.get(0).getStatus()
            );

            for(var entry: nodeServiceMap.entrySet()) {
                HashMap<String, String> serviceMeta = mhcsAfter.getServiceMetaByNodeId(entry.getKey());

                assertEquals(
                        CapabilityServiceStatus.INSTALL.name(),
                        serviceMeta.get(CapabilityService.META_KEY_STATUS)
                );
            }
        }

        @Test
        @Order(40)
        public void testGetMultiHostCapabilityServicesOfUserIfOneServiceIsRegistered() throws ConsulLoginFailedException, ResourceNotFoundException {
            List<MultiHostCapabilityService> multiHostCapabilityIfOneServiceIsRegistered = multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
                    new ConsulCredential()
            );

            assertEquals(
                    1,
                    multiHostCapabilityIfOneServiceIsRegistered.size()
            );

            uuidOfMultiHostCapabilityService = multiHostCapabilityIfOneServiceIsRegistered.get(0).getId();
        }

        @Test
        @Order(50)
        public void testGetMultiHostCapabilityServiceOfUserByServiceIdIfOneServiceIsRegistered() throws ConsulLoginFailedException {
            Optional<MultiHostCapabilityService> optionalMultiHostCapabilityService = multiHostCapabilitiesConsulClient.getMultiHostCapabilityServiceOfUser(
                    new ConsulCredential(),
                    uuidOfMultiHostCapabilityService
            );

            assertTrue(optionalMultiHostCapabilityService.isPresent());
            multiHostCapabilityService = optionalMultiHostCapabilityService.get();
        }

        @Test
        @Order(55)
        public void testUpdateStatusMultiHostCapabilityServiceIfOneServiceIsRegistered() throws ConsulLoginFailedException {
            CapabilityServiceStatus newStatus = CapabilityServiceStatus.READY;

            multiHostCapabilityService.setStatus(newStatus);

            multiHostCapabilitiesConsulClient.updateMultiHostCapabilityService(
                    new ConsulCredential(),
                    multiHostCapabilityService
            );

            Optional<MultiHostCapabilityService> optionalMultiHostCapabilityService = multiHostCapabilitiesConsulClient.getMultiHostCapabilityServiceOfUser(
                    new ConsulCredential(),
                    uuidOfMultiHostCapabilityService
            );

            assertTrue(optionalMultiHostCapabilityService.isPresent());
            MultiHostCapabilityService mhcsAfter = optionalMultiHostCapabilityService.get();

            assertEquals(newStatus, mhcsAfter.getStatus());

            for(var entry: mhcsAfter.getMapOfNodeIdsAndCatalogServices().entrySet()) {
                String newStatusFromMeta = mhcsAfter
                        .getServiceMetaByNodeId(entry.getKey())
                        .get(CapabilityService.META_KEY_STATUS);

                assertEquals(
                        newStatus.name(),
                        newStatusFromMeta
                );
            }
        }

        @Test
        @Order(60)
        public void testGetMultiHostCapabilityServiceByNameIfOneServiceIsRegistered() throws ConsulLoginFailedException, ResourceNotFoundException {
            List<CatalogService> catalogServices = multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
                    new ConsulCredential(),
                    multiHostCapabilityServiceWithoutHealthCheck.getService()
            );

            assertEquals(
                    memberMapping.size(),
                    catalogServices.size()
            );

            for(UUID resourceId : memberMapping.keySet()) {
                assertTrue(
                        catalogServices.stream()
                                .filter(
                                        cs -> cs.getId().equals(resourceId)
                                ).findFirst()
                                .isPresent()
                );
            }
        }

        @Test
        @Order(65)
        public void testGetMultiHostCapabilityServiceByResourceIdIfOneServiceIsRegistered() throws ConsulLoginFailedException, ResourceNotFoundException {
            // Only until size-1 because last resource is for scale tests and not part of the cluster at this point
            for(int i = 0; i < basicResources.size()-1; i++) {
                BasicResource basicResource = basicResources.get(i);
                List<MultiHostCapabilityService> mhcsList = multiHostCapabilitiesConsulClient.getMultiHostCapabilityServicesOfResource(
                        new ConsulCredential(),
                        basicResource.getId()
                );
                assertEquals(1, mhcsList.size());
            }
        }


        @Test
        @Order(70)
        public void testGetMultiHostCapabilityOfResourceIfOneServiceRegistered() throws ConsulLoginFailedException, ResourceNotFoundException {
            for(int i = 0; i < basicResources.size()-1; i++) {
                BasicResource basicResource = basicResources.get(i);
                var installedCapabilityServices = capabilitiesConsulClient.getCapabilityServicesOfResource(
                        basicResource.getId()
                );

                assertEquals(1, installedCapabilityServices.size());
                assertEquals(
                        multiHostCapabilityWithoutHealthCheck.getId(),
                        installedCapabilityServices.get(0).getCapability().getId()
                );
            }
        }

        @Test
        @Order(80)
        public void testGetNodesOfMultiHostCapabilityService() throws ConsulLoginFailedException {
            List<MultiHostCapabilityService> multiHostCapabilityServices = multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
                    new ConsulCredential()
            );

            MultiHostCapabilityService multiHostCapabilityService = multiHostCapabilityServices.get(0);

            List<Node> nodesOfMultiHostCapabilityService = multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
                    new ConsulCredential(),
                    multiHostCapabilityService.getId()
            );

            assertEquals(
                    basicResources.size()-1,
                    nodesOfMultiHostCapabilityService.size()
            );
        }

        @Test
        @Order(90)
        public void testScaleUpMultiHostCapabilityService() throws ConsulLoginFailedException {
            List<Node> nodesOfServiceBefore = multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
                    new ConsulCredential(),
                    uuidOfMultiHostCapabilityService
            );

            multiHostCapabilitiesConsulClient.scaleMultiHostCapabilityService(
                    new ConsulCredential(),
                    new ScaleUpOperation(
                            scaleUpDownResource.getId(),
                            scaleableClusterMemberType
                    ),
                    uuidOfMultiHostCapabilityService
            );

            List<Node> nodesOfServiceAfter = multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
                    new ConsulCredential(),
                    uuidOfMultiHostCapabilityService
            );

            assertEquals(
                    nodesOfServiceBefore.size()+1,
                    nodesOfServiceAfter.size()
            );
        }

        @Test
        @Order(100)
        public void testScaleDownMultiHostCapabilityService() throws ConsulLoginFailedException {
            List<Node> nodesOfServiceBefore = multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
                    new ConsulCredential(),
                    uuidOfMultiHostCapabilityService
            );

            ScaleDownOperation scaleDownOperation = new ScaleDownOperation(
                    scaleUpDownResource.getId()
            );

            multiHostCapabilitiesConsulClient.scaleMultiHostCapabilityService(
                    new ConsulCredential(),
                    scaleDownOperation,
                    uuidOfMultiHostCapabilityService
            );

            List<Node> nodesOfServiceAfter = multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
                    new ConsulCredential(),
                    uuidOfMultiHostCapabilityService
            );

            assertEquals(
                    nodesOfServiceBefore.size()-1,
                    nodesOfServiceAfter.size()
            );
        }

        @Test
        @Order(110)
        public void testRemoveMultiHostCapabilityServiceWithoutHealthCheck() throws ConsulLoginFailedException {
            List<MultiHostCapabilityService> multiHostCapabilityServicesBefore = multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
                    new ConsulCredential()
            );

            multiHostCapabilitiesConsulClient.removeMultiHostCapabilityService(
                    new ConsulCredential(),
                    uuidOfMultiHostCapabilityService
            );

            List<MultiHostCapabilityService> multiHostCapabilityServicesAfter = multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
                    new ConsulCredential()
            );

            assertEquals(
                    multiHostCapabilityServicesBefore.size()-1,
                    multiHostCapabilityServicesAfter.size()
            );
        }
    }
}
