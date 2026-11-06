package org.eclipse.slm.resource_management.features.profiler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ProfilerBadRequestException extends RuntimeException {

    public ProfilerBadRequestException(String message) {
        super("Bad request for profiler: " + message);
    }

}
