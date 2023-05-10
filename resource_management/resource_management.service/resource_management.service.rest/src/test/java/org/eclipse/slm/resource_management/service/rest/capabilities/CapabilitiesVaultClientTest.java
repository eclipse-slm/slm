package org.eclipse.slm.resource_management.service.rest.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.resource_management.persistence.api.CapabilityJpaRepository;
import org.eclipse.slm.resource_management.service.rest.handler.ClusterHandlerITConfig;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesVaultClient;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        SingleHostCapabilitiesVaultClient.class,
        CapabilityUtil.class,
        CapabilityJpaRepository.class,
        ConsulNodesApiClient.class,
        ConsulServicesApiClient.class,
        ConsulAclApiClient.class,
        ResourcesVaultClient.class,
        VaultClient.class,
        ObjectMapper.class,
        RestTemplate.class
})
@ActiveProfiles("test")
@Import(CapabilitiesVaultClientTestConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class CapabilitiesVaultClientTest {
    @Autowired
    SingleHostCapabilitiesVaultClient singleHostCapabilitiesVaultClient;
    @Autowired
    CapabilitiesVaultClientTestConfig capabilitiesVaultClientTestConfig;
    @MockBean
    CapabilityJpaRepository capabilityJpaRepository;

    @Container
    static GenericContainer<?> vaultDockerContainer = new GenericContainer<>(DockerImageName.parse("vault:"+ ClusterHandlerITConfig.VAULT_VERSION))
            .withExposedPorts(CapabilitiesVaultClientTestConfig.VAULT_PORT)
            .withEnv("VAULT_DEV_ROOT_TOKEN_ID", CapabilitiesVaultClientTestConfig.VAULT_TOKEN)
            .waitingFor(new HostPortWaitStrategy());

    @DynamicPropertySource
    static void vaultProperties(DynamicPropertyRegistry registry) {
        registry.add("vault.port", vaultDockerContainer::getFirstMappedPort);
//        registry.add("vault.host", () -> CapabilitiesVaultClientTestConfig.VAULT_HOST);
        registry.add("vault.token", () -> CapabilitiesVaultClientTestConfig.VAULT_TOKEN);
        registry.add("vault.authentication", () -> "token");
    }

    @Autowired
    public void CapabilitiesVaultClientTest() {
        capabilitiesVaultClientTestConfig.createResourcesSecretEngine();
        capabilitiesVaultClientTestConfig.createRemoteAccessService();
    }

    @Test
    @Order(10)
    public void testGetSecretConfigParametersOfSingleHostCapabilityServiceExpectNone() {
        Map<String, String> secrets = singleHostCapabilitiesVaultClient.getSingleHostCapabilityServiceSecrets(
                new VaultCredential(),
                capabilitiesVaultClientTestConfig.getSingleHostCapabilityService()
        );

        assertEquals(0, secrets.size());
    }

    @Test
    @Order(20)
    public void testAddSecretConfigParametersOfSingleHostCapabilityService() {
        singleHostCapabilitiesVaultClient.addSingleHostCapabilityServiceSecrets(
                new VaultCredential(),
                capabilitiesVaultClientTestConfig.getSingleHostCapabilityService(),
                capabilitiesVaultClientTestConfig.resourceId,
                capabilitiesVaultClientTestConfig.configParameter
        );

        Map<String, String> secrets = singleHostCapabilitiesVaultClient.getSingleHostCapabilityServiceSecrets(
                new VaultCredential(),
                capabilitiesVaultClientTestConfig.getSingleHostCapabilityService()
        );

        assertEquals(1, secrets.size());
        assertEquals(
                capabilitiesVaultClientTestConfig.configParameter.get("password"),
                secrets.get("password")
        );
    }

    @Test
    @Order(30)
    public void testDeleteSecretConfigParametersOfSingleHostCapabilityService() {
        singleHostCapabilitiesVaultClient.deleteSingleHostCapabilityServiceSecrets(
                new VaultCredential(),
                capabilitiesVaultClientTestConfig.getSingleHostCapabilityService()
        );

        Map<String, String> secrets = singleHostCapabilitiesVaultClient.getSingleHostCapabilityServiceSecrets(
                new VaultCredential(),
                capabilitiesVaultClientTestConfig.getSingleHostCapabilityService()
        );

        assertEquals(0, secrets.size());
    }
}
