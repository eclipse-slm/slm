
package org.eclipse.slm.resource_management.features.device_integration.firmware_update.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class FirmwareUpdateFileNotFoundException extends RuntimeException {

    public FirmwareUpdateFileNotFoundException(String softwareNameplateId) {
        super("Firmware update file for software nameplate with id '"  + softwareNameplateId + "' not found");
    }

}
