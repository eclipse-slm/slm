package org.eclipse.slm.resource_management.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ResourceNotCreatedException extends Exception {

    public ResourceNotCreatedException(String reason){
        super("Resource could not created because of " + reason);
    }

}
