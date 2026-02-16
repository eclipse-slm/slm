package org.eclipse.slm.service_management.service.app.service_repositories;

import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.model.exceptions.KvValueNotFound;
import org.eclipse.slm.common.vault.model.kv.KvSecrets;
import org.eclipse.slm.common.vault.testing.VaultTestContainer;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepository;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepositoryType;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;

@ExtendWith(MockitoExtension.class)
@Testcontainers
public class ServiceOfferingRepositoryHandlerTest {

    private final static Logger LOG = LoggerFactory.getLogger(ServiceOfferingRepositoryHandlerTest.class);

    private static final String VAULT_TOKEN = "myroot";

    private VaultClient vaultAdminClient;

    private ServiceRepositoryHandler serviceRepositoryHandler;

    @Container
    private static final VaultTestContainer vaultContainer = new VaultTestContainer();

    private static final String VAULT_SECRET_ENGINE_NAME = "service-repositories";

    @BeforeEach
    public void initEach() {
        this.vaultAdminClient = vaultContainer.getVaultAdminClient();
        this.vaultAdminClient.kv(VAULT_SECRET_ENGINE_NAME).createKvSecretEngine();

        this.serviceRepositoryHandler = new ServiceRepositoryHandler(vaultContainer.getVaultClientFactory());
    }

    @Test
    @DisplayName("Get empty repositories list")
    public void getEmptyRepositoriesList() throws ServiceRepositoryNotFound {
        var serviceRepositories = this.serviceRepositoryHandler.getRepositoriesOfServiceVendor(UUID.randomUUID());

        assertThat(serviceRepositories).hasSize(0);
    }

    @Test
    @DisplayName("Get repositories list with one entry")
    public void getRepositoryListWithOneEntry() throws ServiceRepositoryNotFound {
        var serviceVendorId = UUID.randomUUID();
        var serviceRepository = this.getTestServiceRepository(serviceVendorId, UUID.randomUUID());
        this.serviceRepositoryHandler.createOrUpdateServiceRepository(serviceRepository);

        var serviceRepositories = this.serviceRepositoryHandler.getRepositoriesOfServiceVendor(serviceVendorId);

        RecursiveComparisonConfiguration comparisonConfiguration = RecursiveComparisonConfiguration.builder()
                .withIgnoredFields("password")
                .build();
        assertThat(serviceRepositories).hasSize(1).usingRecursiveFieldByFieldElementComparator(comparisonConfiguration)
                .contains(serviceRepository);
    }

    @Test
    @DisplayName("Get repository by id")
    public void getRepositoryById() throws ServiceRepositoryNotFound {
        var serviceVendorId = UUID.randomUUID();
        var serviceRepositoryId = UUID.randomUUID();
        var serviceRepositoryCreated = this.getTestServiceRepository(serviceVendorId, serviceRepositoryId);
        this.serviceRepositoryHandler.createOrUpdateServiceRepository(serviceRepositoryCreated);

        var serviceRepositoryReceived = this.serviceRepositoryHandler.getServiceRepository(serviceVendorId, serviceRepositoryId);

        RecursiveComparisonConfiguration comparisonConfiguration = RecursiveComparisonConfiguration.builder()
                .withIgnoredFields("password")
                .build();
        assertThat(serviceRepositoryReceived).usingRecursiveComparison(comparisonConfiguration).isEqualTo(serviceRepositoryCreated);
    }

    @Test
    @DisplayName("Get non existing repository by id")
    public void getNonExistingRepositoryById() {
        assertThatExceptionOfType(ServiceRepositoryNotFound.class)
                .isThrownBy(() -> {
                    var serviceRepositoryReceived =
                            this.serviceRepositoryHandler.getServiceRepository(UUID.randomUUID(), UUID.randomUUID());
                });
    }

    @Test
    @DisplayName("Create or update repository")
    public void createOrUpdateRepository() throws KvValueNotFound {
        var serviceRepository = this.getTestServiceRepository(UUID.randomUUID(), UUID.randomUUID());
        this.serviceRepositoryHandler.createOrUpdateServiceRepository(serviceRepository);

        var kvPath = this.getRepositoryKvPath(serviceRepository.getServiceVendorId(), serviceRepository.getId());
        var secretsOptional = this.vaultAdminClient.kv(VAULT_SECRET_ENGINE_NAME).getSecretsOfPath(kvPath);

        assertThat(secretsOptional)
                .isPresent()
                .get()
                .extracting(KvSecrets::getData, MAP)
                .containsEntry("address", serviceRepository.getAddress())
                .containsEntry("username", serviceRepository.getUsername())
                .containsEntry("password", serviceRepository.getPassword())
                .containsEntry("serviceVendorId", serviceRepository.getServiceVendorId().toString())
                .containsEntry("type", serviceRepository.getServiceRepositoryType().toString());
    }

    @Test
    @DisplayName("Delete repository")
    public void deleteRepository() throws ServiceRepositoryNotFound {
        var serviceVendorId = UUID.randomUUID();
        var serviceRepositoryId = UUID.randomUUID();
        var serviceRepository = this.getTestServiceRepository(serviceVendorId, serviceRepositoryId);
        this.serviceRepositoryHandler.createOrUpdateServiceRepository(serviceRepository);

        var serviceRepositories = this.serviceRepositoryHandler.getRepositoriesOfServiceVendor(serviceVendorId);
        RecursiveComparisonConfiguration comparisonConfiguration = RecursiveComparisonConfiguration.builder()
                .withIgnoredFields("password")
                .build();
        assertThat(serviceRepositories).hasSize(1).usingRecursiveFieldByFieldElementComparator(comparisonConfiguration)
                .contains(serviceRepository);

        this.serviceRepositoryHandler.deleteServiceRepository(serviceRepository);
        serviceRepositories = this.serviceRepositoryHandler.getRepositoriesOfServiceVendor(serviceVendorId);
        assertThat(serviceRepositories).hasSize(0);
    }

    private ServiceRepository getTestServiceRepository(UUID serviceVendorId, UUID serviceRepositoryId) {
        var serviceRepository = new ServiceRepository(serviceRepositoryId);
        serviceRepository.setServiceVendorId(serviceVendorId);
        serviceRepository.setUsername("testUser");
        serviceRepository.setPassword("testPassword");
        serviceRepository.setAddress("http://test-repo.org");
        serviceRepository.setServiceRepositoryType(ServiceRepositoryType.DOCKER_REGISTRY);

        return serviceRepository;
    }

    private String getRepositoryKvPath(UUID serviceVendorId, UUID serviceRepositoryId) {
        return ServiceRepositoryHandler.getRepositorySecretPath(serviceVendorId, serviceRepositoryId);
    }
}
