package org.eclipse.slm.resource_management.service.discovery.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class DiscoveryJobNotFoundException extends Exception {

    public DiscoveryJobNotFoundException(UUID driverId)
    {
        super("Discovery job with id '"  + driverId + "' not found");
    }

}
