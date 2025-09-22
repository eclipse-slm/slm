package org.eclipse.slm.resource_management.features.capabilities.clusters.handler;

import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.awx.client.observer.*;
import org.eclipse.slm.common.awx.model.ExtraVars;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.notification_service.messaging.NotificationEventMessage;
import org.eclipse.slm.notification_service.messaging.NotificationMessageSender;
import org.eclipse.slm.notification_service.model.NotificationCategory;
import org.eclipse.slm.notification_service.model.NotificationEventType;
import org.eclipse.slm.notification_service.model.NotificationSubCategory;
import org.eclipse.slm.resource_management.common.adapters.ResourcesConsulClient;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.features.capabilities.clusters.*;
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.ClusterMemberType;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.util.*;

@Component
public class ClusterScaleFunctions extends AbstractClusterFunctions implements IAwxJobObserverListener {

    private final static Logger LOG = LoggerFactory.getLogger(ClusterScaleFunctions.class);

    @Autowired
    protected ResourcesConsulClient resourcesConsulClient;

    public ClusterScaleFunctions(
            NotificationMessageSender notificationMessageSender,
            AwxJobExecutor awxJobExecutor,
            MultiTenantKeycloakRegistration multiTenantKeycloakRegistration,
            ConsulServicesApiClient consulServicesApiClient,
            ConsulAclApiClient consulAclApiClient,
            ConsulNodesApiClient consulNodesApiClient,
            CapabilitiesConsulClient capabilitiesConsulClient,
            MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient,
            KeycloakAdminClient keycloakAdminClient,
            AwxJobObserverInitializer awxJobObserverInitializer,
            VaultClient vaultClient) {
        super(
                notificationMessageSender,
                awxJobExecutor,
                multiTenantKeycloakRegistration,
                consulServicesApiClient,
                consulAclApiClient,
                consulNodesApiClient,
                capabilitiesConsulClient,
                multiHostCapabilitiesConsulClient,
                keycloakAdminClient,
                awxJobObserverInitializer,
                vaultClient);
    }

    //TODO: Make one function for scaleUp/scaleDown because scaleUp/Down almost identical
    public int scaleUp(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID consulServiceUuid,
            UUID resourceId
    ) throws SSLException, ConsulLoginFailedException, ResourceNotFoundException {
        Optional<MultiHostCapabilityService> serviceOptional = multiHostCapabilitiesConsulClient.getMultiHostCapabilityServiceOfUser(
                new ConsulCredential(jwtAuthenticationToken),
                consulServiceUuid
        );

        if(serviceOptional.isEmpty()) {
            LOG.warn("Cluster with id = \""+consulServiceUuid+"\" has no been found.");
            return -1;
        }

        MultiHostCapabilityService service = serviceOptional.get();
        AwxAction scaleUpAction = (AwxAction) service.getCapability().getActions().get(ActionType.SCALE_UP);
        Optional<ClusterMemberType> clusterMemberTypeOptional = service.getCapability().getClusterMemberTypes()
                .stream()
                .findFirst();

        if(clusterMemberTypeOptional.isEmpty()) {
            LOG.warn("Cluster with id = \""+service.getId()+"\" has no cluster member types which are scalable.");
            return -1;
        }
        JobTarget jobTarget = JobTarget.RESOURCE;
        JobGoal jobGoal = JobGoal.MODIFY;

        Optional<NodeService> resourceToAddAsSerivce = this.resourcesConsulClient.getRemoteAccessServiceOfResourceAsNodeService(new ConsulCredential(), resourceId);

        if (!resourceToAddAsSerivce.isPresent()){
            LOG.error("Could not find RemoteAccessService for resource (id='" + resourceId + "') in order to add it to cluster ('" + service.getService() + "'). Aborting!");
            throw new ResourceNotFoundException(resourceId);
        }

        Map<String, Object> extraVarsMap = new HashMap<>();
        extraVarsMap.put("resource_service_id", service.getService());
        extraVarsMap.put("resource_to_add_service_id", resourceToAddAsSerivce.get().getService());
        extraVarsMap.put("keycloak_token", jwtAuthenticationToken.getToken().getTokenValue());
        extraVarsMap.put("supported_connection_types", scaleUpAction.getConnectionTypes());
        ExtraVars extraVars = new ExtraVars(extraVarsMap);

        int jobId = awxJobExecutor.executeJob(
                new AwxCredential(jwtAuthenticationToken),
                scaleUpAction.getAwxRepo(), scaleUpAction.getAwxBranch(), scaleUpAction.getPlaybook(),
                extraVars
        );
        var awxJobObserver =this.awxJobObserverInitializer.initNewObserver(
                jobId,
                jobTarget,
                jobGoal,
                this
        );

        var clusterJob = new ClusterJob(service);
        clusterJob.setJwtAuthenticationToken(jwtAuthenticationToken);
        clusterJob.setAwxJobObserver(awxJobObserver);
        clusterJob.setScaleOperation(new ScaleUpOperation(resourceId, clusterMemberTypeOptional.get()));
        this.clusterJobMap.put(clusterJob.getAwxJobObserver(), clusterJob);

        return jobId;
    }

