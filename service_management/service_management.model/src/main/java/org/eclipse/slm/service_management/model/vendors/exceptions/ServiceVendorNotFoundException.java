package org.eclipse.slm.service_management.model.vendors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ServiceVendorNotFoundException extends Exception {

    public ServiceVendorNotFoundException(UUID serviceVendorId)
    {
        super("Service vendor with id '" + serviceVendorId + "' not found");
    }

}
