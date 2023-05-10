package org.eclipse.slm.service_management.service.rest.service_repositories;

import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.common.vault.model.KvPath;
import org.eclipse.slm.common.vault.model.exceptions.KvValueNotFound;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepository;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepositoryType;
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

    public static final String VAULT_SECRET_ENGINE_PATH = "service-repositories";

    private final VaultClient vaultClient;

    /***
     * Instantiates a new {@link ServiceRepositoryHandler}.
     * @param vaultClient {@link VaultClient} which is used by this instance to call the Vault REST API.
     */
    public ServiceRepositoryHandler(VaultClient vaultClient) {
        this.vaultClient = vaultClient;
    }

    /***
     * Gets all {@link ServiceRepository} of a {@link org.eclipse.slm.service_management.model.vendors.ServiceVendor}.
     * @param serviceVendorId Id of the {@link org.eclipse.slm.service_management.model.vendors.ServiceVendor}.
     * @return All {@link ServiceRepository}s of the {@link org.eclipse.slm.service_management.model.vendors.ServiceVendor}
     *         with the specified id.
     * @throws ServiceRepositoryNotFound Thrown when a {@link ServiceRepository} is not found.
     */
    public List<ServiceRepository> getRepositoriesOfServiceVendor(UUID serviceVendorId) throws ServiceRepositoryNotFound {
        var repositoryIdStrings = vaultClient.listSecrets(new VaultCredential(), VAULT_SECRET_ENGINE_PATH, "vendor_" + serviceVendorId);
        var serviceRepositories = new ArrayList<ServiceRepository>();
        for (var repositoryIdString : repositoryIdStrings) {
            var repositoryId = UUID.fromString(repositoryIdString);
            var serviceRepository = this.getServiceRepository(serviceVendorId, repositoryId);
            serviceRepositories.add(serviceRepository);
        }

        return serviceRepositories;
    }

    /***
     * Gets a {@link ServiceRepository}.
     * @param serviceVendorId       Id of the {@link org.eclipse.slm.service_management.model.vendors.ServiceVendor} the
     *                              {@link ServiceRepository} belongs to.
     * @param serviceRepositoryId   Id of the {@link ServiceRepository}.
     * @return {@link ServiceRepository} with specified id of {@link org.eclipse.slm.service_management.model.vendors.ServiceVendor}
     *         with specified id.
     * @throws ServiceRepositoryNotFound Thrown when the {@link ServiceRepository} is not found.
     */
    public ServiceRepository getServiceRepository(UUID serviceVendorId, UUID serviceRepositoryId) throws ServiceRepositoryNotFound {
        try {
            var kvPath = new KvPath(VAULT_SECRET_ENGINE_PATH, this.getRepositorySecretPath(serviceVendorId, serviceRepositoryId));
            var kvContent = vaultClient.getKvContentUsingApplicationProperties(new VaultCredential(), kvPath);

            var serviceRepository = new ServiceRepository(serviceRepositoryId);
            serviceRepository.setAddress(kvContent.get("address"));
            serviceRepository.setUsername(kvContent.get("username"));
            serviceRepository.setServiceRepositoryType(ServiceRepositoryType.valueOf(kvContent.get("type")));
            serviceRepository.setServiceVendorId(serviceVendorId);

            return serviceRepository;
        } catch (KvValueNotFound e) {
            throw new ServiceRepositoryNotFound("Service repository with id '" + serviceRepositoryId + "' of " +
                    "service vendor '" + serviceVendorId + "' not found");
        }
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
        vaultClient.addSecretToKvEngine(new VaultCredential(), VAULT_SECRET_ENGINE_PATH, secretPath, secrets);

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
     * @param serviceVendorId Id of the {@link org.eclipse.slm.service_management.model.vendors.ServiceVendor} the
     *                        {@link ServiceRepository} belongs to.
     * @param repositoryId    The id of the {@link ServiceRepository}.
     */
    public void deleteServiceRepository(UUID serviceVendorId, UUID repositoryId) {
        var secretPath = this.getRepositorySecretPath(serviceVendorId, repositoryId);
        vaultClient.removeSecretFromKvEngine(new VaultCredential(), VAULT_SECRET_ENGINE_PATH, secretPath);
    }

    /***
     * Create the secret path for a {@link ServiceRepository}.
     * @param serviceVendorId  Id of the {@link org.eclipse.slm.service_management.model.vendors.ServiceVendor} the
     *                         {@link ServiceRepository} belongs to.
     * @param repositoryId     The id of the {@link ServiceRepository}.
     * @return The secret path for the {@link ServiceRepository}.
     */
    public static String getRepositorySecretPath(UUID serviceVendorId, UUID repositoryId) {
        return "vendor_" + serviceVendorId + "/" + repositoryId;
    }
}
