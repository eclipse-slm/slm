package org.eclipse.slm.resource_management.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceTypeNotFoundException extends Exception {

    public ResourceTypeNotFoundException(String name){
        super("Resource type with name '" + name + "' not found");
    }

}
