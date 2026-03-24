
package org.eclipse.slm.resource_management.features.capabilities.jobs;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CapabilityAlreadyInstalledException extends RuntimeException {

    public CapabilityAlreadyInstalledException(UUID resourceId, UUID capabilityId, String capabilityName) {
        super("Deployment capability [id='" + capabilityId + "', name='" + capabilityName +"'] already installed on resource [id='" + resourceId + "']");
    }

}
