package org.eclipse.slm.service_management.service.rest.service_repositories;

import com.c4_soft.springaddons.security.oauth2.test.annotations.*;
import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.service_management.service.rest.AbstractRestControllerIT;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepository;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepositoryType;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorAccessDenied;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.vault.core.VaultOperations;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceRepositoriesRestController.class)
@ContextConfiguration(
    classes = {
        ServiceRepositoriesRestController.class,
        ServiceRepositoryHandler.class,
        VaultClient.class,
        VaultOperations.class
    }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ComponentScan(basePackages = { "org.eclipse.slm.common.vault.client" })
@Testcontainers
@WithMockJwtAuth(
    claims = @OpenIdClaims(
            iss = "https://localhost/auth/realms/fabos",
            preferredUsername = "testUser123",
            otherClaims = @Claims(
                    jsonObjectClaims = @JsonObjectClaim(name = "realm_access", value = "{ \"roles\": [\"slm-admin\"] }"))
    )
)

@DisplayName("Service Repositories (/services/vendors/{serviceVendorId}/repositories)")
public class ServiceRepositoriesRestControllerIT extends AbstractRestControllerIT {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceRepositoriesRestControllerIT.class);

    public String getBasePath(UUID serviceVendorId) {
        return "/services/vendors/{serviceVendorId}/repositories".replace("{serviceVendorId}", serviceVendorId.toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServiceRepositoryHandler serviceRepositoryHandler;

    @Container
    private final GenericContainer<?> vaultContainer = new GenericContainer<>(DockerImageName.parse("vault:1.11.0"))
        .withExposedPorts(8200)
        .withEnv("VAULT_DEV_ROOT_TOKEN_ID", "myroot");

    @Autowired
    private VaultClient vaultClient;

    private final RecursiveComparisonConfiguration comparisonConfiguration = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields("password")
            .build();

    @BeforeEach
    public void init() {
        var vaultMappedHostPort = vaultContainer.getMappedPort(8200);
        this.vaultClient.setPort(vaultMappedHostPort);

        this.vaultClient.createKvSecretEngine(new VaultCredential(), "service-repositories");
    }

    @Nested
    @DisplayName("Get repositories (GET /services/vendors/{serviceVendorId}/repositories)")
    public class getRepositories {

        @Test
        @DisplayName("No permission for service vendor")
        @WithMockJwtAuth
        public void noPermissionForServiceVendor() throws Exception {
            var path = getBasePath(UUID.randomUUID());

            mockMvc.perform(get(path)
                        .with(csrf())
                    )
                   .andExpect(status().isForbidden())
                   .andExpect(result -> assertTrue(result.getResolvedException() instanceof ServiceVendorAccessDenied));
        }

        @Test
        @DisplayName("No repos defined")
        public void noRepositoriesDefined() throws Exception {
            var path = getBasePath(UUID.randomUUID());

            var responseBody = mockMvc.perform(get(path)
                        .with(csrf())
                    )
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            var serviceRepositories = objectMapper.readValue(responseBody, new TypeReference<List<ServiceRepository>>() {});

            assertThat(serviceRepositories).hasSize(0);
        }

        @Test
        @DisplayName("One repo defined")
        public void oneRepositoryDefined() throws Exception {
            var serviceVendorId = UUID.randomUUID();
            var serviceRepositoryId = UUID.randomUUID();
            var serviceRepository = getTestServiceRepository(serviceVendorId, serviceRepositoryId);
            var path = getBasePath(serviceVendorId);

            serviceRepositoryHandler.createOrUpdateServiceRepository(serviceRepository);
            var responseBody = mockMvc.perform(get(path)
                            .with(csrf())
                    )
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            var serviceRepositories = objectMapper.readValue(responseBody, new TypeReference<List<ServiceRepository>>() {});

            assertThat(serviceRepositories).hasSize(1)
                    .usingRecursiveFieldByFieldElementComparator(comparisonConfiguration).contains(serviceRepository);
        }

        @Test
        @DisplayName("Get using group permissions")
        @WithMockJwtAuth(
            claims = @OpenIdClaims(
                iss = "https://localhost/auth/realms/fabos",
                preferredUsername = "testUser123",
                otherClaims = @Claims(
                    stringArrayClaims = @StringArrayClaim(name = "groups", value = { "vendor_c12c5a32-c57d-4afd-89f4-9bcd4ae7bee3" })
                )
            )
        )
        public void getUsingGroupPermissions() throws Exception {
            var path = getBasePath(UUID.fromString("c12c5a32-c57d-4afd-89f4-9bcd4ae7bee3"));

            var responseBody = mockMvc.perform(get(path).with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            var serviceRepositories = objectMapper.readValue(responseBody, new TypeReference<List<ServiceRepository>>() {});

            assertThat(serviceRepositories).hasSize(0);
        }
    }

    @Nested
    @DisplayName("Create repository (POST /services/vendors/{serviceVendorId}/repositories)")
    public class addRepository {

        @Test
        @DisplayName("No permission for service vendor")
        @WithMockJwtAuth
        public void noPermissionForServiceVendor() throws Exception {
            var serviceVendorId = UUID.randomUUID();
            var serviceRepositoryId = UUID.randomUUID();
            var serviceRepository = getTestServiceRepository(serviceVendorId, serviceRepositoryId);
            var path = getBasePath(serviceVendorId);

            mockMvc.perform(post(path)
                        .content(asJsonString(serviceRepository))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ServiceVendorAccessDenied));
        }

        @Test
        @DisplayName("Create repository")
        public void createRepository() throws Exception {
            var serviceVendorId = UUID.randomUUID();
            var serviceRepositoryId = UUID.randomUUID();
            var serviceRepository = getTestServiceRepository(serviceVendorId, serviceRepositoryId);
            var path = getBasePath(serviceVendorId);

            var responseBody = mockMvc.perform(post(path)
                        .content(asJsonString(serviceRepository))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                     )
                    .andExpect(status().isOk());
            var serviceRepositories = serviceRepositoryHandler.getRepositoriesOfServiceVendor(serviceVendorId);

            comparisonConfiguration.ignoreFields("id");
            assertThat(serviceRepositories).hasSize(1)
                    .usingRecursiveFieldByFieldElementComparator(comparisonConfiguration).contains(serviceRepository);
        }
    }

    @Nested
    @DisplayName("Create or update repository (PUT /services/vendors/{serviceVendorId}/repositories)")
    public class createOrUpdateRepository {

        @Test
        @DisplayName("No permission for service vendor")
        @WithMockJwtAuth
        public void noPermissionForServiceVendor() throws Exception {
            var serviceVendorId = UUID.randomUUID();
            var serviceRepositoryId = UUID.randomUUID();
            var serviceRepository = getTestServiceRepository(serviceVendorId, serviceRepositoryId);
            var path = getBasePath(serviceVendorId) + "/" + serviceRepositoryId;

            mockMvc.perform(put(path)
                   .content(asJsonString(serviceRepository))
                   .contentType(MediaType.APPLICATION_JSON)
                   .with(csrf())
                )
                   .andExpect(status().isForbidden())
                   .andExpect(result -> assertTrue(result.getResolvedException() instanceof ServiceVendorAccessDenied));
        }

        @Test
        @DisplayName("Create repository")
        public void createRepository() throws Exception {
            var serviceVendorId = UUID.randomUUID();
            var serviceRepositoryId = UUID.randomUUID();
            var serviceRepository = getTestServiceRepository(serviceVendorId, serviceRepositoryId);
            var path = getBasePath(serviceVendorId) + "/" + serviceRepositoryId;

            var responseBody = mockMvc.perform(put(path)
                            .content(asJsonString(serviceRepository))
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf())
                    )
                    .andExpect(status().isOk());
            var serviceRepositories = serviceRepositoryHandler.getRepositoriesOfServiceVendor(serviceVendorId);

            assertThat(serviceRepositories).hasSize(1)
                    .usingRecursiveFieldByFieldElementComparator(comparisonConfiguration).contains(serviceRepository);
        }

        @Test
        @DisplayName("Update repository")
        public void updateRepository() throws Exception {
            var serviceVendorId = UUID.randomUUID();
            var serviceRepositoryId = UUID.randomUUID();
            var serviceRepository = getTestServiceRepository(serviceVendorId, serviceRepositoryId);
            var path = getBasePath(serviceVendorId) + "/" + serviceRepositoryId;

            serviceRepositoryHandler.createOrUpdateServiceRepository(serviceRepository);
            serviceRepository.setAddress("http://other-address.org");
            serviceRepository.setUsername("otherUsername");
            serviceRepository.setPassword("otherPassword");

            var responseBody = mockMvc.perform(put(path)
                            .content(asJsonString(serviceRepository))
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf())
                    )
                    .andExpect(status().isOk());
            var serviceRepositories = serviceRepositoryHandler.getRepositoriesOfServiceVendor(serviceVendorId);

            assertThat(serviceRepositories).hasSize(1)
                    .usingRecursiveFieldByFieldElementComparator(comparisonConfiguration).contains(serviceRepository);
        }
    }

    @Nested
    @DisplayName("Delete repository (DELETE /services/vendors/{serviceVendorId}/repositories)")
    public class deleteRepository {

        @Test
        @DisplayName("No permission for service vendor")
        @WithMockJwtAuth()
        public void noPermissionForServiceVendor() throws Exception {
            var path = getBasePath(UUID.randomUUID()) + "/" + UUID.randomUUID();
            mockMvc.perform(delete(path)
                        .with(csrf())
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ServiceVendorAccessDenied));
        }

        @Test
        @DisplayName("Delete repository")
        public void deleteRepository() throws Exception {
            var serviceVendorId = UUID.randomUUID();
            var serviceRepositoryId = UUID.randomUUID();
            var serviceRepository = getTestServiceRepository(serviceVendorId, serviceRepositoryId);
            var path = getBasePath(serviceVendorId) + "/" + serviceRepositoryId;

            serviceRepositoryHandler.createOrUpdateServiceRepository(serviceRepository);
            var serviceRepositories = serviceRepositoryHandler.getRepositoriesOfServiceVendor(serviceVendorId);
            assertThat(serviceRepositories).hasSize(1)
                    .usingRecursiveFieldByFieldElementComparator(comparisonConfiguration).contains(serviceRepository);

            var responseBody = mockMvc.perform(delete(path)
                            .content(asJsonString(serviceRepository))
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf())
                    )
                    .andExpect(status().isOk());
            serviceRepositories = serviceRepositoryHandler.getRepositoriesOfServiceVendor(serviceVendorId);

            assertThat(serviceRepositories).hasSize(0)
                    .usingRecursiveFieldByFieldElementComparator(comparisonConfiguration).doesNotContain(serviceRepository);

        }
    }

    protected ServiceRepository getTestServiceRepository(UUID serviceVendorId, UUID serviceRepositoryId) {
        var serviceRepository = new ServiceRepository(serviceRepositoryId);
        serviceRepository.setServiceVendorId(serviceVendorId);
        serviceRepository.setUsername("testUser");
        serviceRepository.setPassword("testPassword");
        serviceRepository.setAddress("http://test-repo.org");
        serviceRepository.setServiceRepositoryType(ServiceRepositoryType.DOCKER_REGISTRY);

        return serviceRepository;
    }
}
