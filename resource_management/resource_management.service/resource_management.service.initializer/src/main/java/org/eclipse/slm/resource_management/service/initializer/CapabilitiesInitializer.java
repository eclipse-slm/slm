package org.eclipse.slm.resource_management.service.initializer;

import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.slm.resource_management.features.capabilities.dto.CapabilityDTOApi;
import org.eclipse.slm.resource_management.service.client.ResourceManagementClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class CapabilitiesInitializer extends AbstractInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(CapabilitiesInitializer.class);
    private static final String FILENAME = "capabilities";
    private static final String FILE_EXTENSION= ".json";

    public CapabilitiesInitializer(
            FileUtil fileUtil,
            ResourceManagementClientFactory resourceManagementClientFactory
    ) {
        super(fileUtil, resourceManagementClientFactory);
    }


    @PostConstruct
    public void init() throws FileNotFoundException {
        var files = this.fileUtil.findFiles(
                this.getInitDirectory(),
                FILENAME,
                FILE_EXTENSION
        );

        if (files.length == 0) {
            LOG.info("No init file '" + this.getInitDirectory() + FILENAME + FILE_EXTENSION + "' found " +
                    "--> Skipping initialization of capabilities");
        } else {
            var capabilitiesInitFile = files[0];

            List<CapabilityDTOApi> capabilities = FileUtil.loadFromFile(
                    capabilitiesInitFile,
                    new TypeReference<List<CapabilityDTOApi>>(){});

            if (capabilities == null) {
                LOG.error("Failed to load capabilities from file '" + capabilitiesInitFile.getAbsolutePath() + "'. Skipping initialization of capabilities.");
                return;
            }

            capabilities.forEach(capability -> {
                try {
                    LOG.info("Start creating capability '{}'.", capability.getName());
                    var response = this.resourceManagementClient.capabilities().createCapability(capability);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        LOG.info("Capability '" + capability.getName() + "' created successfully");
                    } else {
                        LOG.error("Failed to create capability '" + capability.getName() + "'. Received status: " + response.getStatusCode());
                    }
                } catch (Exception e) {
                    LOG.error("Error while creating capability '" + capability.getName() + "':", e);
                }
            });
        }
    }
}
