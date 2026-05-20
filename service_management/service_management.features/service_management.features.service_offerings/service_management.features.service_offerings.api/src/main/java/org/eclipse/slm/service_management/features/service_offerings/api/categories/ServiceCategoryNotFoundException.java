package org.eclipse.slm.service_management.features.service_offerings.api.categories;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ServiceCategoryNotFoundException extends Exception {

    public ServiceCategoryNotFoundException(String message) {
    }

}

