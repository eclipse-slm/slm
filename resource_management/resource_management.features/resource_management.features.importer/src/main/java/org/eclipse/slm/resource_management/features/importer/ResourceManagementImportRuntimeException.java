package org.eclipse.slm.resource_management.features.importer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ResourceManagementImportRuntimeException extends RuntimeException {

    public ResourceManagementImportRuntimeException(String message){
        super(message);
    }

}
