package org.eclipse.slm.common.consul.client.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulAgentApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.consul.model.agent.Service;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
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
    //endregion

    //region Autowiring
    @Autowired
    ConsulNodesApiClient consulNodesApiClient;
    @Autowired
    ConsulServicesApiClient consulServicesApiClient;
    @SpyBean
    ConsulAgentApiClient consulAgentApiClient;
    //endregion

    static {
        consulServerClientCompose = new DockerComposeContainer(new File(dockerComposeFilePath))
                .withExposedService(CONSUL_SERVER_SERVICE_NAME, CONSUL_SERVER_PORT, Wait.forListeningPort())
                .withExposedService(CONSUL_CLIENT_SERVICE_NAME, CONSUL_CLIENT_PORT, Wait.forListeningPort())
                .withLocalCompose(true);
        consulServerClientCompose.start();
    }

    @Nested
    @Order(10)
    public class PreTests {
        @Test
        public void testConsulNodeCountEqualsTwo() throws ConsulLoginFailedException, InterruptedException {
            List<Node> nodes = new ArrayList<>();
            int tries = 0;
            int maxTries = 60;
            while(nodes.size() < 2) {
                nodes = consulNodesApiClient.getNodes(new ConsulCredential());
                Thread.sleep(1000);
                tries++;
                LOG.info(tries + "# try");

                if(tries > 60)
                    break;
            }

            assertEquals(nodes.size(), 2);
        }
    }

    @Nested
    @Order(20)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class TestConsulAgentClient {
        //region Test config vars
        //See docker-compose file for nodeName and nodeId
        public static String nodeName = "test-node";
        public static UUID nodeId = UUID.fromString("79e4fb43-8233-4d07-868a-928d521063f3");
        public static UUID serviceId = UUID.randomUUID();
        public static Service service = new Service();

        static {
            service.setId(serviceId.toString());
            service.setService("Test-Service");
            service.setPort(8080);
        }
        //endregion

        private void mockGetConsulAgentClient() throws ConsulLoginFailedException {
            Mockito.doReturn(
                    Consul.builder()
                            .withHostAndPort(HostAndPort.fromParts("localhost", 9500))
                            .build()
            ).when(consulAgentApiClient).getConsulAgentClient(Mockito.any(), Mockito.any());
        }

        @Test
        @Order(10)
        public void testRegisterServiceViaAgent() throws Exception {
            mockGetConsulAgentClient();

            // Size == 2 because "consul" by default is also registered as a service
            int expectedServiceCount = 2;

            consulAgentApiClient.registerService(
                    new ConsulCredential(),
                    nodeId,
                    service.getService(),
                    serviceId,
                    Optional.of(service.getPort()),
                    new ArrayList<>(),
                    new HashMap<>()
            );

            Map<String, List<String>> services = consulServicesApiClient.getServices(new ConsulCredential());

            assertEquals(services.size(), expectedServiceCount);
        }

        @Test
        @Order(20)
        public void testConsulKeepsServiceOverTime() throws ConsulLoginFailedException, InterruptedException {
            int sleepTimeInSeconds = 10;
            // Size == 2 because "consul" by default is also registered as a service
            int expectedServiceCount = 2;

            LOG.info("Sleep for "+sleepTimeInSeconds+" seconds...");
            Thread.sleep(sleepTimeInSeconds*1000);

            // Make sure Node keeps service over time:
            Map<String, List<String>> services = consulServicesApiClient.getServices(new ConsulCredential());
            assertEquals(services.size(), expectedServiceCount);
        }

        @Test
        @Order(30)
        public void testDeregisterServiceViaAgent() throws ConsulLoginFailedException {
            mockGetConsulAgentClient();

            // Size == 1 because "consul" by default is also registered as a service
            int expectedServiceCount = 1;

            consulAgentApiClient.removeServiceByName(
                    new ConsulCredential(),
                    nodeId,
                    service.getService()
            );

            Map<String, List<String>> services = consulServicesApiClient.getServices(new ConsulCredential());

            assertEquals(services.size(), expectedServiceCount);
        }
    }
}
