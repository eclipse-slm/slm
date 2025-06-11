package org.eclipse.slm.common.consul.client.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.*;
import org.eclipse.slm.common.consul.model.agent.Service;
import org.eclipse.slm.common.consul.model.catalog.CatalogNode;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        ConsulNodesApiClient.class,
        ConsulServicesApiClient.class,
        ConsulAgentApiClient.class,
        ConsulAclApiClient.class,
        ConsulHealthApiClient.class,
        ConsulGenericServicesClient.class,
        ConsulGenericNodeRemoveClient.class,
        RestTemplate.class,
        ObjectMapper.class
})
@TestPropertySource(properties = { "spring.config.location=classpath:application-test.yml" })
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class ConsulAgentClientTest {
    //region Variables
    public final static Logger LOG = LoggerFactory.getLogger(ConsulAgentClientTest.class);
    public final static String dockerComposeFilePath = "src/test/resources/docker-compose_consul-server-client.yml";
    public final static DockerComposeContainer consulServerClientCompose;
    private static String CONSUL_SERVER_SERVICE_NAME = "consul-server";
    private static String CONSUL_CLIENT_1_SERVICE_NAME = "consul-client-1";
    private static String CONSUL_CLIENT_2_SERVICE_NAME = "consul-client-2";
    private static int CONSUL_SERVER_PORT = 8500;
    private static int CONSUL_CLIENT_PORT = 8500;

    //See docker-compose file for nodeName and nodeId
    public static UUID nodeIdAgent1 = UUID.fromString("79e4fb43-8233-4d07-868a-928d521063f3");
    public static String nodeNameAgent1 = "test-node-one";
    public static UUID nodeIdAgent2 = UUID.fromString("fff35bca-06f0-473f-acba-5e8e6b63350d");
    public static String nodeNameAgent2 = "test-node-two";
    public static CatalogNode nonAgentNode = new CatalogNode();
    //endregion

    //region Autowiring
    @SpyBean
    ConsulNodesApiClient consulNodesApiClient;
    @Autowired
    ConsulServicesApiClient consulServicesApiClient;
    @Autowired
    ConsulAgentApiClient consulAgentApiClient;
    @Autowired
    ConsulHealthApiClient consulHealthApiClient;
    @Autowired
    ConsulGenericServicesClient consulGenericServicesClient;
    @Autowired
    ConsulGenericNodeRemoveClient consulGenericNodeRemoveClient;
    //endregion

    static {
        consulServerClientCompose = new DockerComposeContainer(new File(dockerComposeFilePath))
                .withExposedService(CONSUL_SERVER_SERVICE_NAME, CONSUL_SERVER_PORT, Wait.forListeningPort())
                .withExposedService(CONSUL_CLIENT_1_SERVICE_NAME, CONSUL_CLIENT_PORT, Wait.forListeningPort())
                .withExposedService(CONSUL_CLIENT_2_SERVICE_NAME, CONSUL_CLIENT_PORT, Wait.forListeningPort())
                .withLocalCompose(false);
        consulServerClientCompose.start();
    }

    @DynamicPropertySource
    static void consulProperties(DynamicPropertyRegistry registry){
        registry.add(
                "consul.port",
                () -> consulServerClientCompose.getServicePort(CONSUL_SERVER_SERVICE_NAME, CONSUL_SERVER_PORT)
        );
    }

    private void mockGetConsulNodeById(UUID nodeId) throws ConsulLoginFailedException {
        Node node = new Node();
        if(nodeId.equals(nodeIdAgent1)) {
            node.setNode(nodeNameAgent1);
            node.setId(nodeIdAgent1);
            node.setAddress("127.0.0.1");
        } else if(nodeId.equals(nodeIdAgent2)) {
            node.setNode(nodeNameAgent2);
            node.setId(nodeIdAgent2);
            node.setAddress("127.0.0.2");
        }

        Mockito
                .doReturn(Optional.of(node))
                .when(consulNodesApiClient).getNodeById(Mockito.any(), Mockito.any());
    }

    private void mockGetConsulNodeByName(String nodeName) throws ConsulLoginFailedException {
        Node node = new Node();
        if(nodeName.equals(nodeNameAgent1)) {
            node.setNode(nodeNameAgent1);
            node.setId(nodeIdAgent1);
            node.setAddress("127.0.0.1");
        } else if(nodeName.equals(nodeNameAgent1)) {
            node.setNode(nodeNameAgent2);
            node.setId(nodeIdAgent2);
            node.setAddress("127.0.0.2");
        }

        Mockito
                .doReturn(node)
                .when(consulNodesApiClient).getNodeByName(Mockito.any(), Mockito.any());
    }

    private void assertServiceCount(int expectedServiceCount) {
        Map<String, List<String>> services = consulServicesApiClient.getServices(new ConsulCredential());

        assertEquals(expectedServiceCount, services.size());
    };

    @Nested
    @Order(10)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class Prepare {
        @Test
        @Order(10)
        public void testConsulNodeCountEqualsTwo() throws ConsulLoginFailedException, InterruptedException {
            int expectedNodeCount = 3;

            List<Node> nodes = new ArrayList<>();
            int tries = 0;
            int maxTries = 120;
            while(nodes.size() < expectedNodeCount) {
                nodes = consulNodesApiClient.getNodes(new ConsulCredential());
                Thread.sleep(1000);
                tries++;
                LOG.info(tries + "# try");

                if(tries > maxTries)
                    break;
            }

            assertEquals(expectedNodeCount, nodes.size() );
        }

        @Test
        @Order(20)
        public void registerConsulClientAsConsulService() throws ConsulLoginFailedException {
            checkAgentsServiceCount(0,0);
            mockGetConsulNodeById(nodeIdAgent1);

            Integer exposedConsulClientPort = consulServerClientCompose.getServicePort(
                    CONSUL_CLIENT_1_SERVICE_NAME,
                    CONSUL_CLIENT_PORT
            );

            consulAgentApiClient.registerService(
                    new ConsulCredential(),
                    exposedConsulClientPort,
                    nodeIdAgent1,
                    ConsulAgentApiClient.CONSUL_CLIENT_SERVICE_NAME,
                    UUID.randomUUID(),
                    Optional.of(exposedConsulClientPort),
                    new ArrayList<>(),
                    new HashMap<>()
            );

            checkAgentsServiceCount(1,0);
            mockGetConsulNodeById(nodeIdAgent2);

            exposedConsulClientPort = consulServerClientCompose.getServicePort(
                    CONSUL_CLIENT_2_SERVICE_NAME,
                    CONSUL_CLIENT_PORT
            );

            consulAgentApiClient.registerService(
                    new ConsulCredential(),
                    exposedConsulClientPort,
                    nodeIdAgent2,
                    ConsulAgentApiClient.CONSUL_CLIENT_SERVICE_NAME,
                    UUID.randomUUID(),
                    Optional.of(exposedConsulClientPort),
                    new ArrayList<>(),
                    new HashMap<>()
            );

            checkAgentsServiceCount(1,1);
        }

        private void checkAgentsServiceCount(
                int expectedServiceCountAgent1,
                int expectedServiceCountAgent2
        ) throws ConsulLoginFailedException {
            assertEquals(
                    expectedServiceCountAgent1,
                    consulServicesApiClient.getNodeServices(new ConsulCredential(), nodeNameAgent1).size()
            );
            assertEquals(
                    expectedServiceCountAgent2,
                    consulServicesApiClient.getNodeServices(new ConsulCredential(), nodeNameAgent2).size()
            );
        }
    }

    @Nested
    @Order(20)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class TestConsulAgentClient {
        //region Test config vars
        public static UUID serviceId = UUID.randomUUID();
        public static Service service = new Service();

        static {
            service.setId(serviceId.toString());
            service.setService("Test-Service");
            service.setPort(8080);
        }
        //endregion

        @Test
        @Order(10)
        public void testIsAgentAvailable() throws ConsulLoginFailedException {
            assertEquals(true, consulHealthApiClient.hasNodeAgent(new ConsulCredential(), nodeIdAgent1));
        }

        @Test
        @Order(20)
        public void testRegisterServiceViaAgent() throws Exception {
            mockGetConsulNodeById(nodeIdAgent1);

            // Size == 3 because "consul" and "consul-client" are registered, too
            int expectedServiceCount = 3;

            consulAgentApiClient.registerService(
                    new ConsulCredential(),
                    nodeIdAgent1,
                    service.getService(),
                    serviceId,
                    Optional.of(service.getPort()),
                    new ArrayList<>(),
                    new HashMap<>()
            );

            assertServiceCount(expectedServiceCount);
        }

        @Test
        @Order(30)
        public void testConsulKeepsServiceOverTime() throws InterruptedException {
            int sleepTimeInSeconds = 10;
            // Size == 3 because "consul" and "consul-client" are registered, too
            int expectedServiceCount = 3;

            LOG.info("Sleep for "+sleepTimeInSeconds+" seconds...");
            Thread.sleep(sleepTimeInSeconds*1000);

            // Make sure Node keeps service over time:
            assertServiceCount(expectedServiceCount);
        }

        @Test
        @Order(40)
        public void testDeregisterServiceViaAgent() throws ConsulLoginFailedException {
            mockGetConsulNodeById(nodeIdAgent1);

            // Size == 2 because "consul" and "consul-client" are registered, too
            int expectedServiceCount = 2;

            consulAgentApiClient.removeServiceByName(
                    new ConsulCredential(),
                    nodeIdAgent1,
                    service.getService()
            );

            assertServiceCount(expectedServiceCount);
        }
    }

    @Nested
    @Order(30)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public class TestConsulGenericServicesClient {
        //region Testvars
        private static final UUID nodeIdNonAgent = UUID.fromString("ea79b972-f805-4fb1-a2b7-28d825f5ef38");
        private static final UUID serviceIdNonAgent = UUID.fromString("30eb3356-f9d9-4568-8641-ed569a992f2f");
        private static final UUID serviceIdAgent = UUID.fromString("d29d28bc-68ba-478e-96cb-9b88d9330a47");
        private static final String serviceName = "test-service-for-mixed-env";
        //endregion
        @BeforeAll
        private void beforeAll() throws ConsulLoginFailedException {
            nonAgentNode.setId(nodeIdNonAgent);
            nonAgentNode.setNode("non-agent-node");
            nonAgentNode.setAddress("1.2.3.4");

            consulNodesApiClient.registerNode(new ConsulCredential(), nonAgentNode);
        }
        @Test
        @Order(10)
        public void testThreeNodesAreAvailable() throws ConsulLoginFailedException {
            //excepted size == 4, because consul server container is also registered as node
            assertEquals(4, consulNodesApiClient.getNodes(new ConsulCredential()).size());
        }
        @Test
        @Order(20)
        public void testHasNodeAgentWithNonAgentNode() throws ConsulLoginFailedException {
            assertEquals(
                    false,
                    consulHealthApiClient.hasNodeAgent(new ConsulCredential(), nodeIdNonAgent)
            );
        }
        @Test
        @Order(40)
        public void registerServiceInAgentNonAgentEnvironment() throws ConsulLoginFailedException {
            int expectedNonAgentServiceCount = 1;
            int expectedAgentServiceCount = 2; // 2 because consul-client also registered

            consulGenericServicesClient.registerService(
                    new ConsulCredential(),
                    nodeIdNonAgent,
                    serviceName,
                    serviceIdNonAgent,
                    Optional.empty(),
                    new ArrayList<>(),
                    new HashMap<>()
            );

            List<NodeService> servicesOfNonAgent =
                    consulServicesApiClient.getNodeServicesByNodeId(new ConsulCredential(), nodeIdNonAgent);

            assertEquals(
                    expectedNonAgentServiceCount,
                    servicesOfNonAgent.size()
            );

            mockGetConsulNodeById(nodeIdAgent1);

            consulGenericServicesClient.registerService(
                    new ConsulCredential(),
                    nodeIdAgent1,
                    serviceName,
                    serviceIdAgent,
                    Optional.empty(),
                    new ArrayList<>(),
                    new HashMap<>()
            );

            List<NodeService> servicesOfAgent =
                    consulServicesApiClient.getNodeServicesByNodeId(new ConsulCredential(), nodeIdAgent1);

            assertEquals(
                    expectedAgentServiceCount,
                    servicesOfAgent.size()
            );

            Optional<List<CatalogService>> optionalServices = consulServicesApiClient.getServiceByName(new ConsulCredential(), serviceName);

            assertEquals(true, optionalServices.isPresent());
            assertEquals(2, optionalServices.get().size());
        }
        @Test
        @Order(50)
        public void deregisterServiceInAgentNonAgentEnvironment() throws ConsulLoginFailedException {
            int expectedNonAgentServiceCount = 0;
            int expectedAgentServiceCount = 1; // 2 because consul-client also registered

            consulGenericServicesClient.deregisterService(
                    new ConsulCredential(),
                    nodeIdNonAgent,
                    serviceName
            );

            List<NodeService> servicesOfNonAgent =
                    consulServicesApiClient.getNodeServicesByNodeId(new ConsulCredential(), nodeIdNonAgent);

            assertEquals(
                    expectedNonAgentServiceCount,
                    servicesOfNonAgent.size()
            );

            mockGetConsulNodeById(nodeIdAgent1);

            consulGenericServicesClient.deregisterService(
                    new ConsulCredential(),
                    nodeIdAgent1,
                    serviceName
            );

            List<NodeService> servicesOfAgent =
                    consulServicesApiClient.getNodeServicesByNodeId(new ConsulCredential(), nodeIdAgent1);

            assertEquals(
                    expectedAgentServiceCount,
                    servicesOfAgent.size()
            );

            Optional<List<CatalogService>> optionalServices =
                    consulServicesApiClient.getServiceByName(new ConsulCredential(), serviceName);

            assertEquals(true, optionalServices.isEmpty());
        }
    }

    @Nested
    @Order(40)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class TestGenericNodeRemoveClient {

        private static Stream<Arguments> getTestNodes() {
            return Stream.of(
                    Arguments.of(nodeNameAgent1, nodeIdAgent1),
                    Arguments.of(nonAgentNode.getNode(), nonAgentNode.getId())
            );
        }
        @Order(10)
        @ParameterizedTest
        @MethodSource("getTestNodes")
        public void testRemoveOfNodeWithAgent(String nodeName, UUID nodeId) throws ConsulLoginFailedException, InterruptedException {
            int sleepTimeInSeconds = 30;
            int nodesBeforeCount = consulNodesApiClient.getNodes(new ConsulCredential()).size();

            mockGetConsulNodeByName(nodeName);
            mockGetConsulNodeById(nodeId);
            consulGenericNodeRemoveClient.removeNode(new ConsulCredential(), nodeName);

            LOG.info("Wait " + sleepTimeInSeconds + " seconds if Node is showing up again.");
            Thread.sleep(sleepTimeInSeconds*1000);

            int nodesAfterCount = consulNodesApiClient.getNodes(new ConsulCredential()).size();

            assertEquals(
                    nodesBeforeCount-1,
                    nodesAfterCount
            );
        }

        @Order(20)
        @ParameterizedTest
        @MethodSource("getTestNodes")
        public void testReAddNode(String nodeName, UUID nodeId) throws ConsulLoginFailedException, InterruptedException {
            int sleepTimeInSeconds = 60;
            int nodesBeforeCount = consulNodesApiClient.getNodes(new ConsulCredential()).size();

            CatalogNode node = new CatalogNode();
            node.setNode(nodeName);
            node.setId(UUID.randomUUID());
            node.setAddress("127.0.0.1");

            consulNodesApiClient.registerNode(new ConsulCredential(), node);

            LOG.info("Wait " + sleepTimeInSeconds + " seconds if Node is showing up again.");
            Thread.sleep(sleepTimeInSeconds*1000);

            int nodesAfterCount = consulNodesApiClient.getNodes(new ConsulCredential()).size();

            assertEquals(
                    nodesBeforeCount+1,
                    nodesAfterCount
            );
        }
    }
}
