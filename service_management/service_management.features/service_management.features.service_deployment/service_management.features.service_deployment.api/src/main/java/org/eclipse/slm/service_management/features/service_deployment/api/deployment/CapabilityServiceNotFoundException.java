package org.eclipse.slm.service_management.features.service_deployment.api.deployment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CapabilityServiceNotFoundException extends Exception {


    public CapabilityServiceNotFoundException(UUID capabilityServiceId) {
        super("Capability service with id '" + capabilityServiceId + "' not found");
    }
}

