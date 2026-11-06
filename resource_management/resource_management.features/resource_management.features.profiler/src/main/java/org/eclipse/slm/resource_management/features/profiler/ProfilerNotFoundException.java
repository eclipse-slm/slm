package org.eclipse.slm.resource_management.features.profiler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ProfilerNotFoundException extends RuntimeException {

    public ProfilerNotFoundException(UUID profilerId) {
        super("Profiler [id='"  + profilerId + "'] not found");
    }

}
