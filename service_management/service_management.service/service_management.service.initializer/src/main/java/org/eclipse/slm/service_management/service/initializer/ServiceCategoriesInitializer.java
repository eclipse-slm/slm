package org.eclipse.slm.service_management.service.initializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.restclient.feign.FeignResponseException;
import org.eclipse.slm.common.utils.files.FilesUtil;
import org.eclipse.slm.service_management.features.service_offerings.api.servicecategories.ServiceOfferingCategory;
import org.eclipse.slm.service_management.service.client.ServiceManagementClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.util.*;

@Component
public class ServiceCategoriesInitializer extends AbstractInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceCategoriesInitializer.class);

    private Map<String, ServiceOfferingCategory> serviceCategories = new HashMap<>();

    protected ServiceCategoriesInitializer(ServiceManagementClientFactory serviceManagementClientFactory) {
        super(serviceManagementClientFactory);
    }

    public void init(String initDirectory) throws FileNotFoundException, JsonProcessingException {
        var files = FilesUtil.findFiles(initDirectory, "service-categories", ".yaml");
        if (files.length == 0) {
            LOG.info("No init file '" + initDirectory + "service-categories.yaml' found " +
                    "--> Skipping initialization of service categories");
        } else {
            var serviceCategoriesInitFile = files[0];
            var alreadyExistingServiceCategories = this.serviceManagementClient.serviceCategories().getServiceCategories();

            List<ServiceOfferingCategory> serviceCategories = FilesUtil.loadFromFile(serviceCategoriesInitFile, new TypeReference<List<ServiceOfferingCategory>>() {
            });
            for (var serviceCategory : serviceCategories) {
                try {
                    if(
                            alreadyExistingServiceCategories.stream().anyMatch(
                                    c -> c.getName().equals(serviceCategory.getName())
                            )
                    ) {
                        LOG.info("Category '"+serviceCategory.getName()+"' exists already -> Skip create.");
                        continue;
                    }

                    this.serviceManagementClient.serviceCategories().createOrUpdateServiceCategory(serviceCategory);
                    this.serviceCategories.put(serviceCategory.getName(), serviceCategory);
                } catch (FeignResponseException e) {
                    var objectMapper = new ObjectMapper();
                    LOG.error("Init of service category '" + objectMapper.writeValueAsString(serviceCategory) + "' failed: " +
                            "HTTP Code: " + e.getStatusCode() + " | Message: " + e.getMessage() + " | Body: " + e.getBody());
                }
            }

            LOG.info("Service categories initialization finished");
        }
    }
}

