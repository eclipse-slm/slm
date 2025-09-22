package org.eclipse.slm.resource_management.service.initializer;

import org.eclipse.slm.resource_management.features.profiler.ProfilerDTOApi;
import org.eclipse.slm.resource_management.service.client.handler.ApiException;
import org.eclipse.slm.resource_management.service.client.handler.ProfilerRestControllerApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfilerInitializerThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(ProfilerInitializerThread.class);
    private final ProfilerDTOApi profilerDTOApi;
    private final String keycloakRealm;
    private final ProfilerRestControllerApi profilerRestControllerApi;

    public ProfilerInitializerThread(
            ProfilerDTOApi profilerDTOApi,
            String keycloakRealm,
            ProfilerRestControllerApi profilerRestControllerApi
    ) {
        this.profilerDTOApi = profilerDTOApi;
        this.keycloakRealm = keycloakRealm;
        this.profilerRestControllerApi = profilerRestControllerApi;
    }

    public void run() {
        try {
            LOG.info("Creating Profiler '" + profilerDTOApi.getName() + "'");
            this.profilerRestControllerApi.createProfiler(
                    keycloakRealm,
                    profilerDTOApi
            );
            LOG.info("Profiler '" + profilerDTOApi.getName() + "' successfully created");
        } catch (ApiException e) {
            LOG.error("Error while creating profiler '" + profilerDTOApi.getName() + "': " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
