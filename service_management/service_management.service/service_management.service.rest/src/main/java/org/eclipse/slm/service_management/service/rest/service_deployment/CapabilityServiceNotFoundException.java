package org.eclipse.slm.service_management.service.rest.service_deployment;

import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CapabilityServiceNotFoundException extends Exception {

    public CapabilityServiceNotFoundException(CapabilityService capabilityService) {
        this(capabilityService.getId());
    }

    public CapabilityServiceNotFoundException(UUID capabilityServiceId) {
        super("Capability service with id '" + capabilityServiceId + "' not found");
    }
}
