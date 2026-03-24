package org.eclipse.slm.resource_management.service.initializer;

import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.slm.resource_management.features.profiler.ProfilerDTOApi;
import org.eclipse.slm.resource_management.service.client.ResourceManagementClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.util.List;

@Component
public class ProfilerInitializer extends AbstractInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(ProfilerInitializer.class);
    private static final String FILENAME = "profiler";
    private static final String FILE_EXTENSION= ".json";

    public ProfilerInitializer(FileUtil fileUtil, ResourceManagementClientFactory resourceManagementClientFactory) {
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
                    "--> Skipping initialization of profiler");
        } else {
            var profilerInitFile = files[0];

            List<ProfilerDTOApi> profilers = FileUtil.loadFromFile(
                    profilerInitFile,
                    new TypeReference<List<ProfilerDTOApi>>() {
                    });

            profilers.forEach(profiler -> {
                try {
                    LOG.info("Creating Profiler '" + profiler.getName() + "'");
                    var response = this.resourceManagementClient.profiler().createProfiler(profiler);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        LOG.info("Profiler '" + profiler.getName() + "' successfully created");
                    } else {
                        LOG.error("Failed to create profiler '" + profiler.getName() + "'. Received status: " + response.getStatusCode());
                    }

                }
                catch (Exception e) {
                    LOG.error("Error while creating profiler '" + profiler.getName() + "': " + e.getMessage());
                }
            });
        }
    }
}
