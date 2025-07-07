package org.eclipse.slm.common.aas.clients.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ShellNotFoundException extends RuntimeException {

    public ShellNotFoundException() {
    }

    public ShellNotFoundException(String shellId) {
        super(getMessage(shellId));
    }

    private static String getMessage(String shellId) {
        return "Shell with Id " + shellId + " does not exist";
    }
}
