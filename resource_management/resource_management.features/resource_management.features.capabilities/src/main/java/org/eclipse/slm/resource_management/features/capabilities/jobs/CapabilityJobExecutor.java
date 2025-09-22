package org.eclipse.slm.resource_management.features.capabilities.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.awx.client.observer.*;
import org.eclipse.slm.common.awx.model.*;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.client.KeycloakServiceClient;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.CapabilitiesService;
import org.eclipse.slm.resource_management.features.capabilities.CapabilityUtil;
import org.eclipse.slm.resource_management.features.capabilities.exceptions.CapabilityRuntimeException;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.eclipse.slm.resource_management.features.capabilities.persistence.SingleHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.persistence.SingleHostCapabilitiesVaultClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class CapabilityJobExecutor implements IAwxJobObserverListener {
    private final static Logger LOG = LoggerFactory.getLogger(CapabilityJobExecutor.class);

    private final CapabilitiesService capabilitiesService;

    private final CapabilityUtil capabilityUtil;

    private final ObjectMapper objectMapper;

    private final AwxJobExecutor awxJobExecutor;

    private final AwxJobObserverInitializer awxJobObserverInitializer;

    private final SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient;

    private final List<CapabilityJobExecutorListener> capabilityJobExecutorListeners = new ArrayList<>();

    private final CapabilityJob capabilityJob;

    private final CapabilityService capabilityService;

    private final int awxJobTimeoutInMin;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<AwxJobObserver, ScheduledFuture<?>> awxJobObserverTimeouts = new HashMap<>();

    public CapabilityJobExecutor(CapabilitiesService capabilitiesService,
                                 CapabilityUtil capabilityUtil,
                                 AwxJobExecutor awxJobExecutor,
                                 AwxJobObserverInitializer awxJobObserverInitializer,
                                 SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient,
                                 @NonNull CapabilityJob capabilityJob,
                                 @NonNull CapabilityService capabilityService,
                                 @Value("${resource-management.capabilities.awx-job-timeout-in-minutes:20}") int awxJobTimeoutInMin) {
        this.capabilitiesService = capabilitiesService;
        this.capabilityUtil = capabilityUtil;
        this.awxJobExecutor = awxJobExecutor;
        this.awxJobObserverInitializer = awxJobObserverInitializer;
        this.singleHostCapabilitiesConsulClient = singleHostCapabilitiesConsulClient;
        this.capabilityJob = capabilityJob;
        this.capabilityService = capabilityService;
        this.awxJobTimeoutInMin = awxJobTimeoutInMin;
        this.objectMapper = new ObjectMapper();
    }

    public void addCapabilityJobExecutorListener(CapabilityJobExecutorListener listener) {
        this.capabilityJobExecutorListeners.add(listener);
    }

    public void installCapabilityOnResource(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID resourceId,
            Map<String, String> configParameters
    ) throws ResourceNotFoundException {
        var capability = capabilityService.getCapability();

        if (!capability.getActions().containsKey(ActionType.INSTALL)) {
            throw new CapabilityJobRuntimeException(capability.getId(), "Capability [id='" + capability.getId() + "'] has no INSTALL action");
        }

        var capabilityInstallAction = capability.getActions().get(ActionType.INSTALL);
        if (!(capabilityInstallAction instanceof AwxAction awxInstallCapabilityAction)) {
            throw new CapabilityJobRuntimeException(capability.getId(), "Capability action type [type='" + capabilityInstallAction.getClass() + "'] " +
                    "not implemented");
        }

        try {
            var awxCredential = new AwxCredential(jwtAuthenticationToken);
            var consulCredential = new ConsulCredential();

            Map<String, String> secretConfigParams = capabilityUtil.getSecretConfigParameter(capability, configParameters);
            Map<String, String> nonSecretConfigParams = capabilityUtil.getNonSecretConfigParameter(capability, configParameters);

            Map<String, Object> extraVars = new HashMap<>();
            extraVars.put("keycloak_token", KeycloakTokenUtil.getToken(jwtAuthenticationToken));
            extraVars.put("resource_id", resourceId.toString());
            extraVars.put("service_name", capabilityService.getService());
            extraVars.put("supported_connection_types", capabilityInstallAction.getConnectionTypes());
            if(!nonSecretConfigParams.isEmpty())
                extraVars.put("consul_service_meta", objectMapper.writeValueAsString(nonSecretConfigParams));
            if(!secretConfigParams.isEmpty())
                extraVars.put("vault_service_secrets", objectMapper.writeValueAsString(secretConfigParams));

            int awxJobId = awxJobExecutor.executeJob(
                    awxCredential,
                    awxInstallCapabilityAction.getAwxRepo(), awxInstallCapabilityAction.getAwxBranch(), awxInstallCapabilityAction.getPlaybook(),
                    new ExtraVars(extraVars)
            );
            var awxJobObserver = this.awxJobObserverInitializer.initNewObserver(
                    awxJobId, JobTarget.DEPLOYMENT_CAPABILITY, JobGoal.ADD, this
            );

            var timeoutFuture = scheduler.schedule(() -> {
                for (var listener : this.capabilityJobExecutorListeners) {
                    listener.onError(this.capabilityJob, this.capabilityService,
                            new CapabilityJobRuntimeException(this.capabilityJob.getId(),
                                    "AWX Job Observer Timeout, job did not finished after " + this.awxJobTimeoutInMin + " minutes"));
                    awxJobObserver.removeListener(this);
                }
            }, this.awxJobTimeoutInMin, TimeUnit.MINUTES);
            this.awxJobObserverTimeouts.put(awxJobObserver, timeoutFuture);

        } catch (Exception e) {
            throw new CapabilityRuntimeException(capability.getId(),
                    "Failed to install capability [id='" + capability.getId() + "'] on resource [id='" + resourceId + "']: " + e.getMessage());
        }
    }

    public void uninstallCapabilityFromResource(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID resourceId) {
        var capability = this.capabilityService.getCapability();

        if (capabilityService.getManaged()) {
            LOG.warn("Uninstalling capability [id='" + capability.getId() + "'] from resource [id='" + resourceId + "'] is not possible, because the " +
                    "capability is managed externally.");
            return;
        }

        try {
            var awxCredential = new AwxCredential(jwtAuthenticationToken);
            var consulCredential = new ConsulCredential();

            var uninstallAction = (AwxAction) capability.getActions().get(ActionType.UNINSTALL);
            Map<String, Object> extraVars = new HashMap<>();
            extraVars.put("keycloak_token", KeycloakTokenUtil.getToken(jwtAuthenticationToken));
            extraVars.put("resource_id", resourceId.toString());
            extraVars.put("service_name", capabilityService.getService());
            extraVars.put("supported_connection_types", uninstallAction.getConnectionTypes());

            int awxJobId = awxJobExecutor.executeJob(
                    awxCredential,
                    uninstallAction.getAwxRepo(), uninstallAction.getAwxBranch(), uninstallAction.getPlaybook(),
                    new ExtraVars(extraVars)
            );

            capabilityService.setStatus(CapabilityServiceStatus.UNINSTALL);
            singleHostCapabilitiesConsulClient.updateCapabilityService(consulCredential, resourceId, capabilityService);

            var awxJobObserver = this.awxJobObserverInitializer.initNewObserver(awxJobId, JobTarget.DEPLOYMENT_CAPABILITY, JobGoal.DELETE, this);

            var timeoutFuture = scheduler.schedule(() -> {
                for (var listener : this.capabilityJobExecutorListeners) {
                    listener.onError(this.capabilityJob, this.capabilityService,
                            new CapabilityJobRuntimeException(this.capabilityJob.getId(),
                                    "AWX Job Observer Timeout, job did not finished after " + this.awxJobTimeoutInMin + " minutes"));
                    awxJobObserver.removeListener(this);
                }
            }, this.awxJobTimeoutInMin, TimeUnit.MINUTES);
            this.awxJobObserverTimeouts.put(awxJobObserver, timeoutFuture);

        } catch (Exception e) {
            throw new CapabilityRuntimeException(capability.getId(), "Failed to uninstall capability [id='" + capability.getId() + "'] from resource [id='" + resourceId + "']: " + e.getMessage());
        }
    }

    //region IAwxJobObserverListener
    @Override
    public void onJobStateChanged(AwxJobObserver sender, JobState newState) {
        LOG.debug("Job state changed to '" + newState + "' for capability [id='" + this.capabilityService.getCapability().getId() + "'] on resource [id='" + this.capabilityJob.getResourceId() + "']");
    }

    @Override
    public void onJobStateFinished(
            AwxJobObserver awxJobObserver,
            JobFinalState finalState
    ) {
        var jobGoal = awxJobObserver.jobGoal;
        var resourceId = this.capabilityJob.getResourceId();
        var capabilityId = this.capabilityJob.getCapabilityId();
        var awxJobObserverTimeout = this.awxJobObserverTimeouts.get(awxJobObserver);
        if (awxJobObserverTimeout != null) {
            awxJobObserverTimeout.cancel(false);
            this.awxJobObserverTimeouts.remove(awxJobObserver);
        }

        if (finalState != JobFinalState.SUCCESSFUL) {
            for (var listener : this.capabilityJobExecutorListeners) {
                listener.onError(this.capabilityJob, this.capabilityService, new CapabilityRuntimeException(capabilityId,
                        "Failed to install capability [id='" + capabilityId + "'] on resource [id='" + resourceId + "']: Job final state was '" + finalState + "'"));
                awxJobObserver.removeListener(this);
            }
            return;
        }

        try {
            switch (jobGoal) {
                case ADD -> {
                    for (var listener : this.capabilityJobExecutorListeners) {
                        listener.onCapabilityInstalled(this.capabilityJob, this.capabilityService);
                    }
                    LOG.debug("Successfully installed capability [id='" + capabilityId + "']" + "to resource '" + resourceId + "'");
                }

                case DELETE -> {
                    for (var listener : this.capabilityJobExecutorListeners) {
                        listener.onCapabilityUninstalled(this.capabilityJob, this.capabilityService);
                    }
                    LOG.debug("Successfully removed deployment capability [id='" + capabilityId + "']" + "from resource '" + resourceId + "'");
                }

                default -> {
                    for (var listener : this.capabilityJobExecutorListeners) {
                        listener.onError(this.capabilityJob, this.capabilityService, new CapabilityJobRuntimeException(this.capabilityJob.getId(),
                                "Failed to install capability [id='" + capabilityId + "'] on resource [id='" + resourceId + "']: " + "Unsupported JobGoal '" + jobGoal + "'"));
                    }
                }
            }
        } catch (Exception e) {
            for (var listener : this.capabilityJobExecutorListeners) {
                listener.onError(this.capabilityJob, this.capabilityService, new CapabilityJobRuntimeException(this.capabilityJob.getId(),
                        "Failed to install capability [id='" + capabilityId + "'] on resource [id='" + resourceId + "']: " + e.getMessage()));
            }
        }
    }
    //endregion IAwxJobObserverListener
}