    public int scaleDown(
            JwtAuthenticationToken jwtAuthenticationToken,
            UUID consulServiceUuid,
            UUID resourceId
    ) throws SSLException, ConsulLoginFailedException, ResourceNotFoundException {
        JobTarget jobTarget = JobTarget.RESOURCE;
        JobGoal jobGoal = JobGoal.MODIFY;
        Optional<MultiHostCapabilityService> serviceOptional = multiHostCapabilitiesConsulClient.getMultiHostCapabilityServiceOfUser(
                new ConsulCredential(jwtAuthenticationToken),
                consulServiceUuid
        );

        if(serviceOptional.isEmpty())
            return -1;

        MultiHostCapabilityService service = serviceOptional.get();
        AwxAction scaleDownAction = (AwxAction) service.getCapability().getActions().get(ActionType.SCALE_DOWN);

        Optional<NodeService> resourceToAddAsSerivce = this.resourcesConsulClient.getRemoteAccessServiceOfResourceAsNodeService(new ConsulCredential(), resourceId);

        if (!resourceToAddAsSerivce.isPresent()){
            LOG.error("Could not find RemoteAccessService for resource (id='" + resourceId + "') in order to remove it from cluster ('" + service.getService() + "'). Aborting!");
            throw new ResourceNotFoundException(resourceId);
        }

        Map<String, Object> extraVarsMap = new HashMap<>();
        extraVarsMap.put("resource_service_id", service.getService());
        extraVarsMap.put("resource_to_add_service_id", resourceToAddAsSerivce.get().getService());
        extraVarsMap.put("keycloak_token", jwtAuthenticationToken.getToken().getTokenValue());
        extraVarsMap.put("supported_connection_types", scaleDownAction.getConnectionTypes());
        ExtraVars extraVars = new ExtraVars(extraVarsMap);

        int jobId = awxJobExecutor.executeJob(
                new AwxCredential(jwtAuthenticationToken),
                scaleDownAction.getAwxRepo(), scaleDownAction.getAwxBranch(), scaleDownAction.getPlaybook(),
                extraVars
        );

        var awxJobObserver = this.awxJobObserverInitializer.initNewObserver(jobId, jobTarget, jobGoal, this);

        var clusterJob = new ClusterJob(service);
        clusterJob.setJwtAuthenticationToken(jwtAuthenticationToken);
        clusterJob.setAwxJobObserver(awxJobObserver);
        clusterJob.setScaleOperation(new ScaleDownOperation(resourceId));
        this.clusterJobMap.put(clusterJob.getAwxJobObserver(), clusterJob);

        return jobId;
    }

    @Override
    public void onJobStateChanged(AwxJobObserver sender, JobState newState) { }

    @Override
    public void onJobStateFinished(AwxJobObserver sender, JobFinalState finalState) {
        LOG.info("Job on cluster is finished.");
        JobGoal jobGoal = sender.jobGoal;
        JobTarget jobTarget = sender.jobTarget;
        ClusterJob clusterJob = this.clusterJobMap.get(sender);
        var jwtAuthenticationToken = clusterJob.getJwtAuthenticationToken();
        MultiHostCapabilityService multiHostCapabilityService = clusterJob.getMultiHostCapabilityService();

        if(!finalState.equals(JobFinalState.SUCCESSFUL)) {
            LOG.warn("Job with id='" + sender.jobId + "' finished not successful ('" + finalState.name().toString() + "')");
            this.clusterJobMap.remove(sender.jobId);
            return;
        }

        if (jobGoal.equals(JobGoal.MODIFY)) {
            ScaleOperation scaleOperation = clusterJob.getScaleOperation();
            Class<? extends ScaleOperation> scaleClass = scaleOperation.getClass();

            multiHostCapabilitiesConsulClient.scaleMultiHostCapabilityService(
                    new ConsulCredential(),
                    scaleOperation,
                    multiHostCapabilityService.getId()
            );

            this.notificationMessageSender.sendMessage(new NotificationEventMessage(
                    KeycloakTokenUtil.getUserUuid(jwtAuthenticationToken),
                    NotificationCategory.RESOURCES, NotificationSubCategory.CLUSTER, NotificationEventType.MODIFIED,
                    null
            ));
            this.clusterJobMap.remove(sender.jobId);
        }
    }
}
