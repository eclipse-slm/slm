package org.eclipse.slm.service_management.service.initializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.utils.files.FilesUtil;
import org.eclipse.slm.service_management.model.offerings.ServiceCategory;
import org.eclipse.slm.service_management.service.client.handler.ApiException;
import org.eclipse.slm.service_management.service.client.handler.ServiceCategoriesRestControllerApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;;
import java.io.FileNotFoundException;
import java.util.*;

@Component
public class ServiceCategoriesInitializer extends AbstractInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceCategoriesInitializer.class);

    private Map<String, ServiceCategory> serviceCategories = new HashMap<>();

    private ServiceCategoriesRestControllerApi serviceOfferingsCategoriesRestApi;

    @PostConstruct
    private void initApi() {
        this.serviceOfferingsCategoriesRestApi = new ServiceCategoriesRestControllerApi(this.apiClient);
    }

    public void init(String initDirectory) throws FileNotFoundException, ApiException, JsonProcessingException {
        var files = FilesUtil.findFiles(initDirectory, "service-categories", ".yaml");
        if (files.length == 0) {
            LOG.info("No init file '" + initDirectory + "service-categories.yaml' found " +
                    "--> Skipping initialization of service categories");
        } else {
            var serviceCategoriesInitFile = files[0];
            var alreadyExistingServiceCategories = serviceOfferingsCategoriesRestApi.getServiceCategories(this.keycloakRealm);

            List<ServiceCategory> serviceCategories = FilesUtil.loadFromFile(serviceCategoriesInitFile, new TypeReference<List<ServiceCategory>>() {
            });
            for (var serviceCategory : serviceCategories) {
                try {
                    if(
                            alreadyExistingServiceCategories.stream().filter(
                                    c -> c.getName().equals(serviceCategory.getName())
                            ).findFirst().isPresent()
                    ) {
                        LOG.info("Category '"+serviceCategory.getName()+"' exists already -> Skip create.");
                        continue;
                    }

                    this.serviceOfferingsCategoriesRestApi.createOrUpdateServiceCategory(this.keycloakRealm, serviceCategory);
                    this.serviceCategories.put(serviceCategory.getName(), serviceCategory);
                } catch (ApiException e) {
                    var objectMapper = new ObjectMapper();
                    LOG.error("Init of service category '" + objectMapper.writeValueAsString(serviceCategory) + "' failed: " +
                            "HTTP Code: " + e.getCode() + " | Message: " + e.getMessage() + " | Body: " + e.getResponseBody());
                }
            }

            LOG.info("Service categories initialization finished");
        }
    }

    public Map<String, ServiceCategory> getServiceCategories() {
        return serviceCategories;
    }
}
