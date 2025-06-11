package org.eclipse.slm.resource_management.service.initializer;

import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.slm.resource_management.model.profiler.ProfilerDTOApi;
import org.eclipse.slm.resource_management.service.client.ResourceManagementApiClientInitializer;
import org.eclipse.slm.resource_management.service.client.handler.ProfilerRestControllerApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;;
import java.io.FileNotFoundException;
import java.util.List;

@Component
public class ProfilerInitializer extends AbstractInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(ProfilerInitializer.class);
    private static final String FILENAME = "profiler";
    private static final String FILE_EXTENSION= ".json";
    private ProfilerRestControllerApi profilerRestControllerApi;
    public ProfilerInitializer(
            FileUtil fileUtil,
            ResourceManagementApiClientInitializer resourceManagementApiClientInitializer
    ) {
        super(fileUtil, resourceManagementApiClientInitializer);
    }


    @PostConstruct
    public void init() throws FileNotFoundException {
        this.profilerRestControllerApi = new ProfilerRestControllerApi(this.apiClient);

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

            List<ProfilerDTOApi> profiler = FileUtil.loadFromFile(
                    profilerInitFile,
                    new TypeReference<List<ProfilerDTOApi>>() {
                    });

            profiler.forEach(p -> {
                ProfilerInitializerThread thread = new ProfilerInitializerThread(
                        p,
                        keycloakRealm,
                        profilerRestControllerApi
                );
                thread.start();
            });
        }
    }
}
