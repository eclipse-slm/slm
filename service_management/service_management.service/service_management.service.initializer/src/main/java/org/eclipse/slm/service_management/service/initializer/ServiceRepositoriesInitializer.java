package org.eclipse.slm.service_management.service.initializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.eclipse.slm.common.utils.files.FilesUtil;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepository;
import org.eclipse.slm.service_management.service.client.handler.ApiException;
import org.eclipse.slm.service_management.service.client.handler.ServiceRepositoriesRestControllerApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.util.*;

@Component
public class ServiceRepositoriesInitializer extends AbstractInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceRepositoriesInitializer.class);

    private Map<UUID, ServiceRepository> serviceRepositories = new HashMap<>();

    private ServiceRepositoriesRestControllerApi serviceRepositoriesRestApi;

    @PostConstruct
    private void initApi() {
        this.serviceRepositoriesRestApi = new ServiceRepositoriesRestControllerApi(this.apiClient);
    }

    public void init(String initDirectory) throws FileNotFoundException, ApiException, JsonProcessingException {
        var files = FilesUtil.findFiles(initDirectory, "service-repositories", ".yaml");
        if (files.length == 0) {
            LOG.info("No init file '" + initDirectory + "service-repositories.yaml' found " +
                    "--> Skipping initialization of service repositories");
        } else {
            var serviceRepositoriesInitFile = files[0];
            List<ServiceRepository> serviceRepositories = FilesUtil.loadFromFile(serviceRepositoriesInitFile, new TypeReference<List<ServiceRepository>>() {
            });
            for (var serviceRepository : serviceRepositories) {
                try {
                    this.serviceRepositoriesRestApi.createOrUpdateRepository(
                            serviceRepository.getServiceVendorId(),
                            serviceRepository.getId(),
                            this.keycloakRealm, serviceRepository);
                    this.serviceRepositories.put(serviceRepository.getId(), serviceRepository);
                } catch (ApiException e) {
                    var objectMapper = new ObjectMapper();
                    LOG.error("API call for service repository '" + objectMapper.writeValueAsString(serviceRepository) + "' failed: " +
                            "HTTP Code: " + e.getCode() + " | Message: " + e.getMessage() + " | Body: " + e.getResponseBody());
                }
            }

            LOG.info("Service repository initialization finished");

        }
    }

    public Map<UUID, ServiceRepository> getServiceRepositories() {
        return serviceRepositories;
    }
}
