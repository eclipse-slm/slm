
package org.eclipse.slm.resource_management.features.capabilities.jobs;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class CapabilityJobRuntimeException extends RuntimeException {

    public CapabilityJobRuntimeException(String message) {
        super(message);
    }

    public CapabilityJobRuntimeException(UUID capabilityJobId, String message) {
        super("Error during capability with id '"  + capabilityJobId + "': " + message);
    }

}
