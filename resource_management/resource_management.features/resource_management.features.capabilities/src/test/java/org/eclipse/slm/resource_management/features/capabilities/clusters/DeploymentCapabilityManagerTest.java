//package org.eclipse.slm.resource_management.service.rest.handler;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import org.eclipse.slm.common.awx.client.AwxClient;
//import org.eclipse.slm.common.awx.client.AwxCredential;
//import org.eclipse.slm.common.awx.client.AwxProjectUpdateFailedException;
//import org.eclipse.slm.common.awx.client.observer.AwxJobObserver;
//import org.eclipse.slm.common.awx.client.observer.AwxJobObserverInitializer;
//import org.eclipse.slm.common.awx.model.CredentialDTOApiCreate;
//import org.eclipse.slm.common.awx.model.CredentialTypeDTOApiCreate;
//import org.eclipse.slm.common.awx.model.CredentialTypeInputFieldItem;
//import org.eclipse.slm.common.consul.client.ConsulCredential;
//import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
//import org.eclipse.slm.common.consul.client.apis.ConsulHealthApiClient;
//import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
//import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
//import org.eclipse.slm.common.consul.model.acl.Policy;
//import org.eclipse.slm.common.consul.model.catalog.NodeService;
//import org.eclipse.slm.common.vault.client.VaultCredential;
//import org.eclipse.slm.resource_management.model.capabilities.CapabilityNotFoundException;
//import org.eclipse.slm.resource_management.model.capabilities.actions.AwxCapabilityAction;
//import org.eclipse.slm.resource_management.model.capabilities.actions.CapabilityActionType;
//import org.eclipse.slm.resource_management.model.cluster.ClusterCreateRequest;
//import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
//import org.eclipse.slm.resource_management.model.consul.capability.MultiHostCapabilityService;
//import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
//import org.eclipse.slm.common.keycloak.config.KeycloakUtil;
//import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
//import org.eclipse.slm.common.model.DeploymentType;
//import org.eclipse.slm.common.vault.client.VaultClient;
//import org.eclipse.slm.notification_service.model.JobFinalState;
//import org.eclipse.slm.notification_service.model.JobGoal;
//import org.eclipse.slm.notification_service.model.JobTarget;
//import org.eclipse.slm.notification_service.messaging.NotificationMessageSender;
//import org.eclipse.slm.resource_management.model.resource.ConnectionType;
//import org.eclipse.slm.resource_management.model.resource.CredentialUsernamePassword;
//import org.eclipse.slm.resource_management.common.model.ResourceNotFoundException;
//import org.eclipse.slm.resource_management.common.resources.BasicResource;
//import org.eclipse.slm.resource_management.model.capabilities.Capability;
//import org.eclipse.slm.resource_management.model.capabilities.CapabilityType;
//import org.eclipse.slm.resource_management.model.capabilities.DeploymentCapability;
//import org.eclipse.slm.resource_management.model.cluster.ClusterMemberType;
//import org.eclipse.slm.resource_management.common.persistence.CapabilityJpaRepository;
//import org.eclipse.slm.resource_management.features.capabilities.CapabilitiesManager;
//import org.eclipse.slm.resource_management.common.ResourcesManager;
//import org.eclipse.slm.resource_management.service.rest.resources.ResourcesRestControllerITHelper;
//import org.junit.jupiter.api.*;
//import org.keycloak.KeycloakPrincipal;
//import org.keycloak.KeycloakSecurityContext;
//import org.keycloak.representations.AccessToken;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.FilterType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.web.client.RestTemplate;
//import org.testcontainers.containers.DockerComposeContainer;
//import org.testcontainers.containers.wait.strategy.Wait;
//
//import javax.net.ssl.SSLException;
//import java.io.File;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@ComponentScan(basePackages = {
//        "org.eclipse.slm.resource_management.service.rest.capabilities",
//        "org.eclipse.slm.resource_management.service.rest.resources",
//        "org.eclipse.slm.resource_management.service.rest.handler",
//        "org.eclipse.slm.resource_management.service.rest.manager",
//        "org.eclipse.slm.resource_management.service.rest.consul",
//        "org.eclipse.slm.common.consul.client",
//        "org.eclipse.slm.common.awx.client",
//        "org.eclipse.slm.common.vault.client",
//        "org.eclipse.slm.common.awx.client.observer",
//        "org.eclipse.slm.notification_service.service.client"
//},
//        excludeFilters = @ComponentScan.Filter(
//                type = FilterType.ASSIGNABLE_TYPE,
//                value = {
//                        ResourcesRestControllerITHelper.class,
//                })
//)
//@AutoConfigureWebClient(registerRestTemplate = true)
//@ActiveProfiles("test")
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@Disabled
//public class DeploymentCapabilityManagerTest {
//
//    //region Variables
//    public static final String keycloakDummyToken = "";
//    private final static Logger LOG = LoggerFactory.getLogger(DeploymentCapabilityManagerTest.class);
//    @Autowired
//    public CapabilitiesManager capabilitiesManager;
//    @Autowired
//    public ClusterHandler clusterHandler;
//    @Autowired
//    public ClusterCreateFunctions clusterCreateFunctions;
//    DeploymentCapability dockerDeploymentCapability;
//    DeploymentCapability dockerSwarmDeploymentCapability;
//    @Autowired
//    ResourcesManager resourcesManager;
//    @Autowired
//    ConsulNodesApiClient consulNodesApiClient;
//    @Autowired
//    ConsulAclApiClient consulAclApiClient;
//    @Autowired
//    ConsulServicesApiClient consulServiceApiClient;
//    @Autowired
//    ConsulHealthApiClient consulHealthApiClient;
//    @Autowired
//    RestTemplate restTemplate;
//    @Autowired
//    AwxClient awxClient;
//    @Autowired
//    VaultClient vaultClient;
//    @Autowired
//    CapabilityJpaRepository capabilityJpaRepository;
//    @Autowired
//    private AwxJobObserverInitializer awxJobObserverInitializer;
//    @MockBean
//    NotificationMessageSender notificationMessageSender;
//    @MockBean
//    MultiTenantKeycloakRegistration multiTenantKeycloakRegistration;
//    @MockBean
//    KeycloakUtil keycloakUtil;
//    KeycloakPrincipal keycloakPrincipal;
//    private static CredentialUsernamePassword credentialUsernamePassword = new CredentialUsernamePassword("username", "password");
//    static DockerComposeContainer dockerComposeWorkload;
//
//    private static int AWX_PORT = 8013;
//    private static String AWX_WEB_SERVICE = "awx-task";
//
//    private AwxCredential awxCredential = new AwxCredential("admin", "password");
//
//    private static int CONSUL_PORT = 8500;
//    private static String CONSUL_SERVICE = "consul";
//
//    private ConsulCredential consulCredential = new ConsulCredential("");
//
//    private static int VAULT_PORT = 8200;
//    private static String VAULT_SERVICE = "vault";
//
//    private static boolean START_WORKLOAD = true;
//    //endregion
//
//    @BeforeAll
//    public void beforeAll() throws JsonProcessingException {
//        var accessToken = new AccessToken();
//        accessToken.setSubject(UUID.randomUUID().toString());
//        accessToken.issuer("fabos");
//        this.keycloakPrincipal = new KeycloakPrincipal<>("testUser", new KeycloakSecurityContext("", accessToken, "", null));
//
//        //region Create Java Object of Docker DeploymentCapability:
//        dockerDeploymentCapability = new DeploymentCapability();
//        dockerDeploymentCapability.setName("Docker");
//        dockerDeploymentCapability.setLogo("mdi-docker");
//        dockerDeploymentCapability.setType(Arrays.asList(
//                CapabilityType.SETUP,
//                CapabilityType.DEPLOY
//        ));
//        dockerDeploymentCapability.setCapabilityClass("DeploymentCapability");
//
//        dockerDeploymentCapability.setSupportedDeploymentTypes(Arrays.asList(
//                DeploymentType.DOCKER_CONTAINER,
//                DeploymentType.DOCKER_COMPOSE
//        ));
//
//        // Set AWX Capability Actions
//        var dockerDeploymentCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-dc-dummy.git";
//        var dockerDeploymentCapabilityBranch = "main";
//        dockerDeploymentCapability.getActions()
//                .put(CapabilityActionType.INSTALL, new AwxCapabilityAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "install.yml"));
//        dockerDeploymentCapability.getActions()
//                .put(CapabilityActionType.UNINSTALL, new AwxCapabilityAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "uninstall.yml"));
//        dockerDeploymentCapability.getActions()
//                .put(CapabilityActionType.DEPLOY, new AwxCapabilityAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "deploy.yml"));
//        dockerDeploymentCapability.getActions()
//                .put(CapabilityActionType.UNDEPLOY, new AwxCapabilityAction(dockerDeploymentCapabilityRepo, dockerDeploymentCapabilityBranch, "undeploy.yml"));
//        //endregion
//
//        //region Create Java Object of Docker Swarm DeploymentCapability:
//        dockerSwarmDeploymentCapability = new DeploymentCapability();
//        dockerSwarmDeploymentCapability.setName("Docker-Swarm");
//        dockerSwarmDeploymentCapability.setLogo("mdi-docker-swarm");
//        dockerSwarmDeploymentCapability.setType(Arrays.asList(
//                CapabilityType.SETUP,
//                CapabilityType.DEPLOY,
//                CapabilityType.SCALE
//        ));
//        dockerSwarmDeploymentCapability.setCapabilityClass("DeploymentCapability");
//
//        dockerSwarmDeploymentCapability.setSupportedDeploymentTypes(Arrays.asList(
//                DeploymentType.DOCKER_CONTAINER,
//                DeploymentType.DOCKER_COMPOSE
//        ));
//
//        // Set AWX Capability Actions
//        var dockerSwarmDeploymentCapabilityRepo = "https://github.com/FabOS-AI/fabos-slm-dc-dummy.git";
//        var dockerSwarmDeploymentCapabilityBranch = "main";
//        dockerSwarmDeploymentCapability.getActions().put(CapabilityActionType.INSTALL,
//                new AwxCapabilityAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "install.yml"));
//        dockerSwarmDeploymentCapability.getActions().put(CapabilityActionType.UNINSTALL,
//                new AwxCapabilityAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "uninstall.yml"));
//        dockerSwarmDeploymentCapability.getActions().put(CapabilityActionType.DEPLOY,
//                new AwxCapabilityAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "deploy.yml"));
//        dockerSwarmDeploymentCapability.getActions().put(CapabilityActionType.UNDEPLOY,
//                new AwxCapabilityAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "undeploy.yml"));
//        dockerSwarmDeploymentCapability.getActions().put(CapabilityActionType.SCALE_UP,
//                new AwxCapabilityAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "scaleup.yml"));
//        dockerSwarmDeploymentCapability.getActions().put(CapabilityActionType.SCALE_DOWN,
//                new AwxCapabilityAction(dockerSwarmDeploymentCapabilityRepo, dockerSwarmDeploymentCapabilityBranch, "scaledown.yml"));
//
//        // Set Cluster Member Types:
//        dockerSwarmDeploymentCapability.setClusterMemberTypes(Arrays.asList(
//                new ClusterMemberType( "docker_swarm_manager","Manager", 3, false),
//                new ClusterMemberType("docker_swarm_worker","Worker", 1, true)
//        ));
//        //endregion
//    }
//
//    @Nested
//    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
//    public class testCreateAndDeleteOfSingleHostDeploymentCapability {
//
//        //region Variables
//        BasicResource basicResource = getBasicResources(1).get(0);
//        List<NodeService> nodeServicesBefore = new ArrayList<>();
//        List<NodeService> nodeServicesAfterAddingCapability = new ArrayList<>();
//        List<DeploymentCapability> capabilitiesOfResourceAfterAddingCapability = new ArrayList<>();
//        //endregion
//
//        @BeforeAll
//        void beforeAll() throws JsonProcessingException {
//            if(START_WORKLOAD) {
//                dockerComposeWorkload = new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
//                        .withExposedService(AWX_WEB_SERVICE, AWX_PORT, Wait.forListeningPort())
//                        .withExposedService(CONSUL_SERVICE, CONSUL_PORT, Wait.forListeningPort())
//                        .withExposedService(VAULT_SERVICE, VAULT_PORT, Wait.forListeningPort())
//                        .withLocalCompose(true);
//
//                dockerComposeWorkload.start();
//
//                setPortsOfDockerComposeServices();
//                createConsulAndVaultCustomCredential();
//            }
//        }
//
//        @BeforeEach
//        void beforeEach() throws ConsulLoginFailedException, ResourceNotFoundException {
//            capabilitiesManager.addCapability(dockerDeploymentCapability);
//            capabilitiesManager.addCapability(dockerSwarmDeploymentCapability);
//
//            // Check Docker + DockerSwarm Capabilities are available:
//            List<Capability> persistedCapabilities = capabilitiesManager.getCapabilities();
//            assertEquals(2, persistedCapabilities.size());
//
//
//        }
//
//        @AfterAll
//        void afterAll() {
//            if(START_WORKLOAD)
//                dockerComposeWorkload.stop();
//        }
//
//
//        @Test
//        @Order(20)
//        public void createResource() throws ConsulLoginFailedException, ResourceNotFoundException {
//            // Add Resource to Resource Management:
//            resourcesManager.addExistingResource(
//                    keycloakPrincipal,
//                    basicResource,
//                    ConnectionType.ssh,
//                    ConnectionType.ssh.getDefaultPort(),
//                    credentialUsernamePassword,
//                    Optional.empty()
//            );
//
//            // Check Resource has no capabilities
//            nodeServicesBefore = consulServiceApiClient.getNodeServicesByNodeId(
//                    consulCredential,
//                    basicResource.getId()
//            );
//
//            List<DeploymentCapability> capabilitiesOfResourceBefore = capabilitiesManager.getDeploymentCapabilitiesOfResource(
//                    basicResource.getId(),
//                    consulCredential
//            );
//
//            assertEquals(0, capabilitiesOfResourceBefore.size());
//        }
//
//        @Test
//        @Order(30)
//        public void addCapability() throws AwxProjectUpdateFailedException, SSLException, ConsulLoginFailedException, ResourceNotFoundException, InterruptedException, CapabilityNotFoundException, JsonProcessingException {
//            // Get Docker DC by Name:
//            Optional<Capability> optionalDockerCapability = capabilitiesManager.getCapabilityByName(
//                    dockerDeploymentCapability.getName()
//            );
//            assertEquals(true, optionalDockerCapability.isPresent());
//
//            // Add Docker Capability to resource:
//            int awxJobId1 = capabilitiesManager.installCapabilityOnResource(
//                    basicResource.getId(),
//                    optionalDockerCapability.get().getId(),
//                    awxCredential,
//                    consulCredential,
//                    keycloakDummyToken
//
//            );
//
//            assertNotEquals(-1, awxJobId1);
//            awxClient.waitForJobUpdate(awxJobId1);
//
//            // Wait till capability has been added and check Resources service count:
//            long start_time = System.currentTimeMillis();
//            long timeout = 30000;
//            long endtime = start_time + timeout;
//            while (System.currentTimeMillis() < endtime) {
//                nodeServicesAfterAddingCapability = consulServiceApiClient.getNodeServicesByNodeId(
//                        consulCredential,
//                        basicResource.getId()
//                );
//
//                if (nodeServicesBefore.size() + 1 == nodeServicesAfterAddingCapability.size())
//                    break;
//                else {
//                    Thread.sleep(2000);
//                }
//            }
//
//            assertEquals(nodeServicesBefore.size() + 1, nodeServicesAfterAddingCapability.size());
//
//            // Get Capabilties of Resource and check if size is +1
//            capabilitiesOfResourceAfterAddingCapability = capabilitiesManager.getDeploymentCapabilitiesOfResource(
//                    basicResource.getId(),
//                    consulCredential
//            );
//
//            assertEquals(1, capabilitiesOfResourceAfterAddingCapability.size());
//            DeploymentCapability capabilityOfResource = capabilitiesOfResourceAfterAddingCapability.get(0);
//
//            // Compare Docker DC and DC added to Resource:
//            compareCapabilities(dockerDeploymentCapability, capabilityOfResource);
//        }
//
//        @Test
//        @Order(40)
//        public void removeCapability() throws AwxProjectUpdateFailedException, SSLException, ConsulLoginFailedException, InterruptedException, ResourceNotFoundException {
//            // Get Docker DC by Name:
//            Optional<Capability> optionalDockerCapability = capabilitiesManager.getCapabilityByName(
//                    dockerDeploymentCapability.getName()
//            );
//            assertEquals(true, optionalDockerCapability.isPresent());
//
//            // Remove Capability from resource
//            int awxJobId2 = capabilitiesManager.uninstallCapabilityFromResource(
//                    basicResource.getId(),
//                    optionalDockerCapability.get().getId(),
//                    awxCredential,
//                    consulCredential,
//                    keycloakDummyToken
//            );
//
//            assertNotEquals(-1, awxJobId2);
//
//            awxClient.waitForJobUpdate(awxJobId2);
//
//            // Wait till capability has been removed and check Resources service count:
//            List<NodeService> nodeServicesAfterRemovingCapability = new ArrayList<>();
//            long start_time = System.currentTimeMillis();
//            int timeout = 120000; //3000000;
//            while (System.currentTimeMillis() < start_time + timeout) {
//                nodeServicesAfterRemovingCapability = consulServiceApiClient.getNodeServicesByNodeId(
//                        consulCredential,
//                        basicResource.getId()
//                );
//
//                if (nodeServicesAfterAddingCapability.size() - 1 == nodeServicesAfterRemovingCapability.size())
//                    break;
//                else {
//                    Thread.sleep(2000);
//                    LOG.info("NodeServicesBeforeRemovingCap: " + nodeServicesAfterAddingCapability.size() +
//                            ", NodeServicesBeforeRemovingCap: " + nodeServicesAfterRemovingCapability.size() +
//                            " => Sleep for 2 seconds"
//                    );
//                }
//            }
//
//            // Get Capabilities of Resource and check if size is -1
//            List<DeploymentCapability> capabilitiesOfResourceAfterRemovingCapability = capabilitiesManager.getDeploymentCapabilitiesOfResource(
//                    basicResource.getId(),
//                    consulCredential
//            );
//            assertEquals(
//                    capabilitiesOfResourceAfterAddingCapability.size()-1,
//                    capabilitiesOfResourceAfterRemovingCapability.size()
//            );
//        }
//    }
//
//    @Nested
//    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
//    public class testOnJobStateFinishedForCreateAndDeleteOfMultiHostDeploymentCapability {
//        int basicResourceCount = 4;
//        List<BasicResource> basicResourceList = getBasicResources(basicResourceCount);
//        int jobId = 1;
//        MultiHostCapabilityService multiHostCapabilityService = createMultiHostCapabilityService(basicResourceList);
//        List<Policy> policyListInit;
//        List<Policy> policyListAfterCreateFinished = new ArrayList<>();
//        Map<String, Policy> policyMapAfterFinishingCreate = new HashMap<>();
//
//        @BeforeAll
//        void beforeAll() throws JsonProcessingException {
//            if(START_WORKLOAD) {
//                dockerComposeWorkload = new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
//                        .withExposedService(AWX_WEB_SERVICE, AWX_PORT, Wait.forListeningPort())
//                        .withExposedService(CONSUL_SERVICE, CONSUL_PORT, Wait.forListeningPort())
//                        .withExposedService(VAULT_SERVICE, VAULT_PORT, Wait.forListeningPort())
//                        .withLocalCompose(true);
//
//                dockerComposeWorkload.start();
//                setPortsOfDockerComposeServices();
//                createConsulAndVaultCustomCredential();
//            }
//        }
//
//        @AfterAll
//        void afterAll() {
//            if(START_WORKLOAD)
//                dockerComposeWorkload.stop();
//        }
//
//        @Test
//        @Order(10)
//        public void create() throws SSLException, ConsulLoginFailedException {
//            policyListInit = consulAclApiClient.getPolicies(new ConsulCredential());
//            //Add Resources to Resource Management
//            basicResourceList.forEach(r -> {
//                try {
//                    resourcesManager.addExistingResource(
//                            keycloakPrincipal,
//                            r,
//                            ConnectionType.ssh,
//                            ConnectionType.ssh.getDefaultPort(),
//                            credentialUsernamePassword,
//                            Optional.empty()
//
//                    );
//                } catch (ConsulLoginFailedException e) {
//                    throw new RuntimeException(e);
//                } catch (ResourceNotFoundException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//
//            var policyList = consulAclApiClient.getPolicies(new ConsulCredential());
//            var expectedPoliciesMap = new HashMap<String, Policy>();
//            for (var policy : policyList) {
//                try {
//                    expectedPoliciesMap.put(policy.getName(),
//                            consulAclApiClient.getPolicyByName(new ConsulCredential(), policy.getName()));
//                } catch (ConsulLoginFailedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            //Check if policy for each resource has been created
//            assertEquals(policyListInit.size() + basicResourceCount, expectedPoliciesMap.size());
//
//            AwxJobObserver awxJobObserver = awxJobObserverInitializer.init(
//                    jobId,
//                    JobTarget.RESOURCE,
//                    JobGoal.CREATE
//            );
//
//            ClusterJob clusterJob = new ClusterJob(multiHostCapabilityService);
//            clusterJob.setKeycloakPrincipal(keycloakPrincipal);
//            clusterJob.setClusterCreateRequest(new ClusterCreateRequest(multiHostCapabilityService.getId()));
//
//            clusterCreateFunctions.clusterJobMap.put(
//                    awxJobObserver,
//                    clusterJob
//            );
//
//            clusterCreateFunctions.onJobStateFinished(
//                    awxJobObserver,
//                    JobFinalState.SUCCESSFUL
//            );
//
//            var multiHostResourceId = "resource_" + multiHostCapabilityService.getId();
//            var multiHostResourcePolicy = consulAclApiClient.getPolicyByName(new ConsulCredential(), multiHostResourceId);
//            expectedPoliciesMap.put(multiHostResourceId, multiHostResourcePolicy);
//
//            policyListAfterCreateFinished = consulAclApiClient.getPolicies(new ConsulCredential());
//            for (var policy : policyListAfterCreateFinished) {
//                try {
//                    policyMapAfterFinishingCreate.put(
//                            policy.getName(),
//                            consulAclApiClient.getPolicyByName(new ConsulCredential(), policy.getName())
//                    );
//                } catch (ConsulLoginFailedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            //Assert that policies of resources have been changed due to adding Capability
//            assertThat(policyMapAfterFinishingCreate).hasSize(expectedPoliciesMap.size());
////            for (var policyEntry : policyMapAfterFinishingCreate.entrySet()) {
////                var policyName = policyEntry.getKey();
////                var policy = policyEntry.getValue();
////                if (policyName.startsWith("resource_")) {
////                    if (policyName.equals(multiHostResourceId)) {
////                        assertThat(policy.getRules()).contains("service").contains("docker-swarm");
////                    }
////                    else {
////                        assertThat(policy.getRules()).hasSize(expectedPoliciesMap.get(policyName).getRules().length());
////                    }
////                }
////            }
//        }
//
//        @Test
//        @Order(20)
//        public void delete() throws SSLException, ConsulLoginFailedException {
//            // Remove Capability
//            AwxJobObserver awxJobObserver = awxJobObserverInitializer.init(
//                    jobId,
//                    JobTarget.RESOURCE,
//                    JobGoal.DELETE
//            );
//
//            ClusterJob clusterJob = new ClusterJob(multiHostCapabilityService);
//            clusterJob.setKeycloakPrincipal(keycloakPrincipal);
//
//            clusterCreateFunctions.clusterJobMap.put(
//                    awxJobObserver,
//                    clusterJob
//            );
//
//            clusterCreateFunctions.onJobStateFinished(
//                    awxJobObserver,
//                    JobFinalState.SUCCESSFUL
//            );
//
//            var policyList = consulAclApiClient.getPolicies(new ConsulCredential());
//            var expectedPoliciesMap = new HashMap<String, Policy>();
//            for (var policy : policyList) {
//                try {
//                    expectedPoliciesMap.put(policy.getName(),
//                            consulAclApiClient.getPolicyByName(new ConsulCredential(), policy.getName()));
//                } catch (ConsulLoginFailedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            var multiHostResourceId = "resource_" + multiHostCapabilityService.getId();
//            var multiHostResourcePolicy = consulAclApiClient.getPolicyByName(new ConsulCredential(), multiHostResourceId);
//            expectedPoliciesMap.put(multiHostResourceId, multiHostResourcePolicy);
//
//            // Assert Policies of resources have been changed due to removing Capability
//            assertThat(policyListAfterCreateFinished).hasSize(expectedPoliciesMap.size());
////            for (var policyEntry : policyMapAfterFinishingCreate.entrySet()) {
////                var policyName = policyEntry.getKey();
////                var policy = policyEntry.getValue();
////                if (policyName.startsWith("resource_")) {
////                    assertThat(policy.getRules()).hasSize(expectedPoliciesMap.get(policyName).getRules().length());
////                }
////            }
//        }
//    }
//
//    @Nested
//    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
//    public class testCreateAndDeleteOfMultiHostDeploymentCapability {
//        List<BasicResource> basicResourceList = getBasicResources(4);
//        MultiHostCapabilityService multiHostCapabilityService;
//
//        @BeforeAll
//        void beforeAll() throws JsonProcessingException {
//            if(START_WORKLOAD) {
//                dockerComposeWorkload = new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
//                        .withExposedService(AWX_WEB_SERVICE, AWX_PORT, Wait.forListeningPort())
//                        .withExposedService(CONSUL_SERVICE, CONSUL_PORT, Wait.forListeningPort())
//                        .withExposedService(VAULT_SERVICE, VAULT_PORT, Wait.forListeningPort())
//                        .withLocalCompose(true);
//
//                dockerComposeWorkload.start();
//                setPortsOfDockerComposeServices();
//                createConsulAndVaultCustomCredential();
//            }
//        }
//
//        @BeforeEach
//        void beforeEach() throws ConsulLoginFailedException, ResourceNotFoundException {
//            capabilitiesManager.addCapability(dockerDeploymentCapability);
//            capabilitiesManager.addCapability(dockerSwarmDeploymentCapability);
//
//            // Check Docker + DockerSwarm Capabilities are available:
//            List<Capability> persistedCapabilities = capabilitiesManager.getCapabilities();
//            assertEquals(2, persistedCapabilities.size());
//        }
//
//        @AfterAll
//        void afterAll() {
//            if(START_WORKLOAD)
//                dockerComposeWorkload.stop();
//        }
//
//        @Test
//        @Order(10)
//        public void createResources() {
//            //Add Resources to Resource Management
//            basicResourceList.forEach(r -> {
//                try {
//                    resourcesManager.addExistingResource(
//                            keycloakPrincipal,
//                            r,
//                            ConnectionType.ssh,
//                            ConnectionType.ssh.getDefaultPort(),
//                            credentialUsernamePassword,
//                            Optional.empty()
//                    );
//                } catch (ConsulLoginFailedException e) {
//                    throw new RuntimeException(e);
//                } catch (ResourceNotFoundException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//        }
//
//
//        @Test
//        @Order(20)
//        public void addCapability() throws AwxProjectUpdateFailedException, SSLException, ConsulLoginFailedException {
//            // Get Docker Swarm DC by Name:
//            Optional<Capability> optionalDockerSwarmCapability = capabilitiesManager.getCapabilityByName(
//                    dockerSwarmDeploymentCapability.getName()
//            );
//            assertEquals(true, optionalDockerSwarmCapability.isPresent());
//
//            // Check Resources have no capabilities
//            List<List<NodeService>> nodeServicesBefore = new ArrayList<>(basicResourceList.size());
//            basicResourceList.forEach(r -> {
//                try {
//                    List<NodeService> servicesBefore = consulServiceApiClient.getNodeServicesByNodeId(
//                            consulCredential,
//                            r.getId()
//                    );
//                    nodeServicesBefore.add(servicesBefore);
//                } catch (ConsulLoginFailedException e) {
//                    throw new RuntimeException(e);
//                }
//
//                List<DeploymentCapability> capabilitiesOfResourceBefore = null;
//                try {
//                    capabilitiesOfResourceBefore = capabilitiesManager.getDeploymentCapabilitiesOfResource(
//                            r.getId(),
//                            consulCredential
//                    );
//                } catch (ResourceNotFoundException e) {
//                    throw new RuntimeException(e);
//                } catch (ConsulLoginFailedException e) {
//                    throw new RuntimeException(e);
//                }
//
//                assertEquals(0, capabilitiesOfResourceBefore.size());
//            });
//
//            // Add Docker Swarm Capability to resource:
//            multiHostCapabilityService = createMultiHostCapabilityService(basicResourceList);
//            int awxJobId1 = clusterHandler.create(
//                    multiHostCapabilityService,
//                    awxCredential,
//                    consulCredential,
//                    new ClusterCreateRequest(optionalDockerSwarmCapability.get().getId())
//            ).getAwxJobObserver().jobId;
//
//            assertNotEquals(-1, awxJobId1);
//            awxClient.waitForJobUpdate(awxJobId1);
//
//            // Wait till capability has been added and check Resource service count:
//            List<List<NodeService>> nodeServicesAfterAddingCapability = new ArrayList<>();
//            long start_time = System.currentTimeMillis();
//            long timeout = 30000;
//            AtomicInteger i = new AtomicInteger(0);
//
//            basicResourceList.forEach(basicResource -> {
//                while (System.currentTimeMillis() < start_time + timeout) {
//                    try {
//                        nodeServicesAfterAddingCapability.add(
//                                i.get(),
//                                consulServiceApiClient.getNodeServicesByNodeId(
//                                        consulCredential,
//                                        basicResource.getId()
//                                )
//                        );
//                    } catch (ConsulLoginFailedException e) {
//                        throw new RuntimeException(e);
//                    }
//
//                    if (nodeServicesBefore.get(i.get()).size() + 1 == nodeServicesAfterAddingCapability.get(i.get()).size())
//                        break;
//                    else {
//                        try {
//                            Thread.sleep(2000);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                }
//
//                assertEquals(
//                        nodeServicesBefore.get(i.get()).size() + 1,
//                        nodeServicesAfterAddingCapability.get(i.get()).size());
//                i.getAndIncrement();
//            });
//        }
//
//        @Test
//        @Order(30)
//        public void removeCapability() throws AwxProjectUpdateFailedException, SSLException, ConsulLoginFailedException {
//            clusterHandler.getClusterMembers(
//                    consulCredential,
//                    dockerSwarmDeploymentCapability.getName()
//            );
//
//            int awxJobId2 = clusterHandler.delete(
//                    awxCredential,
//                    consulCredential,
//                    multiHostCapabilityService
//            ).getAwxJobObserver().jobId;
//
//            assertNotEquals(-1, awxJobId2);
//            awxClient.waitForJobUpdate(awxJobId2);
//        }
//    }
//
//    private void compareCapabilities(DeploymentCapability capability1, DeploymentCapability capability2) {
//        assertEquals(capability1.getCapabilityClass(), capability2.getCapabilityClass());
//        assertEquals(capability1.getName(), capability2.getName());
//        assertEquals(capability1.getLogo(), capability2.getLogo());
//        assertEquals(capability1.getType(), capability2.getType());
//
//        // Assert Actions:
//        assertEquals(capability1.getActions().size(), capability2.getActions().size());
//        assertTrue(capability1.getActions().keySet().equals(capability2.getActions().keySet()));
//        capability1.getActions().values().stream().forEach(capAction -> {
//            assertTrue(capability2.getActions().values().contains(capAction));
//        });
//
//        //Assert clusterMemberTypes:
//        assertEquals(capability1.getClusterMemberTypes().size(), capability2.getClusterMemberTypes().size());
//        capability1.getClusterMemberTypes().stream().forEach(clusMemType -> {
//            assertTrue(capability2.getClusterMemberTypes().contains(clusMemType));
//        });
//
//        //Assert supported Deployment Types:
//        assertEquals(capability1.getSupportedDeploymentTypes().size(), capability2.getSupportedDeploymentTypes().size());
//        capability1.getSupportedDeploymentTypes().stream().forEach(deplType -> {
//            assertTrue(capability2.getSupportedDeploymentTypes().contains(deplType));
//        });
//    }
//
//    private MultiHostCapabilityService createMultiHostCapabilityService(List<BasicResource> basicResourceList) {
//        Map<UUID, String> memberMapping = new HashMap<>();
//        memberMapping.put(
//                basicResourceList.get(0).getId(),
//                "docker_swarm_manager"
//        );
//        memberMapping.put(
//                basicResourceList.get(1).getId(),
//                "docker_swarm_manager"
//        );
//        memberMapping.put(
//                basicResourceList.get(2).getId(),
//                "docker_swarm_manager"
//        );
//        memberMapping.put(
//                basicResourceList.get(3).getId(),
//                "docker_swarm_worker"
//        );
//        return new MultiHostCapabilityService(
//                dockerSwarmDeploymentCapability,
//                memberMapping,
//                CapabilityServiceStatus.INSTALL,
//                false
//        );
//    }
//
//    private List<BasicResource> getBasicResources(int count) {
//        List<BasicResource> basicResourceList = new ArrayList<>(count);
//
//        for(int i = 0; i < count; i++) {
//            basicResourceList.add(new BasicResource(
//                    UUID.randomUUID(),
//                    "test-host-"+(i+1),
//                    "192.168.0."+(i+1)
//            ));
//        }
//
//        return basicResourceList;
//    }
//
//    private void setPortsOfDockerComposeServices() {
//        awxClient.setAwxPort(
//                dockerComposeWorkload.getServicePort(AWX_WEB_SERVICE, AWX_PORT)
//        );
//
//        awxJobObserverInitializer.setAwxPort(
//                dockerComposeWorkload.getServicePort(AWX_WEB_SERVICE, AWX_PORT)
//        );
//
//        vaultClient.setPort(dockerComposeWorkload.getServicePort(VAULT_SERVICE, VAULT_PORT));
//        vaultClient.createResourcesKVSecretEngine(new VaultCredential());
//    }
//
//    private void createConsulAndVaultCustomCredential() throws JsonProcessingException {
//        CredentialTypeDTOApiCreate consulCustomCredentialType = getConsulCustomCredentialType();
//        CredentialTypeDTOApiCreate vaultCustomCredentialType = getVaultCustomCredentialType();
//
//        awxClient.createCredentialType(consulCustomCredentialType);
//        awxClient.createCredentialType(vaultCustomCredentialType);
//
//        awxClient.createCredential(getConsulCredentialDTO());
//        awxClient.createCredential(getVaultCredentialDTO());
//    }
//
//    private CredentialTypeDTOApiCreate getVaultCustomCredentialType() {
//        CredentialTypeInputFieldItem vaultUrlItem = new CredentialTypeInputFieldItem(
//                "vault_url",
//                "Vault URL",
//                false,
//                "string"
//        );
//
//        CredentialTypeInputFieldItem vaultApproleIdItem = new CredentialTypeInputFieldItem(
//                "vault_approle_role_id",
//                "Vault App Role - Role ID",
//                false,
//                "string"
//        );
//
//        CredentialTypeInputFieldItem vaultApproleSecretIdItem = new CredentialTypeInputFieldItem(
//                "vault_approle_secret_id",
//                "Vault App Role - Secret ID",
//                true,
//                "string"
//        );
//
//        List<CredentialTypeInputFieldItem> credentialTypeInputFieldItemList = Arrays.asList(
//                vaultUrlItem,
//                vaultApproleIdItem,
//                vaultApproleSecretIdItem
//        );
//
//        HashMap<String, List<CredentialTypeInputFieldItem>> inputsMap = new HashMap<>();
//        inputsMap.put("fields", credentialTypeInputFieldItemList);
//
//        HashMap<String, String> envMap = new HashMap<>();
//        envMap.put("VAULT_APPROLE_ROLE_ID", "{{ vault_approle_role_id }}");
//        envMap.put("VAULT_APPROLE_SECRET_ID", "{{vault_approle_secret_id}}");
//        envMap.put("VAULT_URL", "{{vault_url}}");
//
//        HashMap<String, Map<String, String>> injectorsMap = new HashMap<>();
//        injectorsMap.put("env", envMap);
//
//        return new CredentialTypeDTOApiCreate(
//                "HashiCorp Vault",
//                "",
//                "cloud",
//                inputsMap,
//                injectorsMap
//        );
//    }
//
//    private CredentialDTOApiCreate getVaultCredentialDTO() throws JsonProcessingException {
//        HashMap<String, String> inputs = new HashMap<>();
//        inputs.put("vault_url","test");
//        inputs.put("vault_approle_role_id","test");
//        inputs.put("vault_approle_secret_id","test");
//
//        return new CredentialDTOApiCreate(
//                getVaultCustomCredentialType().getName(),
//                "",
//                awxClient.getDefaultOrganisation().getId(),
//                awxClient.getCustomCredentialTypeByName(getVaultCustomCredentialType().getName()).getId(),
//                inputs
//        );
//    }
//
//    private CredentialTypeDTOApiCreate getConsulCustomCredentialType() {
//        CredentialTypeInputFieldItem consulUrlItem = new CredentialTypeInputFieldItem(
//                "consul_url",
//                "Consul URL",
//                false,
//                "string"
//        );
//
//        CredentialTypeInputFieldItem consulTokenItem = new CredentialTypeInputFieldItem(
//                "consul_token",
//                "Consul Token",
//                true,
//                "string"
//        );
//
//        List<CredentialTypeInputFieldItem> credentialTypeInputFieldItemList = Arrays.asList(
//                consulUrlItem,
//                consulTokenItem
//        );
//
//        HashMap<String, List<CredentialTypeInputFieldItem>> inputsMap = new HashMap<>();
//        inputsMap.put("fields", credentialTypeInputFieldItemList);
//
//        HashMap<String, String> envMap = new HashMap<>();
//        envMap.put("CONSUL_TOKEN", "{{ consul_token }}");
//        envMap.put("CONSUL_URL", "{{consul_url}}");
//
//        HashMap<String, Map<String, String>> injectorsMap = new HashMap<>();
//        injectorsMap.put("env", envMap);
//
//        return new CredentialTypeDTOApiCreate(
//                "Consul",
//                "",
//                "cloud",
//                inputsMap,
//                injectorsMap
//        );
//    }
//
//    private CredentialDTOApiCreate getConsulCredentialDTO() throws JsonProcessingException {
//        HashMap<String, String> inputs = new HashMap<>();
//        inputs.put("consul_url","test");
//        inputs.put("consul_token","test");
//
//        return new CredentialDTOApiCreate(
//                getConsulCustomCredentialType().getName(),
//                "",
//                awxClient.getDefaultOrganisation().getId(),
//                awxClient.getCustomCredentialTypeByName(getConsulCustomCredentialType().getName()).getId(),
//                inputs
//        );
//    }
//}
