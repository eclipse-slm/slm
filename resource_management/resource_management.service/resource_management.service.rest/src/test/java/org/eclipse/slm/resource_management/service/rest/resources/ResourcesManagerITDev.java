package org.eclipse.slm.resource_management.service.rest.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.*;
import org.eclipse.slm.common.consul.model.acl.Policy;
import org.eclipse.slm.common.consul.model.acl.Role;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakUtil;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.common.vault.client.VaultCredentialType;
import org.eclipse.slm.notification_service.service.client.NotificationServiceClient;
import org.eclipse.slm.resource_management.model.capabilities.CapabilityNotFoundException;
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.model.resource.BasicResource;
import org.eclipse.slm.resource_management.model.resource.ConnectionType;
import org.eclipse.slm.resource_management.model.resource.Location;
import org.eclipse.slm.resource_management.model.resource.RemoteAccessService;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.persistence.api.CapabilityJpaRepository;
import org.eclipse.slm.resource_management.persistence.api.LocationJpaRepository;
import org.eclipse.slm.resource_management.service.rest.aas.resources.digitalnameplate.DigitalNameplateV3;
import org.eclipse.slm.resource_management.service.rest.capabilities.CapabilitiesConsulClient;
import org.eclipse.slm.resource_management.service.rest.capabilities.CapabilitiesManager;
import org.eclipse.slm.resource_management.service.rest.capabilities.CapabilityUtil;
import org.eclipse.slm.resource_management.service.rest.capabilities.SingleHostCapabilitiesConsulClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        ResourcesManager.class,
        ResourcesConsulClient.class,
        SingleHostCapabilitiesConsulClient.class,
        ConsulNodesApiClient.class,
        ConsulAclApiClient.class,
        ConsulServicesApiClient.class,
        ConsulKeyValueApiClient.class,
        ConsulHealthApiClient.class,
        CapabilityUtil.class,
        CapabilityJpaRepository.class,
        ResourcesVaultClient.class,
        VaultClient.class,
        RestTemplate.class,
        ObjectMapper.class
})
@ContextConfiguration(initializers = {ConfigDataApplicationContextInitializer.class} )
@TestPropertySource(properties = { "spring.config.location=classpath:application-test.yml" })
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Disabled
public class ResourcesManagerITDev {
    private ResourcesManagerITDevConfig config = new ResourcesManagerITDevConfig();
    //region MockBeans
    @MockBean
    NotificationServiceClient notificationServiceClient;
    @MockBean
    ConsulHealthApiClient consulHealthApiClient;
    @MockBean
    KeycloakUtil keycloakUtil;
    @MockBean
    CapabilitiesManager capabilitiesManager;
    @MockBean
    CapabilitiesConsulClient capabilitiesConsulClient;
    @MockBean
    CapabilityJpaRepository capabilityJpaRepository;
    @MockBean
    LocationJpaRepository locationJpaRepository;
    //endregion

    @BeforeEach
    public void beforeEach(
            @Autowired ConsulNodesApiClient consulNodesApiClient,
            @Autowired ConsulAclApiClient consulAclApiClient,
            @Autowired ConsulServicesApiClient consulServicesApiClient,
            @Autowired ConsulKeyValueApiClient consulKeyValueApiClient
    ) {
        Integer vaultPort = config.dockerCompose.getServicePort(config.VAULT_SERVICE_NAME, config.VAULT_PORT);
        vaultClient.setPort(vaultPort);

        vaultClient.createResourcesKVSecretEngine(
                new VaultCredential(VaultCredentialType.CONSUL_TOKEN, "myroot")
        );
    }

    //region Autowiring
    @Autowired
    ConsulNodesApiClient consulNodesApiClient;
    @Autowired
    ConsulServicesApiClient consulServicesApiClient;
    @Autowired
    ConsulKeyValueApiClient consulKeyValueApiClient;
    @Autowired
    ConsulAclApiClient consulAclApiClient;
    @Autowired
    ResourcesManager resourcesManager;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ResourcesConsulClient resourcesConsulClient;
    @Autowired
    SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient;
    @Autowired
    CapabilityUtil capabilityUtil;
    @SpyBean
    VaultClient vaultClient;
    @Autowired
    ResourcesVaultClient resourcesVaultClient;
    //endregion

