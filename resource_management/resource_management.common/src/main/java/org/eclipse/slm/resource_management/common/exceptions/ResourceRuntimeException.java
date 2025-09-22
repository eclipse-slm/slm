package org.eclipse.slm.resource_management.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ResourceRuntimeException extends RuntimeException {

    public ResourceRuntimeException(String message){
        super(message);
    }

}
