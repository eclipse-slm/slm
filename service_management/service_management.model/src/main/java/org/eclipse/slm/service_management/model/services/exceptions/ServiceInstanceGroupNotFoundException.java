package org.eclipse.slm.service_management.model.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ServiceInstanceGroupNotFoundException extends Exception {

    public ServiceInstanceGroupNotFoundException(UUID serviceInstanceGroupId)
    {
        super("Service instance group with id '" + serviceInstanceGroupId + "' not found");
    }

}