    @Nested
    @Order(10)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class registerResourcesWithRemoteAccess {
        private static final String username = "username";
        private static final String password = "password";

        private static final UUID basicResourceSshId = UUID.randomUUID();
        private static final UUID basicResourceWinRmId = UUID.randomUUID();

        @BeforeEach
        public void beforeEach() {
            Mockito
                    .doReturn(Map.of(username, username, password, password))
                    .when(vaultClient)
                    .getKvContent(Mockito.any(), Mockito.any());
        }

        @Test
        @Order(10)
        public void getBasicResourcesWithRemoteAccessServiceExpectNoReturn() throws ConsulLoginFailedException, JsonProcessingException, ResourceNotFoundException {
            List<BasicResource> resources = resourcesManager.getResourcesWithCredentialsByRemoteAccessService(
                    config.jwtAuthenticationToken
            );

            //size has to be 1 because consul itself is also registered as node
            assertEquals(1, resources.size());
        }

        @Test
        @Order(15)
        public void getBasicResourceWithRemoteAccessServiceByResourceIdExpectNoReturn() throws ConsulLoginFailedException, JsonProcessingException, ResourceNotFoundException {
            BasicResource resourceSsh = resourcesManager.getResourceWithCredentialsByRemoteAccessService(
                    config.jwtAuthenticationToken,
                    basicResourceSshId
            );

            assertNull(resourceSsh);

            BasicResource resourceWinRm = resourcesManager.getResourceWithCredentialsByRemoteAccessService(
                    config.jwtAuthenticationToken,
                    basicResourceWinRmId
            );

            assertNull(resourceWinRm);
        }

        @Test
        @Order(20)
        public void createBasicResourcesWithRemoteAccessService() throws ConsulLoginFailedException, ResourceNotFoundException, JsonProcessingException, IllegalAccessException, CapabilityNotFoundException, SSLException {
            var digitalNameplate = new DigitalNameplateV3.Builder("", "", "", "").build();

            BasicResource basicResourceSsh = resourcesManager.addExistingResource(
                    config.jwtAuthenticationToken,
                    basicResourceSshId,
                    null,
                    "test-host-ssh",
                    "1.2.3.4",
                    null,
                    digitalNameplate
            );
            resourcesManager.setRemoteAccessOfResource(
                    config.jwtAuthenticationToken,
                    basicResourceSshId,
                    username,
                    password,
                    ConnectionType.ssh,
                    ConnectionType.ssh.getDefaultPort()
            );

            assertEquals(basicResourceSshId, basicResourceSsh.getId());

            BasicResource basicResourceWinrm = resourcesManager.addExistingResource(
                    config.jwtAuthenticationToken,
                    basicResourceWinRmId,
                    null,
                    "test-host-winrm",
                    "1.2.3.5",
                    null,
                    digitalNameplate
            );
            resourcesManager.setRemoteAccessOfResource(
                    config.jwtAuthenticationToken,
                    basicResourceWinRmId,
                    username,
                    password,
                    ConnectionType.WinRM,
                    ConnectionType.WinRM.getDefaultPort()
            );

            assertEquals(basicResourceWinRmId, basicResourceWinrm.getId());
        }

        @Test
        @Order(30)
        public void getBasicResourcesWithRemoteAccessService() throws ConsulLoginFailedException, JsonProcessingException, ResourceNotFoundException {
            List<BasicResource> resources = resourcesManager.getResourcesWithCredentialsByRemoteAccessService(
                    config.jwtAuthenticationToken
            );

            //size has to be +1 because consul itself is also registered as node
            assertEquals(2+1, resources.size());
        }

        @Test
        @Order(40)
        public void getBasicResourceWithRemoteAccessServiceByResourceId() throws ConsulLoginFailedException, ResourceNotFoundException, JsonProcessingException {
            //region SSH
            BasicResource newResourceSsh = resourcesManager.getResourceWithCredentialsByRemoteAccessService(
                    config.jwtAuthenticationToken,
                    basicResourceSshId
            );
            RemoteAccessService remAccServSsh = newResourceSsh.getRemoteAccessService();
            ConnectionType conTypeSsh = ConnectionType.ssh;

            assertEquals(basicResourceSshId, newResourceSsh.getId());
            assertNotNull(remAccServSsh);
            assertEquals(conTypeSsh, remAccServSsh.getConnectionType());
            assertEquals(conTypeSsh.getDefaultPort(), remAccServSsh.getPort());
            //endregion

            //region WinRM
            BasicResource newResourceWinRM = resourcesManager.getResourceWithCredentialsByRemoteAccessService(
                    config.jwtAuthenticationToken,
                    basicResourceWinRmId
            );
            RemoteAccessService remoteAccessServiceWinRM = newResourceWinRM.getRemoteAccessService();
            ConnectionType conTypeWinrm = ConnectionType.WinRM;

            System.out.println(basicResourceWinRmId);

            assertEquals(basicResourceWinRmId, newResourceWinRM.getId());
            assertNotNull(remoteAccessServiceWinRM);
            assertEquals(conTypeWinrm, remoteAccessServiceWinRM.getConnectionType());
            assertEquals(conTypeWinrm.getDefaultPort(), remoteAccessServiceWinRM.getPort());
            //endregion
            return;
        }

        @Test
        @Order(50)
        public void addSingleHostDeploymentCapabilityToResource() throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
            singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                    new ConsulCredential(),
                    config.dockerDeploymentCapability,
                    basicResourceSshId,
                    CapabilityServiceStatus.INSTALL,
                    false,
                    new HashMap<>()
            );
        }

        @Test
        @Order(60)
        public void deleteBasicResourceWithRemoteAccessServiceByResourceId() throws ConsulLoginFailedException, ResourceNotFoundException, JsonProcessingException {
            List<Node> nodesBefore = consulNodesApiClient.getNodes(config.cCred);
            List<UUID> ids = Arrays.asList(basicResourceSshId, basicResourceWinRmId);

            for(UUID id : ids) {
                resourcesManager.deleteResource(
                        config.jwtAuthenticationToken,
                        id
                );

                BasicResource resource = resourcesManager.getResourceWithCredentialsByRemoteAccessService(
                        config.jwtAuthenticationToken,
                        id
                );

                assertNull(resource);
            }

            //region Assert
            List<Policy> policiesAfter = consulAclApiClient.getPolicies(new ConsulCredential());
            List<Role> rolesAfter = consulAclApiClient.getRoles(new ConsulCredential());
            List<Node> nodesAfter = consulNodesApiClient.getNodes(config.cCred);
            Map<String, List<String>> servicesAfter = consulServicesApiClient.getServices(config.cCred);
            List<String> keysAfter = consulKeyValueApiClient.getKeys(config.cCred,"");
            List<String> keysOfResourceSecretEngine = vaultClient.getKeysOfPath(new VaultCredential(), "resources", "");

            assertEquals(
                    nodesBefore.size()-ids.size(),
                    nodesAfter.size()
            );
            // expected == 1 because of consul being a service too
            assertEquals(
                    1,
                    servicesAfter.size()
            );
            assertEquals(
                    0,
                    keysAfter.size()
            );
            // expected = 1 because of policy "global-management"
            assertEquals(1, policiesAfter.size());
            assertEquals(0, rolesAfter.size());
            assertEquals(null, keysOfResourceSecretEngine);
            //endregion
        }


    }

    @Nested
    @Order(20)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class registerResourceWithoutCredentials {
        private static final UUID uuid = UUID.randomUUID();
        private static final String hostname = "host-with-no-credentials";
        private static final String ip = "1.2.3.6";

        @Test
        @Order(10)
        public void getBasicResourcesWithoutRemoteAccessServiceExpectNoReturn() throws ConsulLoginFailedException, JsonProcessingException, ResourceNotFoundException {
            List<BasicResource> resources = resourcesManager.getResourcesWithCredentialsByRemoteAccessService(
                    config.jwtAuthenticationToken
            );

            //size has to be 1 because consul itself is also registered as node
            assertEquals(1, resources.size());
        }

        @Test
        @Order(20)
        public void createBasicResourcesWithoutRemoteAccessService() throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, CapabilityNotFoundException, SSLException, JsonProcessingException {
            var digitalNameplate = new DigitalNameplateV3.Builder("", "", "", "").build();

            var basicResourceWithNoCredentials = resourcesManager.addExistingResource(
                    config.jwtAuthenticationToken,
                    uuid,
                    null,
                    hostname,
                    ip,
                    null,
                    digitalNameplate
            );

            assertEquals(uuid, basicResourceWithNoCredentials.getId());
        }

        @Test
        @Order(30)
        public void getBasicResourcesWithoutRemoteAccessServiceExpectOneResource() throws ConsulLoginFailedException, JsonProcessingException, ResourceNotFoundException {
            List<BasicResource> resources = resourcesManager.getResourcesWithCredentialsByRemoteAccessService(
                    config.jwtAuthenticationToken
            );

            //size has to be +1 because consul itself is also registered as node
            assertEquals(1+1, resources.size());
        }

        @Test
        @Order(40)
        public void getBasicResourceWithoutRemoteAccessServiceByResourceId() throws ConsulLoginFailedException, ResourceNotFoundException, JsonProcessingException {
            BasicResource resource = resourcesManager.getResourceWithCredentialsByRemoteAccessService(
                    config.jwtAuthenticationToken,
                    uuid
            );
            RemoteAccessService remAccServSsh = resource.getRemoteAccessService();

            assertEquals(uuid, resource.getId());
            assertNull(remAccServSsh);
        }

        @Test
        @Order(50)
        public void deleteBasicResourceWithoutRemoteAccessServiceByResourceId() throws ConsulLoginFailedException, ResourceNotFoundException, JsonProcessingException {
            List<Node> nodesBefore = consulNodesApiClient.getNodes(config.cCred);
            Map<String, List<String>> servicesBefore = consulServicesApiClient.getServices(config.cCred);

            resourcesManager.deleteResource(config.jwtAuthenticationToken,uuid);

            BasicResource resource = resourcesManager.getResourceWithCredentialsByRemoteAccessService(
                    config.jwtAuthenticationToken,
                    uuid
            );

            assertNull(resource);

            //region Assert
            List<Policy> policiesAfter = consulAclApiClient.getPolicies(new ConsulCredential());
            List<Role> rolesAfter = consulAclApiClient.getRoles(new ConsulCredential());
            List<Node> nodesAfter = consulNodesApiClient.getNodes(config.cCred);
            Map<String, List<String>> servicesAfter = consulServicesApiClient.getServices(config.cCred);
            List<String> keysAfter = consulKeyValueApiClient.getKeys(config.cCred,"");
            List<String> keysOfResourceSecretEngine = vaultClient.getKeysOfPath(new VaultCredential(), "resources", "");

            assertEquals(
                    nodesBefore.size()-1,
                    nodesAfter.size()
            );
            assertEquals(
                    servicesBefore.size(),
                    servicesAfter.size()
            );
            assertEquals(
                    0,
                    keysAfter.size()
            );
            // expected = 1 because of policy "global-management"
            assertEquals(1, policiesAfter.size());
            assertEquals(0, rolesAfter.size());
            assertEquals(null, keysOfResourceSecretEngine);
            //endregion
        }

    }

    @Nested
    @Order(30)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class registerResourceWithLocation {
        private static UUID resourceId = UUID.randomUUID();
        private static String hostname = "host-with-location";
        private static String ip = "1.2.3.7";
        private static Location location = new Location(UUID.randomUUID(), "test-location");
        private static DigitalNameplateV3 digitalNameplate = new DigitalNameplateV3.Builder("", "", "", "").build();


        @Test
        @Order(10)
        public void registerResourceWithLocation() throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, CapabilityNotFoundException, SSLException, JsonProcessingException {
            Mockito
                    .doReturn(Optional.of(location))
                    .when(locationJpaRepository)
                    .findById(location.getId());

            var basicResourceWithLocation = resourcesManager.addExistingResource(
                    config.jwtAuthenticationToken,
                    resourceId,
                    null,
                    hostname,
                    ip,
                    null,
                    digitalNameplate
            );

            resourcesManager.setLocationOfResource(resourceId, location.getId());

            assertThat(basicResourceWithLocation.getLocation(), samePropertyValuesAs(location));
        }

        @Test
        @Order(20)
        public void registerResourceWithoutLocationExpectLocationNull() throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, CapabilityNotFoundException, SSLException, JsonProcessingException {
            BasicResource basicResourceWithoutLocation = resourcesManager.addExistingResource(
                    config.jwtAuthenticationToken,
                    resourceId,
                    null,
                    hostname,
                    ip,
                    null,
                    digitalNameplate
            );

            assertNull(basicResourceWithoutLocation.getLocation());
        }

        @Test
        @Order(30)
        public void registerResourceWithWrongLocationIdExpectLocationNull() throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, CapabilityNotFoundException, SSLException, JsonProcessingException {
            BasicResource basicResourceWithWrongLocationId = resourcesManager.addExistingResource(
                    config.jwtAuthenticationToken,
                    resourceId,
                    null,
                    hostname,
                    ip,
                    null,
                    digitalNameplate
            );
            resourcesManager.setLocationOfResource(resourceId, UUID.randomUUID());

            assertNotNull(basicResourceWithWrongLocationId);
            assertNull(basicResourceWithWrongLocationId.getLocation());
        }
    }
}
