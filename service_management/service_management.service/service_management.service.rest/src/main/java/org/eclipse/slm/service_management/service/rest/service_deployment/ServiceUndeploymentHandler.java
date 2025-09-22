package org.eclipse.slm.service_management.service.rest.service_deployment;

import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.awx.client.observer.*;
import org.eclipse.slm.common.awx.model.ExtraVars;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.service.client.ResourceManagementApiClientInitializer;
import org.eclipse.slm.resource_management.service.client.handler.ApiException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.kubernetes.KubernetesDeploymentDefinition;
import org.eclipse.slm.service_management.model.services.ServiceInstance;
import org.eclipse.slm.service_management.model.services.exceptions.ServiceInstanceNotFoundException;
import org.eclipse.slm.service_management.service.rest.service_instances.ServiceInstanceEventMessageSender;
import org.eclipse.slm.service_management.service.rest.service_instances.ServiceInstanceEventType;
import org.eclipse.slm.service_management.service.rest.service_instances.ServiceInstancesConsulClient;
import org.eclipse.slm.service_management.service.rest.service_offerings.ServiceOfferingVersionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.util.*;

@Component
public class ServiceUndeploymentHandler extends AbstractServiceDeploymentHandler implements IAwxJobObserverListener {

    public final static Logger LOG = LoggerFactory.getLogger(ServiceUndeploymentHandler.class);

    private final ServiceInstanceEventMessageSender serviceInstanceEventMessageSender;

    private final ConsulServicesApiClient consulServicesApiClient;

    private final KeycloakAdminClient keycloakAdminClient;

    private final ServiceOfferingVersionHandler serviceOfferingVersionHandler;

    private Map<AwxJobObserver, UndeploymentJobRun> observedAwxJobsToUndeploymentJobDetails = new HashMap<>();

    public ServiceUndeploymentHandler(
            AwxJobObserverInitializer awxJobObserverInitializer,
            AwxJobExecutor awxJobExecutor,
            ConsulServicesApiClient consulServicesApiClient,
            KeycloakAdminClient keycloakAdminClient,
            ResourceManagementApiClientInitializer resourceManagementApiClientInitializer,
            ServiceOfferingVersionHandler serviceOfferingVersionHandler,
            ServiceInstancesConsulClient serviceInstancesConsulClient,
            ServiceInstanceEventMessageSender serviceInstanceEventMessageSender
    ) {
        super(resourceManagementApiClientInitializer, serviceInstancesConsulClient, awxJobObserverInitializer, awxJobExecutor);
        this.consulServicesApiClient = consulServicesApiClient;
        this.keycloakAdminClient = keycloakAdminClient;
        this.serviceOfferingVersionHandler = serviceOfferingVersionHandler;
        this.serviceInstanceEventMessageSender = serviceInstanceEventMessageSender;
    }

    public void deleteService(JwtAuthenticationToken jwtAuthenticationToken, List<CatalogService> consulService)
            throws SSLException, ApiException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, CapabilityServiceNotFoundException {
        if (consulService.isEmpty())
        {
            throw new RuntimeException("List contains no services");
        }

        var serviceMetaData = consulService.get(0).getServiceMeta();
        var serviceTags = consulService.get(0).getServiceTags();
        var serviceInstance = ServiceInstance.Companion.ofMetaDataAndTags(new HashMap<>(serviceMetaData), new ArrayList<>(serviceTags));

        var serviceOfferingVersion = serviceOfferingVersionHandler
                .getServiceOfferingVersionById(serviceInstance.getServiceOfferingId(), serviceInstance.getServiceOfferingVersionId());

        var serviceHoster = this.getServiceHoster(jwtAuthenticationToken, serviceInstance.getCapabilityServiceId());

        var awxCapabilityAction = this.getAwxDeployCapabilityAction(ActionType.UNDEPLOY, serviceHoster.getCapabilityService().getCapability());
        var awxGitRepoOfProject = awxCapabilityAction.getAwxRepo();
        var awxGitBranchOfProject = awxCapabilityAction.getAwxBranch();
        var awxPlaybook = awxCapabilityAction.getPlaybook();

        Map<String, Object> extraVarsMap = new HashMap<>() {{
            put("service_id", serviceInstance.getId().toString());
            put("keycloak_token", jwtAuthenticationToken.getToken().getTokenValue());
            put("service_name", serviceHoster.getCapabilityService().getService());
            put("supported_connection_types", awxCapabilityAction.getConnectionTypes());
        }};

        switch (serviceOfferingVersion.getDeploymentType()) {
            case KUBERNETES: {
                var kubernetesServiceOffering = (KubernetesDeploymentDefinition) serviceOfferingVersion.getDeploymentDefinition();
                extraVarsMap.put("manifest_file", kubernetesServiceOffering.getManifestFile());
            }
        }

        var extraVars = new ExtraVars(extraVarsMap);

        var jobTarget = JobTarget.SERVICE;
        var jobGoal = JobGoal.DELETE;
        var awxJobId = awxJobExecutor.executeJob(new AwxCredential(jwtAuthenticationToken), awxGitRepoOfProject, awxGitBranchOfProject, awxPlaybook, extraVars);
        var awxJobObserver = awxJobObserverInitializer.initNewObserver(awxJobId, jobTarget, jobGoal, this);
        this.observedAwxJobsToUndeploymentJobDetails.put(awxJobObserver, new UndeploymentJobRun(jwtAuthenticationToken, serviceInstance.getId(), serviceInstance.getResourceId()));
    }
    @Override
    public void onJobStateChanged(AwxJobObserver sender, JobState newState) {
    }

    @Override
    public void onJobStateFinished(AwxJobObserver sender, JobFinalState finalState) {
        if (this.observedAwxJobsToUndeploymentJobDetails.containsKey(sender)) {
            var jobDetails = this.observedAwxJobsToUndeploymentJobDetails.get(sender);
            var jwtAuthenticationToken = jobDetails.getJwtAuthenticationToken();
            var userUuid = KeycloakTokenUtil.getUserUuid(jwtAuthenticationToken);
            var serviceInstanceId = jobDetails.getServiceInstanceId();
            var resourceId = jobDetails.getResourceId();

            switch (finalState) {
                case SUCCESSFUL -> {
                    // Remove role for service instance in Keycloak
                    var serviceKeycloakRoleName = "service_" + serviceInstanceId;
                    this.keycloakAdminClient.deleteRealmRole(serviceKeycloakRoleName);

                    // Remove Consul service of service instance
                    try {
                        var serviceInstance = this.serviceInstancesConsulClient.getServiceInstance(serviceInstanceId);
                        this.serviceInstancesConsulClient.deregisterConsulServiceForServiceInstance(serviceInstance);
                        this.serviceInstanceEventMessageSender.sendMessage(serviceInstance, ServiceInstanceEventType.DELETED);
                    } catch (ConsulLoginFailedException | ServiceInstanceNotFoundException e) {
                        LOG.error(e.getMessage());
                    }
                }

                default -> {
                    LOG.info("Service undeployed for service '" + serviceInstanceId + "' and user '" + userUuid + "' " +
                            "failed, because job '" + sender.jobId + "' " + finalState);
                }
            }
        }
    }
}
