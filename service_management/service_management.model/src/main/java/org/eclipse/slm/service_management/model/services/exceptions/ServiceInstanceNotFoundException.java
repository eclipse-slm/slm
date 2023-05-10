package org.eclipse.slm.service_management.model.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ServiceInstanceNotFoundException extends Exception {

    public ServiceInstanceNotFoundException(UUID serviceInstanceId)
    {
        super("Service instance with id '" + serviceInstanceId + "' not found");
    }

}
