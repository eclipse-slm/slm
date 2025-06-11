package org.eclipse.slm.resource_management.service.rest.handler;

import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.common.awx.client.observer.IAwxJobObserverListener;
import org.eclipse.slm.common.awx.model.ExtraVars;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.client.apis.ConsulAclApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulNodesApiClient;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.consul.model.catalog.NodeService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakUtil;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.common.vault.client.VaultClient;
import org.eclipse.slm.notification_service.model.*;
import org.eclipse.slm.notification_service.service.client.NotificationServiceClient;
import org.eclipse.slm.resource_management.model.consul.capability.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.model.consul.capability.ScaleDownOperation;
import org.eclipse.slm.resource_management.model.consul.capability.ScaleOperation;
import org.eclipse.slm.resource_management.model.consul.capability.ScaleUpOperation;
import org.eclipse.slm.resource_management.service.rest.capabilities.CapabilitiesConsulClient;
import org.eclipse.slm.resource_management.service.rest.capabilities.MultiHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.model.actions.AwxAction;
import org.eclipse.slm.resource_management.model.actions.ActionType;
import org.eclipse.slm.resource_management.model.cluster.ClusterMemberType;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.util.*;

@Component
class ClusterScaleFunctions extends AbstractClusterFunctions implements IAwxJobObserverListener {

    private final static Logger LOG = LoggerFactory.getLogger(ClusterScaleFunctions.class);

    @Autowired
    protected ResourcesConsulClient resourcesConsulClient;

    public ClusterScaleFunctions(
            NotificationServiceClient notificationServiceClient,
            AwxJobExecutor awxJobExecutor,
            MultiTenantKeycloakRegistration multiTenantKeycloakRegistration,
            ConsulServicesApiClient consulServicesApiClient,
            ConsulAclApiClient consulAclApiClient,
            ConsulNodesApiClient consulNodesApiClient,
            CapabilitiesConsulClient capabilitiesConsulClient,
            MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient,
            KeycloakUtil keycloakUtil,
            AwxJobObserverInitializer awxJobObserverInitializer,
            VaultClient vaultClient) {
        super(
                notificationServiceClient,
                awxJobExecutor,
                multiTenantKeycloakRegistration,
                consulServicesApiClient,
                consulAclApiClient,
                consulNodesApiClient,
                capabilitiesConsulClient,
                multiHostCapabilitiesConsulClient,
                keycloakUtil,
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
                .filter(type -> type.getScalable())
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
        this.notificationServiceClient.postJobObserver(jwtAuthenticationToken, awxJobObserver);

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
        this.notificationServiceClient.postJobObserver(jwtAuthenticationToken, awxJobObserver);

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

            this.notificationServiceClient.postNotification(
                    clusterJob.getJwtAuthenticationToken(),
                    Category.RESOURCES,
                    jobTarget,
                    jobGoal
            );
            this.clusterJobMap.remove(sender.jobId);
        }
    }
}
