package org.eclipse.slm.resource_management.model.resource.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends Exception {

    public ResourceNotFoundException(UUID resourceId)
    {
        super("Resource with id '"  + resourceId + "' not found");
    }

}
