package org.eclipse.slm.resource_management.common.remote_access;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class RemoteAccessNotFoundException extends RuntimeException{

    public RemoteAccessNotFoundException(String message){
        super(message);
    }

}
