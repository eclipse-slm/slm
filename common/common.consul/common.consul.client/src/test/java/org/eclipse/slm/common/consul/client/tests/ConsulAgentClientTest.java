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

import static org.junit.Assert.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        ConsulNodesApiClient.class,
        ConsulServicesApiClient.class,
        ConsulAgentApiClient.class,
        ConsulAclApiClient.class,
        ConsulHealthApiClient.class,
        ConsulGenericServicesClient.class,
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
    private static String CONSUL_CLIENT_SERVICE_NAME = "consul-client";
    private static int CONSUL_SERVER_PORT = 8500;
    private static int CONSUL_CLIENT_PORT = 8500;

    //See docker-compose file for nodeName and nodeId
    public static UUID nodeIdAgent = UUID.fromString("79e4fb43-8233-4d07-868a-928d521063f3");
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
    //endregion

    static {
        consulServerClientCompose = new DockerComposeContainer(new File(dockerComposeFilePath))
                .withExposedService(CONSUL_SERVER_SERVICE_NAME, CONSUL_SERVER_PORT, Wait.forListeningPort())
                .withExposedService(CONSUL_CLIENT_SERVICE_NAME, CONSUL_CLIENT_PORT, Wait.forListeningPort())
                .withLocalCompose(true);
        consulServerClientCompose.start();
    }

    @DynamicPropertySource
    static void consulProperties(DynamicPropertyRegistry registry){
        registry.add(
                "consul.port",
                () -> consulServerClientCompose.getServicePort(CONSUL_SERVER_SERVICE_NAME, CONSUL_SERVER_PORT)
        );
    }

    private void mockGetConsulNodeById() throws ConsulLoginFailedException {
        Node node = new Node();
        node.setNode("test-node");
        node.setId(nodeIdAgent);
        node.setAddress("127.0.0.1");

        Mockito
                .doReturn(Optional.of(node))
                .when(consulNodesApiClient).getNodeById(Mockito.any(), Mockito.any());
    }

    @Nested
    @Order(10)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class Prepare {
        @Test
        @Order(10)
        public void testConsulNodeCountEqualsTwo() throws ConsulLoginFailedException, InterruptedException {
            List<Node> nodes = new ArrayList<>();
            int tries = 0;
            int maxTries = 60;
            while(nodes.size() < 2) {
                nodes = consulNodesApiClient.getNodes(new ConsulCredential());
                Thread.sleep(1000);
                tries++;
                LOG.info(tries + "# try");

                if(tries > maxTries)
                    break;
            }

            assertEquals(nodes.size(), 2);
        }

        @Test
        @Order(20)
        public void registerConsulClientAsConsulService() throws ConsulLoginFailedException {
            mockGetConsulNodeById();

            Integer exposedConsulClientPort = consulServerClientCompose.getServicePort(
                    CONSUL_CLIENT_SERVICE_NAME,
                    CONSUL_CLIENT_PORT
            );

            consulAgentApiClient.registerService(
                    new ConsulCredential(),
                    exposedConsulClientPort,
                    nodeIdAgent,
                    ConsulAgentApiClient.CONSUL_CLIENT_SERVICE_NAME,
                    UUID.randomUUID(),
                    Optional.of(exposedConsulClientPort),
                    new ArrayList<>(),
                    new HashMap<>()
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



        private void assertServiceCount(int expectedServiceCount) {
            Map<String, List<String>> services = consulServicesApiClient.getServices(new ConsulCredential());

            assertEquals(services.size(), expectedServiceCount);
        };

        @Test
        @Order(10)
        public void testIsAgentAvailable() throws ConsulLoginFailedException {
            assertEquals(true, consulHealthApiClient.hasNodeAgent(new ConsulCredential(), nodeIdAgent));
        }

        @Test
        @Order(20)
        public void testRegisterServiceViaAgent() throws Exception {
            mockGetConsulNodeById();

            // Size == 3 because "consul" and "consul-client" are registered, too
            int expectedServiceCount = 3;

            consulAgentApiClient.registerService(
                    new ConsulCredential(),
                    nodeIdAgent,
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
            mockGetConsulNodeById();

            // Size == 2 because "consul" and "consul-client" are registered, too
            int expectedServiceCount = 2;

            consulAgentApiClient.removeServiceByName(
                    new ConsulCredential(),
                    nodeIdAgent,
                    service.getService()
            );

            assertServiceCount(expectedServiceCount);
        }
    }

    @Nested
    @Order(30)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public class TestMixedAgentNonAgentScenario {
        CatalogNode nonAgentNode = new CatalogNode();
        private static final UUID nodeIdNonAgent = UUID.fromString("ea79b972-f805-4fb1-a2b7-28d825f5ef38");
        private static final UUID serviceIdNonAgent = UUID.fromString("30eb3356-f9d9-4568-8641-ed569a992f2f");
        private static final UUID serviceIdAgent = UUID.fromString("d29d28bc-68ba-478e-96cb-9b88d9330a47");

        private static final String serviceName = "test-service-for-mixed-env";
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
            //expted size == 3, because consul server container is also registered as node
            assertEquals(3, consulNodesApiClient.getNodes(new ConsulCredential()).size());
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

            mockGetConsulNodeById();

            consulGenericServicesClient.registerService(
                    new ConsulCredential(),
                    nodeIdAgent,
                    serviceName,
                    serviceIdAgent,
                    Optional.empty(),
                    new ArrayList<>(),
                    new HashMap<>()
            );

            List<NodeService> servicesOfAgent =
                    consulServicesApiClient.getNodeServicesByNodeId(new ConsulCredential(), nodeIdAgent);

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

            mockGetConsulNodeById();

            consulGenericServicesClient.deregisterService(
                    new ConsulCredential(),
                    nodeIdAgent,
                    serviceName
            );

            List<NodeService> servicesOfAgent =
                    consulServicesApiClient.getNodeServicesByNodeId(new ConsulCredential(), nodeIdAgent);

            assertEquals(
                    expectedAgentServiceCount,
                    servicesOfAgent.size()
            );

            Optional<List<CatalogService>> optionalServices =
                    consulServicesApiClient.getServiceByName(new ConsulCredential(), serviceName);

            assertEquals(true, optionalServices.isEmpty());
        }
    }
}
