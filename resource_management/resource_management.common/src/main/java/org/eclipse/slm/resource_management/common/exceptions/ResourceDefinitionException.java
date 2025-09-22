package org.eclipse.slm.resource_management.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ResourceDefinitionException extends Exception {

    public ResourceDefinitionException(String message)
    {
        super(message);
    }

}
