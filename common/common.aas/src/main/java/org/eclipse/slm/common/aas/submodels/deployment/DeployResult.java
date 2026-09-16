package org.eclipse.slm.common.aas.submodels.deployment;

public record DeployResult(String jobId, boolean accepted, String message) {

    public static DeployResult accepted(String jobId) {
        return new DeployResult(jobId, true, "");
    }

    public static DeployResult rejected(String message) {
        return new DeployResult("", false, message);
    }
}
