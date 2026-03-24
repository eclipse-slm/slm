
package org.eclipse.slm.resource_management.features.capabilities.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class CapabilityRuntimeException extends RuntimeException {

    public CapabilityRuntimeException(String message) {
        super("Runtime error related to capability : " + message);
    }

    public CapabilityRuntimeException(UUID capabilityId, String message) {
        super("Runtime error for capability with id '"  + capabilityId + "': " + message);
    }

}
