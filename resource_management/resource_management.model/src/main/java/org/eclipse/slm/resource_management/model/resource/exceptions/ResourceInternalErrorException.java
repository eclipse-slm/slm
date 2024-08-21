package org.eclipse.slm.resource_management.model.resource.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ResourceInternalErrorException extends Exception {

    public ResourceInternalErrorException(String reason){
        super("Resource could not created because of " + reason);
    }

}
