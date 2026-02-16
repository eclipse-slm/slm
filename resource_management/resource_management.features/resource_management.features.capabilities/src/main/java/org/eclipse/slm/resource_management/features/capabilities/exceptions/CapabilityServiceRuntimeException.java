
package org.eclipse.slm.resource_management.features.capabilities.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class CapabilityServiceRuntimeException extends RuntimeException {

    public CapabilityServiceRuntimeException(String message) {
        super(message);
    }

    public CapabilityServiceRuntimeException(String message, Throwable e) {
        super(message, e);
    }


}
