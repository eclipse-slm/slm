package org.eclipse.slm.common.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EventNotAcceptedException extends RuntimeException {

    public EventNotAcceptedException(String currentState, String targetState) {
        super(String.format("Event not accepted: Current state '%s' does not allow transition to target state '%s'.", currentState, targetState));
    }

}
