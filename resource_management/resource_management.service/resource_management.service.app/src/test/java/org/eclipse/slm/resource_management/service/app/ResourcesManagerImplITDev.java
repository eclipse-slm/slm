package org.eclipse.slm.resource_management.service.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.*;
import org.eclipse.slm.common.consul.model.acl.Policy;
import org.eclipse.slm.common.consul.model.acl.Role;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.common.vault.client.VaultCredentialType;
import org.eclipse.slm.notification_service.messaging.NotificationMessageSender;
import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.adapters.ResourcesVaultClient;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.location.Location;
import org.eclipse.slm.resource_management.common.location.LocationJpaRepository;
import org.eclipse.slm.resource_management.common.remote_access.ConnectionType;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessManager;
import org.eclipse.slm.resource_management.common.resources.ResourcesManager;
import org.eclipse.slm.resource_management.features.capabilities.CapabilitiesService;
import org.eclipse.slm.resource_management.features.capabilities.CapabilityUtil;
import org.eclipse.slm.resource_management.features.capabilities.exceptions.CapabilityNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
import org.eclipse.slm.resource_management.features.capabilities.persistence.SingleHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.common.resources.BasicResource;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        ResourcesManager.class,
        RemoteAccessManager.class,
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
@ActiveProfiles("test")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Disabled
public class ResourcesManagerImplITDev {
    //region MockBeans
    @MockBean
    NotificationMessageSender notificationMessageSender;
    @MockBean
    ConsulHealthApiClient consulHealthApiClient;
    @MockBean
    KeycloakAdminClient keycloakAdminClient;
    @MockBean
    CapabilitiesService capabilitiesService;
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
        Integer vaultPort = ResourcesManagerITDevConfig.dockerCompose.getServicePort(ResourcesManagerITDevConfig.VAULT_SERVICE_NAME, ResourcesManagerITDevConfig.VAULT_PORT);
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
    RemoteAccessManager remoteAccessManager;
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
        public void getBasicResourcesWithRemoteAccessServiceExpectNoReturn() throws ResourceNotFoundException {
            List<BasicResource> resources = resourcesManager.getResources(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken
            );

            //size has to be 1 because consul itself is also registered as node
            assertEquals(1, resources.size());
        }

        @Test
        @Order(15)
        public void getBasicResourceWithRemoteAccessServiceByResourceIdExpectNoReturn() throws ResourceNotFoundException {
            BasicResource resourceSsh = resourcesManager.getResourceByIdOrThrow(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken,
                    basicResourceSshId
            );

            assertNull(resourceSsh);

            BasicResource resourceWinRm = resourcesManager.getResourceByIdOrThrow(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken,
                    basicResourceWinRmId
            );

            assertNull(resourceWinRm);
        }

        @Test
        @Order(20)
        public void createBasicResourcesWithRemoteAccessService() throws ResourceNotFoundException, CapabilityNotFoundException {
            var digitalNameplate = new DigitalNameplateV3.Builder("", "", "", "").build();

            BasicResource basicResourceSsh = resourcesManager.addResource(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken,
                    basicResourceSshId,
                    null,
                    "test-host-ssh",
                    "1.2.3.4",
                    null,
                    null,
                    digitalNameplate
            );
            remoteAccessManager.addUsernamePasswordRemoteAccessService(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken.getToken().getSubject(),
                    basicResourceSshId,
                    ConnectionType.ssh,
                    ConnectionType.ssh.getDefaultPort(),
                    username,
                    password
            );

            assertEquals(basicResourceSshId, basicResourceSsh.getId());

            BasicResource basicResourceWinrm = resourcesManager.addResource(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken,
                    basicResourceWinRmId,
                    null,
                    "test-host-winrm",
                    "1.2.3.5",
                    null,
                    null,
                    digitalNameplate
            );
            remoteAccessManager.addUsernamePasswordRemoteAccessService(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken.getToken().getSubject(),
                    basicResourceSshId,
                    ConnectionType.WinRM,
                    ConnectionType.WinRM.getDefaultPort(),
                    username,
                    password
            );

            assertEquals(basicResourceWinRmId, basicResourceWinrm.getId());
        }

