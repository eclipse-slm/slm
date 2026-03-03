//package org.eclipse.slm.resource_management.features.capabilities;
//
//
//import org.eclipse.slm.common.consul.model.catalog.Service;
//import org.eclipse.slm.common.consul.model.catalog.Node;
//import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
//import org.eclipse.slm.common.consul.testing.containers.ConsulTestContainer;
//import org.eclipse.slm.common.model.DeploymentType;
//import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
//import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
//import org.eclipse.slm.resource_management.common.resources.BasicResource;
//import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilitiesConsulClient;
//import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
//import org.eclipse.slm.resource_management.features.capabilities.clusters.ScaleDownOperation;
//import org.eclipse.slm.resource_management.features.capabilities.clusters.ScaleUpOperation;
//import org.eclipse.slm.resource_management.features.capabilities.clusters.model.ClusterMemberType;
//import org.eclipse.slm.resource_management.features.capabilities.model.*;
//import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
//import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
//import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
//import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
//import org.junit.jupiter.api.*;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@TestClassOrder(ClassOrderer.OrderAnnotation.class)
//@Testcontainers
//public class MultiHostCapabilitiesConsulClientTest {
//    private final static Logger LOG = LoggerFactory.getLogger(MultiHostCapabilitiesConsulClientTest.class);
//
//    @Container
//    private final static ConsulTestContainer consulContainer = new ConsulTestContainer();
//
//    private static final String TEST_USER_ID = UUID.randomUUID().toString();
//    private static final String TEST_GROUP_ID = "/users/" + TEST_USER_ID;
//
//    private static CapabilitiesConsulClient capabilitiesConsulClient;
//
//    private static MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient;
//
//    @Mock
//    private static CapabilityJpaRepository capabilityJpaRepository;
//
//    private static ResourcesConsulClient resourcesConsulClient;
//    private static CapabilityUtil capabilityUtil;
//
//    @BeforeAll
//    public static void beforeAll() {
//        capabilityJpaRepository = Mockito.mock(CapabilityJpaRepository.class);
//
//        var consulTestInitializer = new ConsulTestContainerInitializer(consulContainer, false);
//        consulTestInitializer.initUserGroup(TEST_GROUP_ID);
//
//        capabilityUtil = new CapabilityUtil(
//                capabilityJpaRepository,
//                consulContainer.getConsulNodesApiClient(),
//                consulContainer.getConsulServicesApiClient());
//
//        capabilitiesConsulClient = new CapabilitiesConsulClient(
//                consulContainer.getConsulServicesApiClient(),
//                consulContainer.getConsulNodesApiClient(),
//                capabilityUtil
//        );
//
//        resourcesConsulClient = new ResourcesConsulClient(
//                consulContainer.getConsulNodesApiClient(),
//                consulContainer.getConsulAclApiClient(),
//                consulContainer.getConsulServicesApiClient(),
//                consulContainer.getConsulGenericNodeRemoveClient()
//        );
//
//        multiHostCapabilitiesConsulClient = new MultiHostCapabilitiesConsulClient(
//                capabilityJpaRepository,
//                consulContainer.getConsulNodesApiClient(),
//                consulContainer.getConsulServicesApiClient(),
//                consulContainer.getConsulGenericServicesClient(),
//                consulContainer.getConsulAclApiClient(),
//                consulContainer.getConsulGenericNodeRemoveClient(),
//                capabilityUtil
//        );
//    }
//
//    @Nested
//    @Order(30)
//    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//    public class testMultiHostCapabilityServices {
//        //region Variables
//        public static List<BasicResource> basicResources = new ArrayList<>();
//        public static DeploymentCapability multiHostCapabilityWithoutHealthCheck = null;
//        public static Map<UUID,String> memberMapping = new HashMap<>();
//        public static MultiHostCapabilityService multiHostCapabilityServiceWithoutHealthCheck = null;
//        public static UUID uuidOfMultiHostCapabilityService = null;
//        public static MultiHostCapabilityService multiHostCapabilityService = null;
//        public static BasicResource scaleUpDownResource = null;
//        public static ClusterMemberType scaleableClusterMemberType = null;
//        //endregion
//
//        @BeforeAll
//        public static void beforeAll() {
//            multiHostCapabilityWithoutHealthCheck = new DeploymentCapability(
//                    UUID.randomUUID()
//            );
//
//            multiHostCapabilityWithoutHealthCheck.setName("Docker Swarm");
//            multiHostCapabilityWithoutHealthCheck.setLogo("mdi-docker-swarm");
//            multiHostCapabilityWithoutHealthCheck.setType(Arrays.asList(
//                    CapabilityType.SETUP,
//                    CapabilityType.DEPLOY,
//                    CapabilityType.SCALE
//            ));
//
//            multiHostCapabilityWithoutHealthCheck.setCapabilityClass("DeploymentCapability");
//
//            multiHostCapabilityWithoutHealthCheck.setSupportedDeploymentTypes(Arrays.asList(
//                    DeploymentType.DOCKER_CONTAINER,
//                    DeploymentType.DOCKER_COMPOSE
//            ));
//
//            // Set AWX Capability Actions
//            var repo = "https://github.com/FabOS-AI/fabos-slm-dc-docker-swarm.git";
//            var branch = "1.0.0";
//            multiHostCapabilityWithoutHealthCheck.getActions().put(ActionType.INSTALL,
//                    new AwxAction(repo, branch, "install.yml"));
//            multiHostCapabilityWithoutHealthCheck.getActions().put(ActionType.UNINSTALL,
//                    new AwxAction(repo, branch, "uninstall.yml"));
//            multiHostCapabilityWithoutHealthCheck.getActions().put(ActionType.DEPLOY,
//                    new AwxAction(repo, branch, "deploy.yml"));
//            multiHostCapabilityWithoutHealthCheck.getActions().put(ActionType.UNDEPLOY,
//                    new AwxAction(repo, branch, "undeploy.yml"));
//            multiHostCapabilityWithoutHealthCheck.getActions().put(ActionType.SCALE_UP,
//                    new AwxAction(repo, branch, "scaleup.yml"));
//            multiHostCapabilityWithoutHealthCheck.getActions().put(ActionType.SCALE_DOWN,
//                    new AwxAction(repo, branch, "scaledown.yml"));
//
//            multiHostCapabilityWithoutHealthCheck.setClusterMemberTypes(Arrays.asList(
//                    new ClusterMemberType("Manager", "docker_manager", 3, false),
//                    new ClusterMemberType("Worker","docker_worker", 1, true)
//            ));
//
//            multiHostCapabilityServiceWithoutHealthCheck = new MultiHostCapabilityService(
//                    multiHostCapabilityWithoutHealthCheck,
//                    memberMapping,
//                    CapabilityServiceStatus.INSTALL,
//                    false,
//                    TEST_GROUP_ID
//            );
//
//            scaleableClusterMemberType = multiHostCapabilityServiceWithoutHealthCheck.getCapability().getClusterMemberTypes()
//                    .stream()
//                    .filter(type -> type.getName().equals("Worker"))
//                    .findFirst()
//                    .get();
//        }
//
//        @Autowired
//        public testMultiHostCapabilityServices() throws ConsulLoginFailedException {
//            if(basicResources.isEmpty()) {
//                int resourceCount = 5;
//
//                for(int i = 0; i < resourceCount; i++) {
//                    BasicResource basicResource = new BasicResource(
//                            UUID.randomUUID(),
//                            "Test-Host-"+(i+1),
//                            "192.168.0."+(i+1)
//                    );
//
//                    basicResources.add(
//                            resourcesConsulClient.addResource(basicResource, TEST_GROUP_ID)
//                    );
//
//                    if(i == resourceCount-2)
//                        memberMapping.put(basicResources.get(i).getId(),"Worker");
//                    else if(i == resourceCount-1){
//                        scaleUpDownResource = resourcesConsulClient.getResourceByIdOrThrow( basicResource.getId());
//                    }
//                    else
//                        memberMapping.put(basicResources.get(i).getId(), "Manager");
//                }
//            }
//
//            Mockito
//                    .when(capabilityJpaRepository.findById(multiHostCapabilityWithoutHealthCheck.getId()))
//                    .thenReturn(Optional.ofNullable(multiHostCapabilityWithoutHealthCheck));
//        }
//
//        @Test
//        @Order(10)
//        public void testGetMultiHostCapabilityOfResourceIfNoServiceRegistered() throws ConsulLoginFailedException, ResourceNotFoundException {
//            for(BasicResource b : basicResources) {
//                var dcs = capabilitiesConsulClient.getCapabilityServicesOfResource(
//                        b.getId()
//                );
//
//                assertEquals(0, dcs.size());
//            }
//        }
//
//        @Test
//        @Order(20)
//        public void testGetMultiHostCapabilityServicesOfUserIfNoServiceRegistered() throws ConsulLoginFailedException {
//            List<MultiHostCapabilityService> multiHostCapabilityServices = multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
//
//            );
//
//            assertEquals(0, multiHostCapabilityServices.size());
//        }
//
//        @Test
//        @Order(30)
//        public void testAddMultiHostCapabilityServiceWithoutHealthCheck() throws ConsulLoginFailedException {
//            List<MultiHostCapabilityService> multiHostCapabilityServicesBefore = multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
//
//            );
//
//            multiHostCapabilityServiceWithoutHealthCheck.setStatus(CapabilityServiceStatus.INSTALL);
//
//            multiHostCapabilitiesConsulClient.addMultiHostCapabilityService(
//
//                    multiHostCapabilityServiceWithoutHealthCheck
//            );
//
//            List<MultiHostCapabilityService> multiHostCapabilityServicesAfter = multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
//
//            );
//
//            assertEquals(
//                    multiHostCapabilityServicesBefore.size()+1,
//                    multiHostCapabilityServicesAfter.size()
//            );
//
//            MultiHostCapabilityService mhcsAfter = multiHostCapabilityServicesAfter.get(0);
//
//            HashMap<UUID, CatalogNode.Service> nodeServiceMap = mhcsAfter.getMapOfNodeIdsAndCatalogServices();
//
//            assertEquals(
//                    CapabilityServiceStatus.INSTALL,
//                    multiHostCapabilityServicesAfter.get(0).getStatus()
//            );
//
//            for(var entry: nodeServiceMap.entrySet()) {
//                HashMap<String, String> serviceMeta = mhcsAfter.getServiceMetaByNodeId(entry.getKey());
//
//                assertEquals(
//                        CapabilityServiceStatus.INSTALL.name(),
//                        serviceMeta.get(CapabilityService.META_KEY_STATUS)
//                );
//            }
//        }
//
//        @Test
//        @Order(40)
//        public void testGetMultiHostCapabilityServicesOfUserIfOneServiceIsRegistered() throws ConsulLoginFailedException, ResourceNotFoundException {
//            List<MultiHostCapabilityService> multiHostCapabilityIfOneServiceIsRegistered = multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
//
//            );
//
//            assertEquals(
//                    1,
//                    multiHostCapabilityIfOneServiceIsRegistered.size()
//            );
//
//            uuidOfMultiHostCapabilityService = multiHostCapabilityIfOneServiceIsRegistered.get(0).getId();
//        }
//
//        @Test
//        @Order(50)
//        public void testGetMultiHostCapabilityServiceOfUserByServiceIdIfOneServiceIsRegistered() throws ConsulLoginFailedException {
//            Optional<MultiHostCapabilityService> optionalMultiHostCapabilityService = multiHostCapabilitiesConsulClient.getMultiHostCapabilityServiceOfUser(
//
//                    uuidOfMultiHostCapabilityService
//            );
//
//            assertTrue(optionalMultiHostCapabilityService.isPresent());
//            multiHostCapabilityService = optionalMultiHostCapabilityService.get();
//        }
//
//        @Test
//        @Order(55)
//        public void testUpdateStatusMultiHostCapabilityServiceIfOneServiceIsRegistered() throws ConsulLoginFailedException {
//            CapabilityServiceStatus newStatus = CapabilityServiceStatus.READY;
//
//            multiHostCapabilityService.setStatus(newStatus);
//
//            multiHostCapabilitiesConsulClient.updateMultiHostCapabilityService(
//
//                    multiHostCapabilityService
//            );
//
//            Optional<MultiHostCapabilityService> optionalMultiHostCapabilityService = multiHostCapabilitiesConsulClient.getMultiHostCapabilityServiceOfUser(
//
//                    uuidOfMultiHostCapabilityService
//            );
//
//            assertTrue(optionalMultiHostCapabilityService.isPresent());
//            MultiHostCapabilityService mhcsAfter = optionalMultiHostCapabilityService.get();
//
//            assertEquals(newStatus, mhcsAfter.getStatus());
//
//            for(var entry: mhcsAfter.getMapOfNodeIdsAndCatalogServices().entrySet()) {
//                String newStatusFromMeta = mhcsAfter
//                        .getServiceMetaByNodeId(entry.getKey())
//                        .get(CapabilityService.META_KEY_STATUS);
//
//                assertEquals(
//                        newStatus.name(),
//                        newStatusFromMeta
//                );
//            }
//        }
//
//        @Test
//        @Order(60)
//        public void testGetMultiHostCapabilityServiceByNameIfOneServiceIsRegistered() throws ConsulLoginFailedException, ResourceNotFoundException {
//            List<Service> services = multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
//
//                    multiHostCapabilityServiceWithoutHealthCheck.getService()
//            );
//
//            assertEquals(
//                    memberMapping.size(),
//                    services.size()
//            );
//
//            for(UUID resourceId : memberMapping.keySet()) {
//                assertTrue(
//                        services.stream()
//                                .filter(
//                                        cs -> cs.getNodeId().equals(resourceId)
//                                ).findFirst()
//                                .isPresent()
//                );
//            }
//        }
//
//        @Test
//        @Order(65)
//        public void testGetMultiHostCapabilityServiceByResourceIdIfOneServiceIsRegistered() throws ConsulLoginFailedException, ResourceNotFoundException {
//            // Only until size-1 because last resource is for scale tests and not part of the cluster at this point
//            for(int i = 0; i < basicResources.size()-1; i++) {
//                BasicResource basicResource = basicResources.get(i);
//                List<MultiHostCapabilityService> mhcsList = multiHostCapabilitiesConsulClient.getMultiHostCapabilityServicesOfResource(
//
//                        basicResource.getId()
//                );
//                assertEquals(1, mhcsList.size());
//            }
//        }
//
//
//        @Test
//        @Order(70)
//        public void testGetMultiHostCapabilityOfResourceIfOneServiceRegistered() throws ConsulLoginFailedException, ResourceNotFoundException {
//            for(int i = 0; i < basicResources.size()-1; i++) {
//                BasicResource basicResource = basicResources.get(i);
//                var installedCapabilityServices = capabilitiesConsulClient.getCapabilityServicesOfResource(
//                        basicResource.getId()
//                );
//
//                assertEquals(1, installedCapabilityServices.size());
//                assertEquals(
//                        multiHostCapabilityWithoutHealthCheck.getId(),
//                        installedCapabilityServices.get(0).getCapability().getId()
//                );
//            }
//        }
//
//        @Test
//        @Order(80)
//        public void testGetNodesOfMultiHostCapabilityService() throws ConsulLoginFailedException {
//            List<MultiHostCapabilityService> multiHostCapabilityServices = multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
//
//            );
//
//            MultiHostCapabilityService multiHostCapabilityService = multiHostCapabilityServices.get(0);
//
//            List<Node> nodesOfMultiHostCapabilityService = multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
//
//                    multiHostCapabilityService.getId()
//            );
//
//            assertEquals(
//                    basicResources.size()-1,
//                    nodesOfMultiHostCapabilityService.size()
//            );
//        }
//
//        @Test
//        @Order(90)
//        public void testScaleUpMultiHostCapabilityService() throws ConsulLoginFailedException {
//            List<Node> nodesOfServiceBefore = multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
//
//                    uuidOfMultiHostCapabilityService
//            );
//
//            multiHostCapabilitiesConsulClient.scaleMultiHostCapabilityService(
//
//                    new ScaleUpOperation(
//                            scaleUpDownResource.getId(),
//                            scaleableClusterMemberType
//                    ),
//                    uuidOfMultiHostCapabilityService
//            );
//
//            List<Node> nodesOfServiceAfter = multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
//
//                    uuidOfMultiHostCapabilityService
//            );
//
//            assertEquals(
//                    nodesOfServiceBefore.size()+1,
//                    nodesOfServiceAfter.size()
//            );
//        }
//
//        @Test
//        @Order(100)
//        public void testScaleDownMultiHostCapabilityService() throws ConsulLoginFailedException {
//            List<Node> nodesOfServiceBefore = multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
//
//                    uuidOfMultiHostCapabilityService
//            );
//
//            ScaleDownOperation scaleDownOperation = new ScaleDownOperation(
//                    scaleUpDownResource.getId()
//            );
//
//            multiHostCapabilitiesConsulClient.scaleMultiHostCapabilityService(
//
//                    scaleDownOperation,
//                    uuidOfMultiHostCapabilityService
//            );
//
//            List<Node> nodesOfServiceAfter = multiHostCapabilitiesConsulClient.getNodesOfMultiHostCapabilityService(
//
//                    uuidOfMultiHostCapabilityService
//            );
//
//            assertEquals(
//                    nodesOfServiceBefore.size()-1,
//                    nodesOfServiceAfter.size()
//            );
//        }
//
//        @Test
//        @Order(110)
//        public void testRemoveMultiHostCapabilityServiceWithoutHealthCheck() throws ConsulLoginFailedException {
//            List<MultiHostCapabilityService> multiHostCapabilityServicesBefore = multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
//
//            );
//
//            multiHostCapabilitiesConsulClient.removeMultiHostCapabilityService(
//
//                    uuidOfMultiHostCapabilityService
//            );
//
//            List<MultiHostCapabilityService> multiHostCapabilityServicesAfter = multiHostCapabilitiesConsulClient.getMultiHostCapabilitiesServicesOfUser(
//
//            );
//
//            assertEquals(
//                    multiHostCapabilityServicesBefore.size()-1,
//                    multiHostCapabilityServicesAfter.size()
//            );
//        }
//    }
//}
