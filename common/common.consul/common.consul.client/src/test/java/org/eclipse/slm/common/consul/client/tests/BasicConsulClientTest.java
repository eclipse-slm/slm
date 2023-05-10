package org.eclipse.slm.common.consul.client.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.*;
import org.eclipse.slm.common.consul.model.acl.BindingRule;
import org.eclipse.slm.common.consul.model.acl.Policy;
import org.eclipse.slm.common.consul.model.acl.PolicyLink;
import org.eclipse.slm.common.consul.model.acl.Role;
import org.eclipse.slm.common.consul.model.catalog.*;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        ConsulAclApiClient.class,
        ConsulHealthApiClient.class,
        ConsulNodesApiClient.class,
        ConsulServicesApiClient.class,
        ConsulKeyValueApiClient.class,
        RestTemplate.class,
        ObjectMapper.class
})
@ContextConfiguration(initializers = {ConfigDataApplicationContextInitializer.class} )
@TestPropertySource(properties = { "spring.config.location=classpath:application-test.yml" })
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class BasicConsulClientTest {
    //region Variables
    public final static Logger LOG = LoggerFactory.getLogger(BasicConsulClientTest.class);
    public final static GenericContainer<?> consulDockerContainer;
    private static int CONSUL_PORT = 8500;


    @DynamicPropertySource
    static void consulProperties(DynamicPropertyRegistry registry){
        registry.add("consul.port", consulDockerContainer::getFirstMappedPort);
    }

    @Autowired
    ConsulAclApiClient consulAclApiClient;
    @Autowired
    ConsulHealthApiClient consulHealthApiClient;
    @Autowired
    ConsulNodesApiClient consulNodesApiClient;
    @Autowired
    ConsulServicesApiClient consulServicesApiClient;
    @Autowired
    ConsulKeyValueApiClient consulKeyValueApiClient;
    @Autowired
    ObjectMapper objectMapper;
    //endregion

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
    public class PreTests {
        @Test
        public void testInjectedConsulInstances() {
            assertNotEquals(null, consulAclApiClient);
        }
    }

    @Nested
    @Order(20)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class testNodeAndAclConsulClient {

        //region Variables
        public static CatalogNode catalogNode = null;
        String defaultPolicyName = "global-management";
        public Policy policy = new Policy(
                "Test-Policy",
                "Description of Test-Policy",
                "node \"test-node\" { policy = \"read\" }",
                Arrays.asList("fabos")
        );

        public Policy newValuePolicy = new Policy(
                policy.getName(),
                "New Description of Test-Policy",
                "node \"new-test-node\" { policy = \"read\" }",
                policy.getDatacenters()
        );

        String newReadRuleType = "node";
        String newReadRuleName = "another-new-test-node";
        String expectedNewRule = newReadRuleType + " \"" + newReadRuleName + "\" { policy = \"read\" }";

        public Role role = new Role(
                "New-Test-Role",
                "Description of 'new test role'",
                Arrays.asList(
                        new PolicyLink(newValuePolicy.getName())
                )
        );
        //endregion

        @BeforeAll
        public static void beforeAll() {
            int i = 0;
            UUID uuid = UUID.randomUUID();
            String nodeName = "test-node-"+(i+1);
            String address = "192.168.0.10"+i;

            catalogNode = new CatalogNode();
            catalogNode.setId(uuid);
            catalogNode.setNode(nodeName);
            catalogNode.setAddress(address);
        }

        @Test
        @Order(10)
        public void testGetNodes() throws ConsulLoginFailedException {
            List<Node> nodes = consulNodesApiClient.getNodes(new ConsulCredential());
            assertEquals(1, nodes.size());
        }

        //region Policy Tests (1/2)
        @Test
        @Order(11)
        public void testGetPolicies() throws ConsulLoginFailedException {
            List<Policy> policies = consulAclApiClient.getPolicies(new ConsulCredential());

            assertEquals(1, policies.size());
            assertEquals(defaultPolicyName, policies.get(0).getName());
        }

        @Test
        @Order(12)
        public void testGetPolicyByName() throws ConsulLoginFailedException {
            Policy policy = consulAclApiClient.getPolicyByName(
                    new ConsulCredential(),
                    defaultPolicyName
            );

            assertEquals(defaultPolicyName, policy.getName());
        }

        @Test
        @Order(13)
        public void testCreatePolicy() throws ConsulLoginFailedException {
            List<Policy> policiesBefore = consulAclApiClient.getPolicies(new ConsulCredential());

            consulAclApiClient.createPolicy(
                    new ConsulCredential(),
                    policy
            );

            List<Policy> policiesAfter = consulAclApiClient.getPolicies(new ConsulCredential());

            assertEquals(
                    policiesBefore.size()+1,
                    policiesAfter.size()
            );

            Policy newPolicy = consulAclApiClient.getPolicyByName(new ConsulCredential(), policy.getName());

            assertEquals(policy.getName(), newPolicy.getName());
            assertEquals(policy.getRules(), newPolicy.getRules());
            assertEquals(policy.getDescription(), newPolicy.getDescription());
            assertEquals(policy.getDatacenters(), newPolicy.getDatacenters());
        }

        @Test
        @Order(14)
        public void testUpdatePolicy() throws ConsulLoginFailedException {
            Policy oldValuePolicy = consulAclApiClient.getPolicyByName(new ConsulCredential(), policy.getName());
            newValuePolicy.setId(oldValuePolicy.getId());

            consulAclApiClient.updatePolicy(new ConsulCredential(), newValuePolicy);

            Policy updatedPolicy = consulAclApiClient.getPolicyByName(new ConsulCredential(), policy.getName());

            assertEquals(newValuePolicy.getId(),            updatedPolicy.getId());
            assertEquals(newValuePolicy.getName(),          updatedPolicy.getName());
            assertEquals(newValuePolicy.getDescription(),   updatedPolicy.getDescription());
            assertEquals(newValuePolicy.getRules(),         updatedPolicy.getRules());
            assertEquals(newValuePolicy.getDatacenters(),   updatedPolicy.getDatacenters());
        }

        @Test
        @Order(15)
        public void testAddReadRuleToPolicy() throws ConsulLoginFailedException {
            Policy policyToAddReadRuleBefore = consulAclApiClient.getPolicyByName(new ConsulCredential(), newValuePolicy.getName());

            consulAclApiClient.addReadRuleToPolicy(
                    new ConsulCredential(),
                    policyToAddReadRuleBefore.getName(),
                    newReadRuleType,
                    newReadRuleName
            );

            Policy policyToAddReadRuleAfter = consulAclApiClient.getPolicyByName(new ConsulCredential(), newValuePolicy.getName());
            assertTrue(policyToAddReadRuleAfter.getRules().contains(expectedNewRule));
        }

        @Test
        @Order(16)
        public void testRemoveAddRuleFromPolicy() throws ConsulLoginFailedException {
            consulAclApiClient.removeReadRuleFromPolicy(
                    new ConsulCredential(),
                    newValuePolicy.getName(),
                    newReadRuleType,
                    newReadRuleName
            );

            Policy policyToRemoveReadRuleAfter = consulAclApiClient.getPolicyByName(new ConsulCredential(), newValuePolicy.getName());

            assertFalse(policyToRemoveReadRuleAfter.getRules().contains(expectedNewRule));
        }
        //endregion

        //region Role Tests
        @Test
        @Order(18)
        public void testGetRoles() throws ConsulLoginFailedException {
            List<Role> roles = consulAclApiClient.getRoles(new ConsulCredential());

            assertEquals(0, roles.size());
        }

        @Test
        @Order(19)
        public void testCreateRole() throws ConsulLoginFailedException {
            List<Role> rolesBefore = consulAclApiClient.getRoles(new ConsulCredential());

            consulAclApiClient.createRole(
                    new ConsulCredential(),
                    role.getName(),
                    role.getDescription(),
                    role.getPolicies().get(0).getName()
            );

            List<Role> rolesAfter = consulAclApiClient.getRoles(new ConsulCredential());

            assertEquals(
                    rolesBefore.size()+1,
                    rolesAfter.size()
            );
        }

        @Test
        @Order(20)
        public void testGetRoleByName() throws ConsulLoginFailedException {
            Role roleByName = consulAclApiClient.getRoleByName(
                    new ConsulCredential(),
                    role.getName()
            );

            assertEquals(role.getName(),        roleByName.getName());
            assertEquals(role.getDescription(), roleByName.getDescription());
            assertEquals(role.getPolicies().size(), roleByName.getPolicies().size());
            assertEquals(role.getPolicies().size(), 1);
            assertEquals(roleByName.getPolicies().size(), 1);
            assertEquals(role.getPolicies().get(0).getName(), roleByName.getPolicies().get(0).getName());
        }

        @Test
        @Order(21)
        public void testDeleteRoleById() throws ConsulLoginFailedException {
            Role roleByName = consulAclApiClient.getRoleByName(
                    new ConsulCredential(),
                    role.getName()
            );

            List<Role> rolesBefore = consulAclApiClient.getRoles(new ConsulCredential());

            consulAclApiClient.deleteRoleById(
                    new ConsulCredential(),
                    roleByName.getId()
            );

            List<Role> rolesAfter = consulAclApiClient.getRoles(new ConsulCredential());

            assertEquals(rolesBefore.size()-1, rolesAfter.size());
        }
        //endregion

        //region Binding Rule Tests
        @Test
        @Order(22)
        public void testGetBindingRules() throws ConsulLoginFailedException {
            List<BindingRule> bindingRules = consulAclApiClient.getBindingRules(new ConsulCredential());

            assertEquals(0, bindingRules.size());
        }
        //endregion

        //region Policy Tests (2/2)
        @Test
        @Order(29)
        public void testDeletePolicy() throws ConsulLoginFailedException {
            List<Policy> policiesBefore = consulAclApiClient.getPolicies(new ConsulCredential());
            Policy policyToBeDeleted = consulAclApiClient.getPolicyByName(new ConsulCredential(), newValuePolicy.getName());

            consulAclApiClient.deletePolicyById(new ConsulCredential(), policyToBeDeleted.getId());

            List<Policy> policiesAfter = consulAclApiClient.getPolicies(new ConsulCredential());

            assertEquals(policiesBefore.size()-1, policiesAfter.size());
        }
        //endregion

        //region Node Tests
        @Test
        @Order(30)
        public void testRegisterNode() throws ConsulLoginFailedException {
            List<Node> nodesBefore = consulNodesApiClient.getNodes(new ConsulCredential());
            consulNodesApiClient.registerNode(
                    new ConsulCredential(),
                    catalogNode
            );

            assertEquals(
                    nodesBefore.size()+1,
                    consulNodesApiClient.getNodes(new ConsulCredential()).size()
            );
        }

        @Test
        @Order(40)
        public void testGetNodeById() throws ConsulLoginFailedException {
            Optional<Node> nodeOptional = consulNodesApiClient.getNodeById(
                    new ConsulCredential(),
                    catalogNode.getId()
            );

            assertEquals(true, nodeOptional.isPresent());

            Node node = nodeOptional.get();

            assertEquals(catalogNode.getId(), node.getId());
            assertEquals(catalogNode.getNode(), node.getNode());
            assertEquals(catalogNode.getAddress(), node.getAddress());
            assertEquals(catalogNode.getTaggedAddresses(), node.getTaggedAddresses());
        }

        @Test
        @Order(45)
        public void testGetNodesAndCompareToGetNodeById() throws ConsulLoginFailedException {
            var credentials = new ConsulCredential();
            var nodes = consulNodesApiClient.getNodes(credentials);
            for (Node node : nodes) {
                var optionalNodeToTest = consulNodesApiClient.getNodeById(credentials, node.getId());
                assertTrue(optionalNodeToTest.isPresent());
                var nodeToTest = optionalNodeToTest.get();
                assertEquals(node.getId(), nodeToTest.getId());
                assertEquals(node.getAddress(), nodeToTest.getAddress());
                assertEquals(node.getNode(), nodeToTest.getNode());
                if(node.getTaggedAddresses() != null){
                    assertEquals(node.getTaggedAddresses().getLan(), nodeToTest.getTaggedAddresses().getLan());
                    assertEquals(node.getTaggedAddresses().getWan(), nodeToTest.getTaggedAddresses().getWan());
                }else{
                    assertNull(nodeToTest.getTaggedAddresses());
                }

                assertEquals(node.getDatacenter(), nodeToTest.getDatacenter());
                assertEquals(node.getMeta(), nodeToTest.getMeta());
            }
        }


        @Test
        @Order(50)
        public void testDeregisterNode() throws ConsulLoginFailedException {
            List<Node> nodesBefore = consulNodesApiClient.getNodes(new ConsulCredential());

            consulNodesApiClient.deleteNode(
                    new ConsulCredential(),
                    catalogNode
            );

            assertEquals(
                    nodesBefore.size()-1,
                    consulNodesApiClient.getNodes(new ConsulCredential()).size()
            );
        }

        @Test
        @Order(60)
        public void registerMultipleNodes() throws ConsulLoginFailedException {
            for(int i = 1; i < 100; i++) {
                CatalogNode catalogNode1 = new CatalogNode();
                catalogNode1.setNode("node-"+i);
                catalogNode1.setAddress("1.1.1."+i);
                catalogNode1.setId(UUID.randomUUID());
                consulNodesApiClient.registerNode(
                        new ConsulCredential(),
                        catalogNode1
                );
            }

            List<Node> nodes = consulNodesApiClient.getNodes(new ConsulCredential());

            assertEquals(100, nodes.size());

            for(int i = 1; i < 100; i++) {
                consulNodesApiClient.deleteNode(
                        new ConsulCredential(),
                        "node-"+i
                );
            }
        }

        //endregion

        @Nested
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        public class testServiceAndHealthConsulClient {

            //region Variables
            private static ConsulServicesApiClient staticConsulServicesApiClient = null;
            CatalogNode.Service service = null;
            List<ConsulService> services = new ArrayList<>();

            CatalogNode.Check check = null;
            //endregion

            public testServiceAndHealthConsulClient() throws ConsulLoginFailedException {
                staticConsulServicesApiClient = consulServicesApiClient;

                consulNodesApiClient.registerNode(
                        new ConsulCredential(),
                        catalogNode
                );
                List<String> tags = Arrays.asList("tag1","tag2","tag3");
                Map<String,String> meta = Map.of(
                        "key1", "value1",
                        "key2", "value2",
                        "key3", "value3"
                );

                service = new CatalogNode.Service(UUID.randomUUID());
                service.setService("Test-Service");
                service.setPort(8080);
                service.setTags(tags);
                service.setServiceMeta(meta);

                for(int i = 0; i < 5; i++) {
                   services.add(new ConsulService(
                            catalogNode.getId(),
                            "Test-Service-"+(i+1),
                            UUID.randomUUID(),
                            tags,
                            meta
                    ));

                }

                CatalogNode.HttpCheckDefinition httpCheck = new CatalogNode.HttpCheckDefinition();
                httpCheck.setHttp("http://localhost");
                httpCheck.setInterval("30s");

                check = new CatalogNode.Check();
                check.setCheckId(UUID.randomUUID().toString());
                check.setName("Test-Health-Check");
                check.setDefinition(httpCheck);
            }

            @AfterAll
            public static void afterAll() throws ConsulLoginFailedException {
                Map<String, List<String>> services = staticConsulServicesApiClient.getServices(
                        new ConsulCredential()
                );

                for(String serviceName : services.keySet()) {
                    if(serviceName.equals("consul")){
                        continue;
                    }
                    Optional<List<CatalogService>> optionalCatalogServices = staticConsulServicesApiClient.getServiceByName(
                            new ConsulCredential(),
                            serviceName
                    );

                    CatalogService catalogService = optionalCatalogServices.get().get(0);

                    staticConsulServicesApiClient.removeServiceByName(
                            new ConsulCredential(),
                            catalogService.getId(),
                            serviceName
                    );
                }

                Map<String, List<String>> servicesAfter = staticConsulServicesApiClient.getServices(
                        new ConsulCredential()
                );



                return;
            }

            @Test
            @Order(10)
            public void testGetServicesWhenNoServiceIsRegistered() throws ConsulLoginFailedException {
                Map<String, List<String>> services = consulServicesApiClient.getServices(
                        new ConsulCredential()
                );

                assertEquals(1, services.size());
                assertTrue(services.containsKey("consul"));
            }

            @Test
            @Order(20)
            public void testGetNodeServicesNoRegisteredService() throws ConsulLoginFailedException {
                List<NodeService> nodeServices = consulServicesApiClient.getNodeServices(
                        new ConsulCredential(),
                        catalogNode.getNode()
                );

                assertEquals(0, nodeServices.size());
            }

            @Test
            @Order(30)
            public void testRegisterService() throws ConsulLoginFailedException {
                Map<String, List<String>> servicesBefore = consulServicesApiClient.getServices(
                        new ConsulCredential()
                );

                consulServicesApiClient.registerServiceWithoutAccess(
                        new ConsulCredential(),
                        catalogNode.getId(),
                        service
                );

                Map<String, List<String>> servicesAfter = consulServicesApiClient.getServices(
                        new ConsulCredential()
                );

                assertEquals(servicesBefore.size()+1, servicesAfter.size());
                assertTrue(servicesAfter.containsKey(service.getService()));
            }

            @Test
            @Order(35)
            public void testGetServicesWhenOneServiceIsRegistered() throws ConsulLoginFailedException {
                Map<String, List<String>> services = consulServicesApiClient.getServices(
                        new ConsulCredential()
                );

                assertEquals(2, services.size());
                assertTrue(services.containsKey("consul"));
                assertTrue(services.containsKey(service.getService()));
            }

            @Test
            @Order(40)
            public void testGetServiceByName() throws ConsulLoginFailedException {
                Optional<List<CatalogService>> serviceOptional = consulServicesApiClient.getServiceByName(
                        new ConsulCredential(),
                        service.getService()
                );

                assertTrue(serviceOptional.isPresent());
                List<CatalogService> serviceList = serviceOptional.get();
                assertEquals(1, serviceList.size());
                CatalogService serviceFromList = serviceList.get(0);
                assertEquals(service.getService(), serviceFromList.getServiceName());
                assertEquals(service.getPort(), serviceFromList.getServicePort());
                assertEquals(service.getServiceMeta(), serviceFromList.getServiceMeta());
                assertEquals(service.getTags(), serviceFromList.getServiceTags());

                // TODO: service id is not the same in consul as set when registering java service object
            }

            @Test
            @Order(45)
            public void testGetServiceById() throws ConsulLoginFailedException {
                Optional<List<CatalogService>> serviceOptional = consulServicesApiClient.getServiceByName(
                        new ConsulCredential(),
                        service.getService()
                );

                UUID serviceID = UUID.fromString(
                        serviceOptional.get().get(0).getServiceId()
                );

                Optional<CatalogService> optionalService = consulServicesApiClient.getServiceById(
                        new ConsulCredential(),
                        serviceID
                );

                assertTrue(optionalService.isPresent());
            }

            @Test
            @Order(50)
            public void testRemoveService() throws ConsulLoginFailedException {
                Map<String, List<String>> servicesBefore = consulServicesApiClient.getServices(
                        new ConsulCredential()
                );

                consulServicesApiClient.removeServiceByName(
                        new ConsulCredential(),
                        catalogNode.getId(),
                        service.getService().toString()
                );

                Map<String, List<String>> servicesAfter = consulServicesApiClient.getServices(
                        new ConsulCredential()
                );

                assertEquals(
                        servicesBefore.size()-1,
                        servicesAfter.size()
                );

                assertFalse(servicesAfter.containsKey(service.getService()));
            }

            @Test
            @Order(60)
            public void testRegisterServices() throws ConsulLoginFailedException {
                Map<String, List<String>> servicesBefore = consulServicesApiClient.getServices(
                        new ConsulCredential()
                );

                consulServicesApiClient.registerServices(
                        new ConsulCredential(),
                        services
                );

                Map<String, List<String>> servicesAfter = consulServicesApiClient.getServices(
                        new ConsulCredential()
                );

                assertEquals(
                        servicesBefore.size()+services.size(),
                        servicesAfter.size()
                );
            }

            @Test
            @Order(70)
            public void testGetNodeServices() throws ConsulLoginFailedException {
                List<NodeService> nodeServices = consulServicesApiClient.getNodeServices(
                        new ConsulCredential(),
                        catalogNode.getNode()
                );

                assertEquals(services.size(), nodeServices.size());
            }

            @Test
            @Order(80)
            public void testRemoveServiceByNameNotExists() throws ConsulLoginFailedException {
                Map<String, List<String>> servicesBefore = consulServicesApiClient.getServices(
                        new ConsulCredential()
                );

                consulServicesApiClient.removeServiceByName(
                        new ConsulCredential(),
                        catalogNode.getId(),
                        "test-test-test-test"
                );

                Map<String, List<String>> servicesAfter = consulServicesApiClient.getServices(
                        new ConsulCredential()
                );

                assertEquals(servicesBefore.size(), servicesAfter.size());
            }

            @Test
            @Order(90)
            public void testGetServicesByServiceNameList() throws ConsulLoginFailedException {
                List<String> serviceNameList = new ArrayList<>();

                for(int i = 0; i < 2; i++)
                    serviceNameList.add(services.get(i).getServiceName());

                Map<String, List<CatalogService>> servicesByName = consulServicesApiClient.getServicesByName(
                        new ConsulCredential(),
                        serviceNameList
                );

                assertEquals(serviceNameList.size(), servicesByName.size());

                for(String serviceName : serviceNameList)
                    assertTrue(servicesByName.containsKey(serviceName));
            }

            @Nested
            @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
            public class testHealthCheckConsulClient {

                CatalogService catalogService = null;

                public testHealthCheckConsulClient() throws ConsulLoginFailedException {
                    catalogService = consulServicesApiClient.getServiceByName(
                            new ConsulCredential(),
                            services.get(0).getServiceName()
                    ).get().get(0);
                }

                @Test
                @Order(10)
                public void testGetHealthChecksOfNode() throws ConsulLoginFailedException {
                    List<CatalogNode.Check> healthChecks = consulHealthApiClient.getChecksOfNode(
                            new ConsulCredential(),
                            catalogService.getId()
                    );

                    assertEquals(0, healthChecks.size());
                }

                @Test
                @Order(20)
                public void testAddHealthCheckToService() throws ConsulLoginFailedException {
                    var nodes = consulNodesApiClient.getNodes(new ConsulCredential());
                    var node = nodes.stream().filter(n -> Objects.equals(n.getNode(), catalogService.getNode())).findFirst();

                    assertTrue(node.isPresent());

                    List<CatalogNode.Check> healthChecksBefore = consulHealthApiClient.getChecksOfNode(
                            new ConsulCredential(),
                            node.get().getId()
                    );


                    consulHealthApiClient.addCheckForService(
                            new ConsulCredential(),
                            catalogService.getNode(),
                            UUID.fromString( catalogService.getServiceId() ),
                            check
                    );



                    List<CatalogNode.Check> healthChecksAfter = consulHealthApiClient.getChecksOfNode(
                            new ConsulCredential(),
                            catalogService.getNode()
                    );

                    assertEquals(healthChecksBefore.size()+1, healthChecksAfter.size());

                    return;
                }

//                @Test
//                @Order(30)
//                public void testGetStatusOfServiceCheck() {
//                    consulHealthApiClient.getStatusOfServiceCheck(
//                            new ConsulCredential(),
//
//                    );
//                }

                @Test
                @Order(40)
                public void testRemoveHealthCheckFromNode() throws ConsulLoginFailedException {
                    var nodes = consulNodesApiClient.getNodes(new ConsulCredential());
                    var node = nodes.stream().filter(n -> Objects.equals(n.getNode(), catalogService.getNode())).findFirst();

                    assertTrue(node.isPresent());

                    List<CatalogNode.Check> healthChecksBefore = consulHealthApiClient.getChecksOfNode(
                            new ConsulCredential(),
                            node.get().getId()
                    );


                    consulHealthApiClient.removeCheckFromNode(
                            new ConsulCredential(),
                            catalogService.getNode(),
                            healthChecksBefore.get(0).getCheckId()
                    );

                    List<CatalogNode.Check> healthChecksAfter = consulHealthApiClient.getChecksOfNode(
                            new ConsulCredential(),
                            catalogService.getNode()
                    );

                    assertEquals(healthChecksBefore.size()-1, healthChecksAfter.size());

                }
            }
        }
    }

    @Nested
    @Order(30)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class testKeyValueConsulClient {
        private static final UUID serviceId = UUID.randomUUID();
        private static final String key1 = serviceId+"/CredentialClasses";
        private static final String key2 = serviceId+"/ConnectionTypes";
        private static final List<String> value1 =
                Arrays.asList("CredentialUsernamePassword");
        private static final List<String> value2 =
                Arrays.asList("ssh", "tcp");

        @Test
        @Order(10)
        public void testGetConsulKeyValueIfNoKeyExists() throws ConsulLoginFailedException {
            String returnValue1 = consulKeyValueApiClient.getValueOfKey(
                    new ConsulCredential(),
                    key1
            );

            String returnValue2 = consulKeyValueApiClient.getValueOfKey(
                    new ConsulCredential(),
                    key1
            );

            assertEquals(null, returnValue1);
            assertEquals(null, returnValue2);
        }

        @Test
        @Order(11)
        public void testGetConsulKeyValueAsObjectIfNoKeyExists() throws ConsulLoginFailedException, JsonProcessingException {
            List<String> returnValue1 = consulKeyValueApiClient.getValueOfKey(
                    new ConsulCredential(),
                    key1,
                    new TypeReference<>() {}
            );

            List<String> returnValue2 = consulKeyValueApiClient.getValueOfKey(
                    new ConsulCredential(),
                    key1,
                    new TypeReference<>() {}
            );

            assertEquals(null, returnValue1);
            assertEquals(null, returnValue2);
        }

        @Test
        @Order(12)
        public void testGetKeysAndExpectNone() throws ConsulLoginFailedException {
            List<String> keys = consulKeyValueApiClient.getKeys(new ConsulCredential(), "");

            assertEquals(0, keys.size());
        }

        @Test
        @Order(20)
        public void testCreateConsulKeyValue() throws ConsulLoginFailedException {
            consulKeyValueApiClient.createKey(
                    new ConsulCredential(),
                    key1,
                    value1
            );

            consulKeyValueApiClient.createKey(
                    new ConsulCredential(),
                    key2,
                    value2
            );
        }

        @Test
        @Order(30)
        public void testGetConsulKeyValueAsString() throws ConsulLoginFailedException, JsonProcessingException {
            String returnedValue1 = consulKeyValueApiClient.getValueOfKey(
                    new ConsulCredential(),
                    key1
            );

            String returnedValue2 = consulKeyValueApiClient.getValueOfKey(
                    new ConsulCredential(),
                    key2
            );

            String value1String = objectMapper.writeValueAsString(value1);
            String value2String = objectMapper.writeValueAsString(value2);

            assertTrue(value1String.equals(returnedValue1));
            assertTrue(value2String.equals(returnedValue2));
        }

        @Test
        @Order(40)
        public void testGetConsulKeyValueAsObject() throws ConsulLoginFailedException, JsonProcessingException {
            List<String> returnedValue1 = consulKeyValueApiClient.getValueOfKey(
                    new ConsulCredential(),
                    key1,
                    new TypeReference<>(){}
            );

            List<String> returnedValue2 = consulKeyValueApiClient.getValueOfKey(
                    new ConsulCredential(),
                    key2,
                    new TypeReference<>(){}
            );

            assertIterableEquals(value1, returnedValue1);
            assertIterableEquals(value2, returnedValue2);
        }

        @Test
        @Order(45)
        public void testGetKeysAndExpectTwo() throws ConsulLoginFailedException {
            List<String> keys = consulKeyValueApiClient.getKeys(new ConsulCredential(), "");

            assertEquals(2, keys.size());
        }

        @Test
        @Order(50)
        public void testDeleteConsulKey() throws ConsulLoginFailedException {
            consulKeyValueApiClient.deleteKey(
                    new ConsulCredential(),
                    key1
            );

            consulKeyValueApiClient.deleteKey(
                    new ConsulCredential(),
                    key2
            );

            String returnedValue1 = consulKeyValueApiClient.getValueOfKey(
                    new ConsulCredential(),
                    key1
            );

            String returnedValue2 = consulKeyValueApiClient.getValueOfKey(
                    new ConsulCredential(),
                    key2
            );

            assertEquals(null, returnedValue1);
            assertEquals(null, returnedValue2);
        }

        @Test
        @Order(60)
        public void testRecursiveDeleteOfConsulKeys() throws ConsulLoginFailedException {
            consulKeyValueApiClient.createKey(
                    new ConsulCredential(),
                    key1,
                    value1
            );

            consulKeyValueApiClient.createKey(
                    new ConsulCredential(),
                    key2,
                    value2
            );

            consulKeyValueApiClient.deleteKeyRecursive(new ConsulCredential(), String.valueOf(serviceId));

            List<String> keysAfter = consulKeyValueApiClient.getKeys(new ConsulCredential(), "");

            assertEquals(0, keysAfter.size());
        }
    }
}
