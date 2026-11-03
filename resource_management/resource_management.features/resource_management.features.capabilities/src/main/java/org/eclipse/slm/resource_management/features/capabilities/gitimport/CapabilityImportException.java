package org.eclipse.slm.resource_management.features.capabilities.gitimport;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CapabilityImportException extends ResponseStatusException {

    public CapabilityImportException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public CapabilityImportException(HttpStatus status, String reason, Throwable cause) {
        super(status, reason, cause);
    }
}
