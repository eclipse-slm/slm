
package org.eclipse.slm.resource_management.features.device_integration.firmware_update.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class FirmwareUpdateJobNotFoundException extends RuntimeException {

    public FirmwareUpdateJobNotFoundException(UUID firmwareUpdateJobId) {
        super("Firmware update job with id '"  + firmwareUpdateJobId + "' not found");
    }

}
