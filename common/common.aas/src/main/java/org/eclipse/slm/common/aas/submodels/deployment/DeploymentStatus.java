package org.eclipse.slm.common.aas.submodels.deployment;

public record DeploymentStatus(DeploymentJobState state, String message) {

    public static DeploymentStatus of(DeploymentJobState state) {
        return new DeploymentStatus(state, "");
    }
}
