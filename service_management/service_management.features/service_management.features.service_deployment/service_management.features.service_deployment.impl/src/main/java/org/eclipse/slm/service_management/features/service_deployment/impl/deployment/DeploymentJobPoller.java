package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.slm.common.aas.submodels.deployment.DeploymentJobState;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

/**
 * Wartet auf den Abschluss eines Deployment-Jobs am Ziel. Bewusst Polling statt Push:
 * Ein Fremd-Asset kann keine SLM-Events senden, waere ohne Polling also nicht beobachtbar.
 */
@Component
public class DeploymentJobPoller {

    private final static Logger LOG = LoggerFactory.getLogger(DeploymentJobPoller.class);

    private final AasDeploymentClient deploymentClient;
    private final Duration pollInterval;
    private final Duration timeout;

    public DeploymentJobPoller(AasDeploymentClient deploymentClient,
                               @Value("${service-deployment.poll-interval:5s}") Duration pollInterval,
                               @Value("${service-deployment.poll-timeout:30m}") Duration timeout) {
        this.deploymentClient = deploymentClient;
        this.pollInterval = pollInterval;
        this.timeout = timeout;
    }

    @Async
    public void awaitTerminalState(DeploymentTarget target, String jobId, String accessToken,
                                   Consumer<DeploymentStatus> onTerminalState) {
        var deadline = Instant.now().plus(this.timeout);

        while (Instant.now().isBefore(deadline)) {
            DeploymentStatus status;
            try {
                status = this.deploymentClient.getStatus(target, jobId, accessToken);
            } catch (DeploymentInvocationException e) {
                LOG.error("Deployment job '{}' can no longer be polled: {}", jobId, e.getMessage());
                onTerminalState.accept(new DeploymentStatus(DeploymentJobState.FAILED, e.getMessage()));
                return;
            }

            if (status.state().isTerminal()) {
                onTerminalState.accept(status);
                return;
            }

            try {
                Thread.sleep(this.pollInterval.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                onTerminalState.accept(new DeploymentStatus(DeploymentJobState.FAILED,
                        "Waiting for deployment job '" + jobId + "' was interrupted"));
                return;
            }
        }

        LOG.warn("Deployment job '{}' did not finish within {}", jobId, this.timeout);
        onTerminalState.accept(new DeploymentStatus(DeploymentJobState.FAILED,
                "Deployment job '" + jobId + "' did not finish within " + this.timeout
                        + " and may still be running on the target"));
    }
}
