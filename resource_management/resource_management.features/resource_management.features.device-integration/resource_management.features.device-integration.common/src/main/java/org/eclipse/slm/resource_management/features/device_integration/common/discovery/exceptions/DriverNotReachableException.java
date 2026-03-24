package org.eclipse.slm.resource_management.features.device_integration.common.discovery.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class DriverNotReachableException extends Exception {

    public DriverNotReachableException(String driverAddress, int driverPort)
    {
        super("Driver '"  + driverAddress + ":" +  driverPort + "' not reachable");
    }

}