        @Test
        @Order(30)
        public void getBasicResourcesWithRemoteAccessService() throws ResourceNotFoundException {
            List<BasicResource> resources = resourcesManager.getResources(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken
            );

            //size has to be +1 because consul itself is also registered as node
            assertEquals(2+1, resources.size());
        }

        @Test
        @Order(40)
        public void getBasicResourceWithRemoteAccessServiceByResourceId() throws ResourceNotFoundException {
            //region SSH
            BasicResource newResourceSsh = resourcesManager.getResourceByIdOrThrow(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken,
                    basicResourceSshId
            );
            var remAccServSshIds = newResourceSsh.getRemoteAccessIds();
            ConnectionType conTypeSsh = ConnectionType.ssh;

            assertThat(newResourceSsh.getId().equals(basicResourceSshId));
            assertThat(remAccServSshIds.size()).isEqualTo(1);
//            assertEquals(conTypeSsh, remAccServSsh.getConnectionType());
//            assertEquals(conTypeSsh.getDefaultPort(), remAccServSsh.getPort());
            //endregion

            //region WinRM
            var newResourceWinRM = resourcesManager.getResourceByIdOrThrow(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken,
                    basicResourceWinRmId
            );
            var remoteAccessWinRMIds = newResourceWinRM.getRemoteAccessIds();
            ConnectionType conTypeWinrm = ConnectionType.WinRM;

            System.out.println(basicResourceWinRmId);

            assertEquals(basicResourceWinRmId, newResourceWinRM.getId());
            assertThat(remoteAccessWinRMIds.size()).isEqualTo(1);
//            assertEquals(conTypeWinrm, remoteAccessWinRMIds.getConnectionType());
//            assertEquals(conTypeWinrm.getDefaultPort(), remoteAccessWinRMIds.getPort());
            //endregion
        }

