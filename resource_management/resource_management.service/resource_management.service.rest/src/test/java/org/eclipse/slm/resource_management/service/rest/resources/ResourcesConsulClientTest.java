package org.eclipse.slm.resource_management.service.rest.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.*;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.model.resource.BasicResource;
import org.eclipse.slm.resource_management.persistence.api.LocationJpaRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        ResourcesConsulClient.class,
        ConsulAclApiClient.class,
        ConsulNodesApiClient.class,
        ConsulServicesApiClient.class,
        ConsulGenericServicesClient.class,
        ConsulGenericNodeRemoveClient.class,
        ConsulKeyValueApiClient.class,
        ConsulHealthApiClient.class,
        ConsulAgentApiClient.class,
        RestTemplate.class,
        ObjectMapper.class
})
@ContextConfiguration(initializers = {ConfigDataApplicationContextInitializer.class} )
@TestPropertySource(properties = { "spring.config.location=classpath:application-test.yml" })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResourcesConsulClientTest {
    //region Variables
    public final static Logger LOG = LoggerFactory.getLogger(ResourcesConsulClientTest.class);
    public final static GenericContainer<?> consulDockerContainer;
    private static int CONSUL_PORT = 8500;


    @DynamicPropertySource
    static void consulProperties(DynamicPropertyRegistry registry){
        registry.add("consul.port", consulDockerContainer::getFirstMappedPort);
    }

    @Autowired
    ConsulAclApiClient consulAclApiClient;
    @Autowired
    ConsulNodesApiClient consulNodesApiClient;
    @Autowired
    ConsulServicesApiClient consulServicesApiClient;
    @Autowired
    ConsulKeyValueApiClient consulKeyValueApiClient;
    @Autowired
    ResourcesConsulClient resourcesConsulClient;
    @MockBean
    LocationJpaRepository locationJpaRepository;
    //endregion

    //region Test variables
    BasicResource basicResource = new BasicResource(
            UUID.randomUUID(),
            "test-hostname",
            "192.168.0.1"
    );
    Boolean sshAccess = true;
    //endregion

    static {
        consulDockerContainer = new GenericContainer<>(DockerImageName.parse("consul:1.14"))
                .withExposedPorts(CONSUL_PORT)
                .withEnv("CONSUL_LOCAL_CONFIG", "{\"datacenter\": \"fabos\", \"domain\": \".fabos\", \"bind_addr\": \"0.0.0.0\", \"retry_join\": [\"0.0.0.0\"], \"acl\":{\"enabled\": true, \"default_policy\": \"allow\", \"tokens\":{\"master\": \"myroot\"}}}");
        consulDockerContainer.start();
    }

    @BeforeAll
    public static void beforeAll() throws InterruptedException {
        Thread.sleep(5000);
    }

    @AfterAll
    public static void afterAll(){
        consulDockerContainer.stop();
    }

    @Test
    @Order(10)
    public void testInjectedConsulInstances() {
        assertNotEquals(null, consulAclApiClient);
        assertNotEquals(null, consulNodesApiClient);
        assertNotEquals(null, resourcesConsulClient);
    }

    @Test
    @Order(20)
    public void testGetResources() throws ConsulLoginFailedException {
        List<BasicResource> resources = resourcesConsulClient.getResources(new ConsulCredential());

        // Consul server is registered by default as node
        // Change to 0 because they do not return consul as node
        assertEquals(0, resources.size());
        return;
    }

    @Test
    @Order(30)
    public void testCreateResource() throws ConsulLoginFailedException {
        List<BasicResource> resourcesBefore = resourcesConsulClient.getResources(new ConsulCredential());

        resourcesConsulClient.addResource(basicResource);

        List<BasicResource> resourcesAfter = resourcesConsulClient.getResources(new ConsulCredential());

        assertEquals(resourcesBefore.size()+1, resourcesAfter.size());
    }

    @Test
    @Order(40)
    public void testGetResourceByHostname() throws ConsulLoginFailedException {
        Optional<BasicResource> optionalResource = resourcesConsulClient.getResourceByHostname(new ConsulCredential(), basicResource.getHostname());

        assertTrue(optionalResource.isPresent());
        BasicResource foundResource = optionalResource.get();

        assertEquals(basicResource.getHostname(),   foundResource.getHostname());
        assertEquals(basicResource.getIp(),         foundResource.getIp());
    }

    @Test
    @Order(50)
    public void testGetResourceByIp() throws ConsulLoginFailedException {
        Optional<BasicResource> optionalResource = resourcesConsulClient.getResourceByIp(new ConsulCredential(), basicResource.getIp());

        assertTrue(optionalResource.isPresent());
        BasicResource foundResource = optionalResource.get();

        assertEquals(basicResource.getHostname(),   foundResource.getHostname());
        assertEquals(basicResource.getIp(),         foundResource.getIp());
    }

    @Test
    @Order(60)
    public void testDeleteResource() throws ConsulLoginFailedException {
        List<BasicResource> resourcesBefore = resourcesConsulClient.getResources(new ConsulCredential());

        Optional<BasicResource> resourceOptional = resourcesBefore.stream().filter(r -> r.getHostname().equals(basicResource.getHostname())).findFirst();

        assertTrue(resourceOptional.isPresent());

        BasicResource resourceFromConsul = resourceOptional.get();

        resourcesConsulClient.deleteResource(
                new ConsulCredential(),
                resourceFromConsul
        );

        List<BasicResource> resourcesAfter = resourcesConsulClient.getResources(new ConsulCredential());

        assertEquals(resourcesBefore.size()-1, resourcesAfter.size());
    }
}
