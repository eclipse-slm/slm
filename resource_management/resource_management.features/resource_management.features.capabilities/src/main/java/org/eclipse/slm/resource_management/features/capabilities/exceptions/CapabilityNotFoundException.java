
package org.eclipse.slm.resource_management.features.capabilities.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CapabilityNotFoundException extends RuntimeException {

    public CapabilityNotFoundException(UUID capabilityJobId) {
        super("Capability[id='"  + capabilityJobId + "'] not found");
    }

}
