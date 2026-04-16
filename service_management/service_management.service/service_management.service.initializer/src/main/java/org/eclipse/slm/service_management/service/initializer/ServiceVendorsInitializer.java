package org.eclipse.slm.service_management.service.initializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.aas.clients.base.FeignResponseException;
import org.eclipse.slm.common.utils.files.FilesUtil;
import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.eclipse.slm.service_management.model.vendors.ServiceVendorDTOApi;
import org.eclipse.slm.service_management.model.vendors.ServiceVendorDTOFileImport;
import org.eclipse.slm.service_management.service.client.ServiceManagementClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Component
public class ServiceVendorsInitializer extends AbstractInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceVendorsInitializer.class);

    private Map<String, ServiceVendor> serviceVendors = new HashMap<>();

    protected ServiceVendorsInitializer(ServiceManagementClientFactory serviceManagementClientFactory) {
        super(serviceManagementClientFactory);
    }

    public void init(String initDirectory) throws FileNotFoundException, JsonProcessingException {
        var serviceVendorsInitDirectory = initDirectory + "service-vendors/";
        var files = FilesUtil.findFiles(
                serviceVendorsInitDirectory, "service-vendors", ".yaml");
        if (files.length == 0) {
            LOG.info("No init file '" + serviceVendorsInitDirectory + "service-vendors.yaml' found " +
                    "--> Skipping initialization of service vendors");
        } else {
            var serviceVendorInitFile = files[0];
            List<ServiceVendorDTOFileImport> serviceVendorInits = FilesUtil.loadFromFile(serviceVendorInitFile, new TypeReference<List<ServiceVendorDTOFileImport>>() {
            });
            for (var serviceVendorInit : serviceVendorInits) {
                var serviceVendor = new ServiceVendor(serviceVendorInit.getId());
                serviceVendor.setName(serviceVendorInit.getName());
                serviceVendor.setDescription(serviceVendorInit.getDescription());
                try {
                    var logo = FilesUtil.loadFileBytes(serviceVendorsInitDirectory + serviceVendorInit.getLogoFilename());
                    serviceVendor.setLogo(logo);
                } catch (IOException e) {
                    LOG.error("Failed to load logo '" + serviceVendorInit.getLogoFilename() + "' of service vendor '" + serviceVendorInit.getName() + "'");
                }

                var serviceVendorDTOApi = ObjectMapperUtils.map(serviceVendor, ServiceVendorDTOApi.class);

                try {
                    this.serviceManagementClient.serviceVendors().createOrUpdateServiceVendorWithId(serviceVendorDTOApi.getId(), serviceVendorDTOApi);
                    this.serviceVendors.put(serviceVendor.getName(), serviceVendor);
                } catch (FeignResponseException e) {
                    var objectMapper = new ObjectMapper();
                    LOG.error("API call for service vendor '" + objectMapper.writeValueAsString(serviceVendorDTOApi) + "' failed: " +
                            "HTTP Code: " + e.getStatusCode() + " | Message: " + e.getMessage() + " | Body: " + e.getBody());
                }
            }

            LOG.info("Service vendors initialization finished");
        }
    }
}
