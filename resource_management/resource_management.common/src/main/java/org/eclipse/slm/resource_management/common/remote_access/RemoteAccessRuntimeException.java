package org.eclipse.slm.resource_management.common.remote_access;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class RemoteAccessRuntimeException extends RuntimeException{

    public RemoteAccessRuntimeException(String message){
        super(message);
    }

}