        @Test
        @Order(50)
        public void addSingleHostDeploymentCapabilityToResource() throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
            singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                    new ConsulCredential(),
                    ResourcesManagerITDevConfig.dockerDeploymentCapability,
                    basicResourceSshId,
                    CapabilityServiceStatus.INSTALL,
                    false,
                    new HashMap<>()
            );
        }

        @Test
        @Order(60)
        public void deleteBasicResourceWithRemoteAccessServiceByResourceId() throws ConsulLoginFailedException, ResourceNotFoundException {
            List<Node> nodesBefore = consulNodesApiClient.getNodes(ResourcesManagerITDevConfig.cCred);
            List<UUID> ids = Arrays.asList(basicResourceSshId, basicResourceWinRmId);

            for(UUID id : ids) {
                resourcesManager.deleteResource(
                        ResourcesManagerITDevConfig.jwtAuthenticationToken,
                        id
                );

                var resource = resourcesManager.getResourceByIdOrThrow(
                        ResourcesManagerITDevConfig.jwtAuthenticationToken,
                        id
                );

                assertNull(resource);
            }

            //region Assert
            List<Policy> policiesAfter = consulAclApiClient.getPolicies(new ConsulCredential());
            List<Role> rolesAfter = consulAclApiClient.getRoles(new ConsulCredential());
            List<Node> nodesAfter = consulNodesApiClient.getNodes(ResourcesManagerITDevConfig.cCred);
            Map<String, List<String>> servicesAfter = consulServicesApiClient.getServices(ResourcesManagerITDevConfig.cCred);
            List<String> keysAfter = consulKeyValueApiClient.getKeys(ResourcesManagerITDevConfig.cCred,"");
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
        public void getBasicResourcesWithoutRemoteAccessServiceExpectNoReturn() throws ResourceNotFoundException {
            var resources = resourcesManager.getResources(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken
            );

            //size has to be 1 because consul itself is also registered as node
            assertEquals(1, resources.size());
        }

        @Test
        @Order(20)
        public void createBasicResourcesWithoutRemoteAccessService() throws ResourceNotFoundException, CapabilityNotFoundException {
            var digitalNameplate = new DigitalNameplateV3.Builder("", "", "", "").build();

            var basicResourceWithNoCredentials = resourcesManager.addResource(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken,
                    uuid,
                    null,
                    hostname,
                    ip,
                    null,
                    null,
                    digitalNameplate
            );

            assertEquals(uuid, basicResourceWithNoCredentials.getId());
        }

        @Test
        @Order(30)
        public void getBasicResourcesWithoutRemoteAccessServiceExpectOneResource() throws ResourceNotFoundException {
            var resources = resourcesManager.getResources(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken
            );

            //size has to be +1 because consul itself is also registered as node
            assertEquals(1+1, resources.size());
        }

        @Test
        @Order(40)
        public void getBasicResourceWithoutRemoteAccessServiceByResourceId() throws ResourceNotFoundException {
            var resource = resourcesManager.getResourceByIdOrThrow(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken,
                    uuid
            );
            var remAccServSsh = resource.getRemoteAccessIds();

            assertEquals(uuid, resource.getId());
            assertEquals(0, remAccServSsh.size());
        }

        @Test
        @Order(50)
        public void deleteBasicResourceWithoutRemoteAccessServiceByResourceId() throws ConsulLoginFailedException, ResourceNotFoundException {
            List<Node> nodesBefore = consulNodesApiClient.getNodes(ResourcesManagerITDevConfig.cCred);
            Map<String, List<String>> servicesBefore = consulServicesApiClient.getServices(ResourcesManagerITDevConfig.cCred);

            resourcesManager.deleteResource(ResourcesManagerITDevConfig.jwtAuthenticationToken,uuid);

            var resource = resourcesManager.getResourceByIdOrThrow(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken,
                    uuid
            );

            assertNull(resource);

            //region Assert
            List<Policy> policiesAfter = consulAclApiClient.getPolicies(new ConsulCredential());
            List<Role> rolesAfter = consulAclApiClient.getRoles(new ConsulCredential());
            List<Node> nodesAfter = consulNodesApiClient.getNodes(ResourcesManagerITDevConfig.cCred);
            Map<String, List<String>> servicesAfter = consulServicesApiClient.getServices(ResourcesManagerITDevConfig.cCred);
            List<String> keysAfter = consulKeyValueApiClient.getKeys(ResourcesManagerITDevConfig.cCred,"");
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
        public void registerResourceWithLocation() throws ConsulLoginFailedException, ResourceNotFoundException, CapabilityNotFoundException {
            Mockito
                    .doReturn(Optional.of(location))
                    .when(locationJpaRepository)
                    .findById(location.getId());

            var basicResourceWithLocation = resourcesManager.addResource(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken,
                    resourceId,
                    null,
                    hostname,
                    ip,
                    null,
                    null,
                    digitalNameplate
            );

            resourcesManager.setLocationOfResource(resourceId, location.getId());

            assertEquals(location.getId(), basicResourceWithLocation.getLocationId());
        }

        @Test
        @Order(20)
        public void registerResourceWithoutLocationExpectLocationNull() throws ResourceNotFoundException, CapabilityNotFoundException {
            BasicResource basicResourceWithoutLocation = resourcesManager.addResource(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken,
                    resourceId,
                    null,
                    hostname,
                    ip,
                    null,
                    null,
                    digitalNameplate
            );

            assertNull(basicResourceWithoutLocation.getLocationId());
        }

        @Test
        @Order(30)
        public void registerResourceWithWrongLocationIdExpectLocationNull() throws ConsulLoginFailedException, ResourceNotFoundException, CapabilityNotFoundException {
            BasicResource basicResourceWithWrongLocationId = resourcesManager.addResource(
                    ResourcesManagerITDevConfig.jwtAuthenticationToken,
                    resourceId,
                    null,
                    hostname,
                    ip,
                    null,
                    null,
                    digitalNameplate
            );
            resourcesManager.setLocationOfResource(resourceId, UUID.randomUUID());

            assertNotNull(basicResourceWithWrongLocationId);
            assertNull(basicResourceWithWrongLocationId.getLocationId());
        }
    }
}
