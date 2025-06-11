package org.eclipse.slm.resource_management.service.initializer;

import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.slm.resource_management.model.capabilities.CapabilityDTOApi;
import org.eclipse.slm.resource_management.service.client.ResourceManagementApiClientInitializer;
import org.eclipse.slm.resource_management.service.client.handler.ApiException;
import org.eclipse.slm.resource_management.service.client.handler.CapabilitiesRestControllerApi;
import org.eclipse.slm.resource_management.service.client.handler.ResourcesRestControllerApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CapabilitiesInitializer extends AbstractInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(CapabilitiesInitializer.class);
    private static final String FILENAME = "capabilities";
    private static final String FILE_EXTENSION= ".json";
    private CapabilitiesRestControllerApi capabilitiesRestControllerApi;

    public CapabilitiesInitializer(
            FileUtil fileUtil,
            ResourceManagementApiClientInitializer resourceManagementApiClientInitializer
    ) {
        super(fileUtil, resourceManagementApiClientInitializer);
    }


    @PostConstruct
    public void init() throws FileNotFoundException {
        this.capabilitiesRestControllerApi = new CapabilitiesRestControllerApi(this.apiClient);

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

            capabilities.forEach(capability -> {
                CapabilitiesInitializerThread thread = new CapabilitiesInitializerThread(capability, keycloakRealm, capabilitiesRestControllerApi);
                thread.start();
                LOG.info("Wait till creation of '"+capability.getName()+"' is finished.");

                while(thread.isAlive()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                LOG.info("Creation of '"+capability.getName()+"' is finished.");
            });
        }
    }
}
