package org.eclipse.slm.resource_management.features.profiler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ProfilerRuntimeException extends RuntimeException {

    public ProfilerRuntimeException(UUID profilerId, String message) {
        super("Runtime error for profiler with id '"  + profilerId + "': " + message);
    }

}
