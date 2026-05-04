package org.eclipse.slm.resource_management.features.capabilities.clusters.handler;

import org.eclipse.slm.awx.client.AwxCredential;
import org.eclipse.slm.awx.client.observer.*;
import org.eclipse.slm.awx.model.ExtraVars;

import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.common.vault.client.VaultClientFactory;
import org.eclipse.slm.notification_service.messaging.NotificationEventMessage;
import org.eclipse.slm.notification_service.messaging.NotificationMessageSender;
import org.eclipse.slm.notification_service.model.NotificationCategory;
import org.eclipse.slm.notification_service.model.NotificationEventType;
import org.eclipse.slm.notification_service.model.NotificationSubCategory;
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessManager;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.util.*;

@Component
public class ClusterDeleteFunctions extends AbstractClusterFunctions implements IAwxJobObserverListener {

    private final static Logger LOG = LoggerFactory.getLogger(ClusterDeleteFunctions.class);

    public ClusterDeleteFunctions(
            NotificationMessageSender notificationMessageSender,
            AwxJobExecutor awxJobExecutor,
            MultiTenantKeycloakRegistration multiTenantKeycloakRegistration,
            ConsulClientFactory consulClientFactory,
            CapabilitiesConsulClient capabilitiesConsulClient,
            MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient,
            AwxJobObserverInitializer awxJobObserverInitializer,
            VaultClientFactory vaultClientFactory,
            RemoteAccessManager remoteAccessManager) {
        super(
                notificationMessageSender,
                awxJobExecutor,
                multiTenantKeycloakRegistration,
                consulClientFactory,
                capabilitiesConsulClient,
                multiHostCapabilitiesConsulClient,
                awxJobObserverInitializer,
                vaultClientFactory,
                remoteAccessManager);
    }

    public ClusterJob createClusterJob(JwtAuthenticationToken jwtAuthenticationToken, MultiHostCapabilityService multiHostCapabilityService
    ) {
        var clusterJob = new ClusterJob(multiHostCapabilityService);

        AwxAction uninstallAction =
                (AwxAction) multiHostCapabilityService.getCapability().getActions().get(ActionType.UNINSTALL);

        Map<String, Object> extraVarsMap = new HashMap<>();
        extraVarsMap.put("resource_id",multiHostCapabilityService.getId().toString());
        extraVarsMap.put("keycloak_token", jwtAuthenticationToken.getToken().getTokenValue());
        extraVarsMap.put("service_name", multiHostCapabilityService.getServiceName());
        extraVarsMap.put("supported_connection_types", uninstallAction.getConnectionTypes());
        ExtraVars extraVars = new ExtraVars(extraVarsMap);

        var jobId = awxJobExecutor.executeJob(
                new AwxCredential(jwtAuthenticationToken),
                uninstallAction.getAwxRepo(),
                uninstallAction.getAwxBranch(),
                uninstallAction.getPlaybook(),
                extraVars
        );
        var awxJobObserver = this.awxJobObserverInitializer.initNewObserver(jobId, JobTarget.RESOURCE, JobGoal.DELETE, this);
        clusterJob.setAwxJobObserver(awxJobObserver);

        return clusterJob;
    }

    private void delete(JwtAuthenticationToken jwtAuthenticationToken, MultiHostCapabilityService multiHostCapabilityService
    ) throws SSLException, ConsulLoginFailedException {
        var clusterJob = createClusterJob(
                jwtAuthenticationToken,
                multiHostCapabilityService
        );

        clusterJob.setJwtAuthenticationToken(jwtAuthenticationToken);
        this.clusterJobMap.put(clusterJob.getAwxJobObserver(), clusterJob);
        multiHostCapabilityService.setStatus(CapabilityServiceStatus.UNINSTALL);
        multiHostCapabilitiesConsulClient.updateMultiHostCapabilityService(
                multiHostCapabilityService
        );
    }

    public void delete(JwtAuthenticationToken jwtAuthenticationToken, UUID consulServiceUuid
    ) throws SSLException, ConsulLoginFailedException {
        Optional<MultiHostCapabilityService> service = multiHostCapabilitiesConsulClient.getMultiHostCapabilityServiceOfUser(
                consulServiceUuid
        );

        if(service.isPresent()) {
            this.delete(jwtAuthenticationToken, service.get());
        }
    }

    @Override
    public void onJobStateChanged(AwxJobObserver sender, JobState newState) { }

    @Override
    public void onJobStateFinished(AwxJobObserver sender, JobFinalState finalState) {
        LOG.info("Job on cluster finished.");
        var jobGoal = sender.jobGoal;
        var jobTarget = sender.jobTarget;
        var clusterJob = this.clusterJobMap.get(sender);
        var jwtAuthenticationToken = clusterJob.getJwtAuthenticationToken();
        var multiHostCapabilityService = clusterJob.getMultiHostCapabilityService();

        if(!finalState.equals(JobFinalState.SUCCESSFUL)) {
            LOG.warn("Job with id='" + sender.jobId + "' finished not successful ('" + finalState.name().toString() + "')");
            this.clusterJobMap.remove(sender.jobId);
            return;
        }

        if (jobGoal.equals(JobGoal.DELETE)) {
            // Remove read access for secret of awx policy
            String resourceId = multiHostCapabilityService.getId().toString();
            this.vaultAdminClient.acl().removeRuleFromPolicy(
                    "awx",
                    "resources/data/"+ resourceId
            );

            // Delete secret from KV engine
            this.vaultAdminClient.kv("resources").deleteSecretFromKvEngine(resourceId);

            // remove policy
            this.vaultAdminClient.acl().deletePolicy(
                    "policy_resource_" + resourceId
            );
            // remove group
            this.vaultAdminClient.acl().deleteGroupByName(
                    "group_resource_" + resourceId
            );

            // Delete cluster representation in consul
            try {
                multiHostCapabilitiesConsulClient.removeMultiHostCapabilityService(
                        
                        multiHostCapabilityService.getServiceId()
                );
            } catch (ConsulLoginFailedException e) {
                LOG.error("Failed to delete MultiHostCapabilityService [id = '"+multiHostCapabilityService.getId()+"'] due to login error");
            }

            this.notificationMessageSender.sendMessage(new NotificationEventMessage(
                    jwtAuthenticationToken.getToken().getSubject(),
                    NotificationCategory.RESOURCES,
                    NotificationSubCategory.CLUSTER,
                    NotificationEventType.DELETED,
                    null)
            );
            this.clusterJobMap.remove(sender.jobId);
        }
    }
}
