package org.eclipse.slm.resource_management.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class LocationNotFoundException extends Exception {

    public LocationNotFoundException(UUID locationId)
    {
        super("Location with id '"  + locationId + "' not found");
    }

}
