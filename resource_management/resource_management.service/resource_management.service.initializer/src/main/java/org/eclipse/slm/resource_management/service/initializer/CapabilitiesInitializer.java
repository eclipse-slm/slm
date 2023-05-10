package org.eclipse.slm.resource_management.service.initializer;

import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.slm.resource_management.model.capabilities.CapabilityDTOApi;
import org.eclipse.slm.resource_management.service.client.ResourceManagementApiClientInitializer;
import org.eclipse.slm.resource_management.service.client.handler.ApiException;
import org.eclipse.slm.resource_management.service.client.handler.CapabilitiesRestControllerApi;
import org.eclipse.slm.resource_management.service.client.handler.ResourcesRestControllerApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.util.List;

@Component
public class CapabilitiesInitializer extends AbstractInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(CapabilitiesInitializer.class);

    private CapabilitiesRestControllerApi capabilitiesRestControllerApi;

    public CapabilitiesInitializer(FileUtil fileUtil,
                                   ResourceManagementApiClientInitializer resourceManagementApiClientInitializer) {
        super(fileUtil, resourceManagementApiClientInitializer);
    }

    @PostConstruct
    public void init() throws FileNotFoundException {
        this.capabilitiesRestControllerApi = new CapabilitiesRestControllerApi(this.apiClient);

        String filename = "capabilities";
        String fileExtension = ".json";

        var files = this.fileUtil.findFiles(
                this.getInitDirectory(),
                filename,
                fileExtension
        );

        if (files.length == 0) {
            LOG.info("No init file '" + this.getInitDirectory() + filename + "." + fileExtension + "' found " +
                    "--> Skipping initialization of capabilities");
        } else {
            var capabilitiesInitFile = files[0];

            List<CapabilityDTOApi> capabilities = FileUtil.loadFromFile(
                    capabilitiesInitFile,
                    new TypeReference<List<CapabilityDTOApi>>(){});

            capabilities.forEach(capability -> {
                try {
                    LOG.info("Creating capability '" + capability.getName() + "'");
                    this.capabilitiesRestControllerApi.createCapability(
                            keycloakRealm,
                            capability
                    );
                    LOG.info("Capability '" + capability.getName() + "' successfully created");
                } catch (ApiException e) {
                    LOG.error("Error while creating capability '" + capability.getName() + "': " + e.getMessage());
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
