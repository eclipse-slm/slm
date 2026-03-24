package org.eclipse.slm.resource_management.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ResourceAlreadyExistsException extends Exception {

    public ResourceAlreadyExistsException(String message)
    {
        super(message);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public static class DeploymentCapabilityNotSupported extends Exception {

        public DeploymentCapabilityNotSupported(String deploymentCapability)
        {
            super("Deployment Capability '"  + deploymentCapability + "' not supported");
        }

    }
}
