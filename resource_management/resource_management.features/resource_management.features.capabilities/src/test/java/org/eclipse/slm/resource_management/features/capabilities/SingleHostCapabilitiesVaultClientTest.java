package org.eclipse.slm.resource_management.features.capabilities;

import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.exceptions.VaultPolicyNotFoundException;
import org.eclipse.slm.common.vault.testing.VaultTestContainer;
import org.eclipse.slm.common.vault.testing.VaultTestContainerInitializer;
import org.eclipse.slm.resource_management.features.capabilities.persistence.SingleHostCapabilitiesVaultClient;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Import(SingleHostCapabilitiesVaultClientTestData.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class SingleHostCapabilitiesVaultClientTest {

    @Container private static final VaultTestContainer vaultContainer = new VaultTestContainer();
    private static VaultClient vaultAdminClient;
    private static SingleHostCapabilitiesVaultClient singleHostCapabilitiesVaultClient;

    @BeforeAll
    public static void beforeAll() {
        var vaultTestContainerInitializer = new VaultTestContainerInitializer(vaultContainer, false);
        vaultTestContainerInitializer.initUserGroup(SingleHostCapabilitiesVaultClientTestData.TEST_GROUP_ID);

        vaultAdminClient = vaultContainer.getVaultAdminClient();
        vaultAdminClient.kv(SingleHostCapabilitiesVaultClient.VAULT_SECRETS_ENGINE_NAME).createKvSecretEngine();
        singleHostCapabilitiesVaultClient = new SingleHostCapabilitiesVaultClient(vaultAdminClient);
    }

    @Test
    @Order(10)
    public void testAddSecretConfigParametersOfSingleHostCapabilityService() {
        // Act
        singleHostCapabilitiesVaultClient.addSingleHostCapabilityServiceSecrets(
                SingleHostCapabilitiesVaultClientTestData.singleHostCapabilityService,
                SingleHostCapabilitiesVaultClientTestData.configParameters,
                SingleHostCapabilitiesVaultClientTestData.TEST_GROUP_ID
        );
        // Assert | Secrets added
        var secrets = singleHostCapabilitiesVaultClient.getSingleHostCapabilityServiceSecrets(SingleHostCapabilitiesVaultClientTestData.singleHostCapabilityService.getServiceId());
        assertThat(secrets)
                .hasSize(1)
                .containsEntry("password", SingleHostCapabilitiesVaultClientTestData.configParameters.get("password"));
        // Assert | Policy created and assigned to user group
        var capabilityPolicyName = SingleHostCapabilitiesVaultClient
                .getCapabilityServicePolicyName(SingleHostCapabilitiesVaultClientTestData.singleHostCapabilityService.getServiceId());
        var capabilityPolicy =  vaultAdminClient.acl().getPolicy(capabilityPolicyName);
        assertThat(capabilityPolicy).isNotNull();
        var vaultGroup = vaultAdminClient.acl().getGroupByName(SingleHostCapabilitiesVaultClientTestData.TEST_GROUP_ID);
        assertThat(vaultGroup.getPolicies()).contains(capabilityPolicyName);

    }

    @Test
    @Order(20)
    public void testDeleteSecretConfigParametersOfSingleHostCapabilityService() {
        // Act
        singleHostCapabilitiesVaultClient.deleteSingleHostCapabilityServiceSecrets(SingleHostCapabilitiesVaultClientTestData.singleHostCapabilityService.getServiceId());
        // Assert | Secrets deleted
        var secrets = singleHostCapabilitiesVaultClient.getSingleHostCapabilityServiceSecrets(
                SingleHostCapabilitiesVaultClientTestData.singleHostCapabilityService.getServiceId()
        );
        assertEquals(0, secrets.size());
        // Assert | Policy deleted
        var capabilityPolicyName = SingleHostCapabilitiesVaultClient
                .getCapabilityServicePolicyName(SingleHostCapabilitiesVaultClientTestData.singleHostCapabilityService.getServiceId());
        assertThatThrownBy(() -> vaultAdminClient.acl().getPolicy(capabilityPolicyName))
                .isInstanceOf(VaultPolicyNotFoundException.class);
        var vaultGroup = vaultAdminClient.acl().getGroupByName(SingleHostCapabilitiesVaultClientTestData.TEST_GROUP_ID);
        assertThat(vaultGroup.getPolicies()).doesNotContain(capabilityPolicyName);
    }
}
