package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

public class DeploymentInvocationException extends RuntimeException {

    public DeploymentInvocationException(String submodelId, String operation, Throwable cause) {
        super("Invoking '" + operation + "' on deployment target '" + submodelId + "' failed: "
                + cause.getMessage(), cause);
    }
}
