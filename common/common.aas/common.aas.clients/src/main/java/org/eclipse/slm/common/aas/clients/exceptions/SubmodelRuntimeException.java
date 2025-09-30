package org.eclipse.slm.common.aas.clients.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SubmodelRuntimeException extends RuntimeException {


    public SubmodelRuntimeException() {
    }

    public SubmodelRuntimeException(String message) {
        super(message);
    }
}
