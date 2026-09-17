package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

public class DeploymentTargetNotFoundException extends RuntimeException {

    public DeploymentTargetNotFoundException(String submodelId) {
        super("No deployment target with submodel id '" + submodelId + "'");
    }
}
