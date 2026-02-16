
package org.eclipse.slm.resource_management.features.capabilities.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CapabilityServiceNotFoundException extends RuntimeException {

    public CapabilityServiceNotFoundException(String message) {
        super(message);
    }

    public CapabilityServiceNotFoundException(UUID capabilityServiceId) {
        super("Capability service [id='"  + capabilityServiceId + "'] not found");
    }

}
