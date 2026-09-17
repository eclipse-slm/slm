package org.eclipse.slm.resource_management.features.capabilities.aas;

import org.eclipse.slm.common.aas.submodels.deployment.DeploymentJobState;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Haelt den Zustand laufender und abgeschlossener Deployment-Jobs.
 * Bewusst im Speicher: Ein Neustart verliert die Zustaende, genau wie der AwxJobObserver heute.
 * Die persistente Wiederaufnahme ist in der Spec als bekannte Luecke vermerkt.
 */
@Component
public class DeploymentJobStore {

    private final Map<String, DeploymentStatus> statusByJobId = new ConcurrentHashMap<>();

    public void put(String jobId, DeploymentStatus status) {
        this.statusByJobId.put(jobId, status);
    }

    public DeploymentStatus get(String jobId) {
        return this.statusByJobId.getOrDefault(jobId,
                new DeploymentStatus(DeploymentJobState.UNKNOWN, "No job with id '" + jobId + "'"));
    }
}
