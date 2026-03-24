
package org.eclipse.slm.resource_management.features.device_integration.firmware_update.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class FirmwareUpdateAlreadyInProgressException extends RuntimeException {

    public FirmwareUpdateAlreadyInProgressException(UUID resourceId) {
        super("Firmware update for resource with id '"  + resourceId + "' is already in progress");
    }
}
