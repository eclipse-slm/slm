package org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ServiceInstanceRuntimeException extends Exception {

    public ServiceInstanceRuntimeException(String message)
    {
        super(message);
    }

    public ServiceInstanceRuntimeException(String message, Throwable cause)
    {
        super(message);
        this.initCause(cause);
    }

}

