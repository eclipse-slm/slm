package org.eclipse.slm.resource_management.service.initializer;

import org.eclipse.slm.resource_management.features.capabilities.dto.CapabilityDTOApi;
import org.eclipse.slm.resource_management.service.client.handler.ApiException;
import org.eclipse.slm.resource_management.service.client.handler.CapabilitiesRestControllerApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CapabilitiesInitializerThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(CapabilitiesInitializerThread.class);
    private final CapabilityDTOApi capabilityDTOApi;
    private final String keycloakRealm;
    private final CapabilitiesRestControllerApi capabilitiesRestControllerApi;

    public CapabilitiesInitializerThread(
            CapabilityDTOApi capabilityDTOApi,
            String keycloakRealm,
            CapabilitiesRestControllerApi capabilitiesRestControllerApi
    ) {
        this.capabilityDTOApi = capabilityDTOApi;
        this.keycloakRealm = keycloakRealm;
        this.capabilitiesRestControllerApi = capabilitiesRestControllerApi;
    }

    public void run() {
        try {
            LOG.info("Creating capability '" + capabilityDTOApi.getName() + "'");
            this.capabilitiesRestControllerApi.createCapability(
                    keycloakRealm,
                    capabilityDTOApi
            );
            LOG.info("Capability '" + capabilityDTOApi.getName() + "' successfully created");
        } catch (ApiException e) {
            LOG.error("Error while creating capability '" + capabilityDTOApi.getName() + "': " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
