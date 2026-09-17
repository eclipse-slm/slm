package org.eclipse.slm.resource_management.features.capabilities.aas;

import org.eclipse.slm.awx.client.AwxCredential;
import org.eclipse.slm.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.awx.client.observer.IAwxJobObserverListener;
import org.eclipse.slm.awx.client.observer.JobFinalState;
import org.eclipse.slm.awx.client.observer.JobGoal;
import org.eclipse.slm.awx.client.observer.JobState;
import org.eclipse.slm.awx.client.observer.JobTarget;
import org.eclipse.slm.awx.model.ExtraVars;
import org.eclipse.slm.common.aas.submodels.deployment.DeployRequest;
import org.eclipse.slm.common.aas.submodels.deployment.DeployResult;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentJobState;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Nimmt Deploy-Auftraege entgegen, prueft sie gegen die Faehigkeiten des Capability-Service,
 * startet das AWX-Playbook und fuehrt den Job-Zustand nach.
 */
@Component
public class DeploymentJobManager implements IAwxJobObserverListener {

    private final static Logger LOG = LoggerFactory.getLogger(DeploymentJobManager.class);

    private final CapabilitiesConsulClient capabilitiesConsulClient;
    private final AwxJobExecutor awxJobExecutor;
    private final AwxJobObserverInitializer awxJobObserverInitializer;
    private final DeploymentExtraVarsBuilder extraVarsBuilder;
    private final DeploymentJobStore jobStore;

    private final ConcurrentHashMap<AwxJobObserver, String> jobIdByObserver = new ConcurrentHashMap<>();
    private final AtomicLong jobCounter = new AtomicLong();

    public DeploymentJobManager(CapabilitiesConsulClient capabilitiesConsulClient,
                                AwxJobExecutor awxJobExecutor,
                                AwxJobObserverInitializer awxJobObserverInitializer,
                                DeploymentExtraVarsBuilder extraVarsBuilder,
                                DeploymentJobStore jobStore) {
        this.capabilitiesConsulClient = capabilitiesConsulClient;
        this.awxJobExecutor = awxJobExecutor;
        this.awxJobObserverInitializer = awxJobObserverInitializer;
        this.extraVarsBuilder = extraVarsBuilder;
        this.jobStore = jobStore;
    }

    /**
     * Note: takes a {@link JwtAuthenticationToken} rather than a plain token string, because the
     * external AwxCredential type (org.eclipse.slm:awx.client) offers no constructor for a plain
     * token string - only (username, password) or (JwtAuthenticationToken). The plain token value
     * needed for extraVars is extracted here via jwtAuthenticationToken.getToken().getTokenValue().
     */
    public DeployResult deploy(UUID capabilityServiceId, DeployRequest request, JwtAuthenticationToken jwtAuthenticationToken) {
        var capabilityServiceOptional = this.findCapabilityService(capabilityServiceId);
        if (capabilityServiceOptional.isEmpty()) {
            return DeployResult.rejected("No deployment capability service with id '" + capabilityServiceId + "'");
        }
        var capabilityService = capabilityServiceOptional.get();

        if (!(capabilityService.getCapability() instanceof DeploymentCapability capability)) {
            return DeployResult.rejected("Capability service '" + capabilityServiceId
                    + "' is not a deployment capability");
        }

        if (!capability.getSupportedDeploymentTypes().contains(request.deploymentType())) {
            return DeployResult.rejected("Deployment type '" + request.deploymentType()
                    + "' is not supported by '" + capability.getName() + "'");
        }

        var action = capability.getActions().get(ActionType.DEPLOY);
        if (!(action instanceof AwxAction awxAction)) {
            return DeployResult.rejected("Deployment capability '" + capability.getName()
                    + "' has no AWX deploy action");
        }

        var keycloakToken = jwtAuthenticationToken.getToken().getTokenValue();
        var extraVars = this.extraVarsBuilder.build(request, capabilityServiceId,
                capabilityService.getServiceName(), awxAction.getConnectionTypes(), keycloakToken);

        var jobId = "deploy-" + this.jobCounter.incrementAndGet() + "-" + request.serviceInstanceId();
        this.jobStore.put(jobId, DeploymentStatus.of(DeploymentJobState.RUNNING));

        try {
            var awxJobId = this.awxJobExecutor.executeJob(
                    new AwxCredential(jwtAuthenticationToken),
                    awxAction.getAwxRepo(), awxAction.getAwxBranch(), awxAction.getPlaybook(),
                    new ExtraVars(extraVars));
            var observer = this.awxJobObserverInitializer.initNewObserver(
                    awxJobId, JobTarget.SERVICE, JobGoal.CREATE, this);
            this.jobIdByObserver.put(observer, jobId);
        } catch (Exception e) {
            LOG.error("Failed to start deployment job '{}': {}", jobId, e.getMessage());
            this.jobStore.put(jobId, new DeploymentStatus(DeploymentJobState.FAILED, e.getMessage()));
            return DeployResult.rejected("Failed to start deployment: " + e.getMessage());
        }

        return DeployResult.accepted(jobId);
    }

    public DeploymentStatus getStatus(String jobId) {
        return this.jobStore.get(jobId);
    }

    @Override
    public void onJobStateChanged(AwxJobObserver sender, JobState newState) {
        // Zwischenzustaende sind fuer den Vertrag nicht relevant: RUNNING bleibt RUNNING.
    }

    @Override
    public void onJobStateFinished(AwxJobObserver sender, JobFinalState finalState) {
        var jobId = this.jobIdByObserver.remove(sender);
        if (jobId == null) {
            return;
        }

        if (finalState == JobFinalState.SUCCESSFUL) {
            this.jobStore.put(jobId, DeploymentStatus.of(DeploymentJobState.SUCCEEDED));
        } else {
            this.jobStore.put(jobId,
                    new DeploymentStatus(DeploymentJobState.FAILED, "AWX job finished as " + finalState));
        }
    }

    private Optional<CapabilityService> findCapabilityService(UUID capabilityServiceId) {
        return this.capabilitiesConsulClient.getCapabilityServices().stream()
                .filter(service -> capabilityServiceId.equals(service.getServiceId()))
                .findFirst();
    }
}
