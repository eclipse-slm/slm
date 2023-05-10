package org.eclipse.slm.service_management.model.vendors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ServiceVendorAccessDenied extends Exception {

    public ServiceVendorAccessDenied(String message)
    {
        super(message);
    }

}
