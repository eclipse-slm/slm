package org.eclipse.slm.service_management.service.rest.service_repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.common.vault.model.KvPath;
import org.eclipse.slm.common.vault.model.exceptions.KvValueNotFound;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepository;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepositoryType;
import org.apache.commons.lang3.ObjectUtils;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Testcontainers
public class ServiceOfferingRepositoryHandlerTest {

    public final static Logger LOG = LoggerFactory.getLogger(ServiceOfferingRepositoryHandlerTest.class);

    private static final String VAULT_TOKEN = "myroot";

    private VaultClient vaultClient;

    private ServiceRepositoryHandler serviceRepositoryHandler;

    @Container
    private final GenericContainer<?> vaultContainer = new GenericContainer<>(DockerImageName.parse("vault:1.11.0"))
            .withExposedPorts(8200)
            .withEnv("VAULT_DEV_ROOT_TOKEN_ID", VAULT_TOKEN);

    @BeforeEach
    public void initEach() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        var vaultMappedHostPort = vaultContainer.getMappedPort(8200);
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            System.out.format("%s=%s%n", envName, env.get(envName));
        }
        var vaultHost = ObjectUtils.defaultIfNull(System.getenv("VAULT_HOST"), "localhost");
        this.vaultClient = new VaultClient(
                "http", vaultHost, vaultMappedHostPort,
                "token", VAULT_TOKEN, "", "", new ObjectMapper());
        this.vaultClient.createKvSecretEngine(new VaultCredential(), "service-repositories");

        this.serviceRepositoryHandler = new ServiceRepositoryHandler(vaultClient);
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
        var secrets = this.vaultClient.getKvContentUsingApplicationProperties(new VaultCredential(), kvPath);

        assertThat(secrets).contains(
                entry("address", serviceRepository.getAddress()),
                entry("username", serviceRepository.getUsername()),
                entry("password", serviceRepository.getPassword()),
                entry("serviceVendorId", serviceRepository.getServiceVendorId().toString()),
                entry("type", serviceRepository.getServiceRepositoryType().toString())
        );
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

    private KvPath getRepositoryKvPath(UUID serviceVendorId, UUID serviceRepositoryId) {
        return new KvPath(ServiceRepositoryHandler.VAULT_SECRET_ENGINE_PATH,
                ServiceRepositoryHandler.getRepositorySecretPath(serviceVendorId, serviceRepositoryId));
    }
}
