package org.eclipse.slm.service_management.model.vendors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ServiceVendorDeveloperNotFoundException extends Exception {

    public ServiceVendorDeveloperNotFoundException(UUID developerId, UUID serviceVendorId)
    {
        super("Service developer with id '" + developerId + "' of service vendor '" + serviceVendorId + "' not found");
    }

}
