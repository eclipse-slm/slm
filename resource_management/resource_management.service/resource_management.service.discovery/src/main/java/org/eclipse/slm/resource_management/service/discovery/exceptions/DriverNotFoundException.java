package org.eclipse.slm.resource_management.service.discovery.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class DriverNotFoundException extends Exception {

    public DriverNotFoundException(String driverId)
    {
        super("Driver with id '"  + driverId + "' not found");
    }

}
