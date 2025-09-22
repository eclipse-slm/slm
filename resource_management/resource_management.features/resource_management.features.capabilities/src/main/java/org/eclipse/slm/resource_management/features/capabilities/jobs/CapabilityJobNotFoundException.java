
package org.eclipse.slm.resource_management.features.capabilities.jobs;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CapabilityJobNotFoundException extends RuntimeException {

    public CapabilityJobNotFoundException(UUID capabilityJobId) {
        super("Capability job with id '"  + capabilityJobId + "' not found");
    }

    public CapabilityJobNotFoundException(UUID resourceId, UUID capabilityId) {
        super("No capability job for resource with id '" + resourceId + "' and capability with id '" + capabilityId + "' found");
    }

}
