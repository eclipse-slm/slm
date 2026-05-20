package org.eclipse.slm.service_management.features.service_deployment.api.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ServiceInstanceUpdateException extends Exception {

    public ServiceInstanceUpdateException(String message) {
        super(message);
    }
}

