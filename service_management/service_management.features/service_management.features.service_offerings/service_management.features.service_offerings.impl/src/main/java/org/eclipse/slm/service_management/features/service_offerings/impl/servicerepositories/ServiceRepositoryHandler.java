package org.eclipse.slm.service_management.features.service_offerings.impl.servicerepositories;

import org.eclipse.slm.service_management.features.service_offerings.api.offerings.ServiceRepositoryNotFound;
import org.eclipse.slm.service_management.features.service_offerings.api.vendors.ServiceVendor;

import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultClientFactory;
import org.eclipse.slm.service_management.features.service_offerings.api.servicerepositories.ServiceRepository;
import org.eclipse.slm.service_management.features.service_offerings.api.servicerepositories.ServiceRepositoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/***
 * Handles {@link ServiceRepository}s.
 */
@Component
public class ServiceRepositoryHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceRepositoryHandler.class);

    public static final String VAULT_SECRET_ENGINE_NAME = "service-repositories";

    private final VaultClientFactory vaultClientFactory;
    private final VaultClient vaultAdminClient;

    /***
     * Instantiates a new {@link ServiceRepositoryHandler}.
     * @param vaultClientFactory {@link VaultClientFactory} which is used to create a {@link VaultClient} instance to call the Vault REST API.
     */
    public ServiceRepositoryHandler(VaultClientFactory vaultClientFactory) {
        this.vaultClientFactory = vaultClientFactory;
        this.vaultAdminClient = vaultClientFactory.createAdminClient();
    }

    /***
     * Gets all {@link ServiceRepository} of a {@link ServiceVendor}.
     * @param serviceVendorId Id of the {@link ServiceVendor}.
     * @return All {@link ServiceRepository}s of the {@link ServiceVendor}
     *         with the specified id.
     * @throws ServiceRepositoryNotFound Thrown when a {@link ServiceRepository} is not found.
     */
    public List<ServiceRepository> getRepositoriesOfServiceVendor(UUID serviceVendorId) throws ServiceRepositoryNotFound {
        var repositoryKvSubkeys = vaultAdminClient.kv(VAULT_SECRET_ENGINE_NAME).listSecretKeysOfPath("vendor_" + serviceVendorId);
        var serviceRepositories = new ArrayList<ServiceRepository>();
        for (var repositoryIdString : repositoryKvSubkeys) {
            var repositoryId = UUID.fromString(repositoryIdString);
            var serviceRepository = this.getServiceRepository(serviceVendorId, repositoryId);
            serviceRepositories.add(serviceRepository);
        }

        return serviceRepositories;
    }

    /***
     * Gets a {@link ServiceRepository}.
     * @param serviceVendorId       Id of the {@link ServiceVendor} the
     *                              {@link ServiceRepository} belongs to.
     * @param serviceRepositoryId   Id of the {@link ServiceRepository}.
     * @return {@link ServiceRepository} with specified id of {@link ServiceVendor}
     *         with specified id.
     * @throws ServiceRepositoryNotFound Thrown when the {@link ServiceRepository} is not found.
     */
    public ServiceRepository getServiceRepository(UUID serviceVendorId, UUID serviceRepositoryId) throws ServiceRepositoryNotFound {
            var optionalKvSecret = this.vaultAdminClient.kv(VAULT_SECRET_ENGINE_NAME).getSecretsOfPath(this.getRepositorySecretPath(serviceVendorId, serviceRepositoryId));
            if (optionalKvSecret.isEmpty()) {
                throw new ServiceRepositoryNotFound("Service repository with id '" + serviceRepositoryId + "' of " +
                        "service vendor '" + serviceVendorId + "' not found");
            }

            var kvContent = optionalKvSecret.get().getData();

            var serviceRepository = new ServiceRepository(serviceRepositoryId);
            serviceRepository.setAddress(kvContent.get("address"));
            serviceRepository.setUsername(kvContent.get("username"));
            serviceRepository.setServiceRepositoryType(ServiceRepositoryType.valueOf(kvContent.get("type")));
            serviceRepository.setServiceVendorId(serviceVendorId);

            return serviceRepository;
    }

    /***
     * Create or update a {@link ServiceRepository}.
     * @param serviceRepository The {@link ServiceRepository} to update.
     * @return The updated {@link ServiceRepository}.
     */
    public ServiceRepository createOrUpdateServiceRepository(ServiceRepository serviceRepository) {
        var secrets = new HashMap<String, String>() {{
            put("serviceVendorId", serviceRepository.getServiceVendorId().toString());
            put("type", serviceRepository.getServiceRepositoryType().toString());
            put("address", serviceRepository.getAddress());
            put("username", serviceRepository.getUsername());
            put("password", serviceRepository.getPassword());
        }};

        var secretPath = this.getRepositorySecretPath(serviceRepository.getServiceVendorId(), serviceRepository.getId());
        vaultAdminClient.kv(VAULT_SECRET_ENGINE_NAME).addSecretsToKvEngine(secretPath, secrets);

        return serviceRepository;
    }

    /***
     * Deletes the specified {@link ServiceRepository}.
     * @param serviceRepository The {@link ServiceRepository} to delete.
     */
    public void deleteServiceRepository(ServiceRepository serviceRepository) {
        this.deleteServiceRepository(serviceRepository.getServiceVendorId(), serviceRepository.getId());
    }

    /***
     * Deletes the {@link ServiceRepository} with the specified id.
     * @param serviceVendorId Id of the {@link ServiceVendor} the
     *                        {@link ServiceRepository} belongs to.
     * @param repositoryId    The id of the {@link ServiceRepository}.
     */
    public void deleteServiceRepository(UUID serviceVendorId, UUID repositoryId) {
        var secretPath = this.getRepositorySecretPath(serviceVendorId, repositoryId);
        this.vaultAdminClient.kv(VAULT_SECRET_ENGINE_NAME).deleteSecretFromKvEngine(secretPath);
    }

    /***
     * Create the secret path for a {@link ServiceRepository}.
     * @param serviceVendorId  Id of the {@link ServiceVendor} the
     *                         {@link ServiceRepository} belongs to.
     * @param repositoryId     The id of the {@link ServiceRepository}.
     * @return The secret path for the {@link ServiceRepository}.
     */
    public static String getRepositorySecretPath(UUID serviceVendorId, UUID repositoryId) {
        return "vendor_" + serviceVendorId + "/" + repositoryId;
    }
}

