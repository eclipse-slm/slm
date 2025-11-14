package org.eclipse.slm.common.aas.model.shellregistry.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ShellDescriptorNotFoundException extends RuntimeException {

    public ShellDescriptorNotFoundException(String aasId) {
        super("Shell Descriptor [id='" + aasId + "'] not found");
    }
}
