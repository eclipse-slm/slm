package org.eclipse.slm.service_management.service.initializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.restclient.feign.FeignResponseException;
import org.eclipse.slm.common.utils.files.FilesUtil;
import org.eclipse.slm.service_management.model.service_repositories.ServiceRepository;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorAccessDenied;
import org.eclipse.slm.service_management.service.client.ServiceManagementClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.util.*;

@Component
public class ServiceRepositoriesInitializer extends AbstractInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceRepositoriesInitializer.class);

    private Map<UUID, ServiceRepository> serviceRepositories = new HashMap<>();

    protected ServiceRepositoriesInitializer(ServiceManagementClientFactory serviceManagementClientFactory) {
        super(serviceManagementClientFactory);
    }


    public void init(String initDirectory) throws FileNotFoundException, JsonProcessingException {
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
                    this.serviceManagementClient.serviceRepositories().createOrUpdateRepository(
                            serviceRepository.getServiceVendorId(),
                            serviceRepository.getId(),
                            serviceRepository);
                    this.serviceRepositories.put(serviceRepository.getId(), serviceRepository);
                } catch (FeignResponseException e) {
                    var objectMapper = new ObjectMapper();
                    LOG.error("API call for service repository '" + objectMapper.writeValueAsString(serviceRepository) + "' failed: " +
                            "HTTP Code: " + e.getStatusCode() + " | Message: " + e.getMessage() + " | Body: " + e.getBody());
                } catch (ServiceVendorAccessDenied e) {
                    throw new RuntimeException(e);
                }
            }

            LOG.info("Service repository initialization finished");

        }
    }
}
