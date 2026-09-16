package org.eclipse.slm.common.aas.submodels.deployment;

public enum DeploymentJobState {
    RUNNING,
    SUCCEEDED,
    FAILED,
    UNKNOWN;

    public boolean isTerminal() {
        return this != RUNNING;
    }
}
