package org.eclipse.slm.common.aas.model.submodelrepository.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SubmodelNotFoundException extends RuntimeException {

    public SubmodelNotFoundException(String message) {
        super(message);
    }

    public static SubmodelNotFoundException forSubmodelId(String submodelId) {
        return new SubmodelNotFoundException("Submodel with id '" + submodelId + "' not found");
    }
}
