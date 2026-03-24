package org.eclipse.slm.resource_management.features.importer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ResourceManagementImportBadRequestException extends RuntimeException {

    public ResourceManagementImportBadRequestException(String message){
        super(message);
    }

}
