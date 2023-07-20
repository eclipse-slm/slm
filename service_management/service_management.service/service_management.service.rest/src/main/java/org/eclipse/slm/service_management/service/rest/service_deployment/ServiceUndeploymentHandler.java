package org.eclipse.slm.service_management.service.rest.service_deployment;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.connected.ConnectedSubmodel;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;
import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.common.awx.client.observer.IAwxJobObserverListener;
import org.eclipse.slm.common.awx.model.ExtraVars;
import org.eclipse.slm.common.consul.client.apis.ConsulServicesApiClient;
import org.eclipse.slm.common.consul.model.catalog.CatalogService;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakUtil;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.notification_service.model.*;
import org.eclipse.slm.notification_service.service.client.NotificationServiceClient;
import org.eclipse.slm.resource_management.model.actions.ActionType;
import org.eclipse.slm.resource_management.service.client.ResourceManagementApiClientInitializer;
import org.eclipse.slm.resource_management.service.client.handler.ApiException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.model.offerings.kubernetes.KubernetesDeploymentDefinition;
import org.eclipse.slm.service_management.model.services.ServiceInstance;
import org.eclipse.slm.service_management.model.services.exceptions.ServiceInstanceNotFoundException;
import org.eclipse.slm.service_management.service.rest.service_instances.ServiceInstancesConsulClient;
import org.eclipse.slm.service_management.service.rest.service_offerings.ServiceOfferingVersionHandler;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.util.*;

@Component
public class ServiceUndeploymentHandler extends AbstractServiceDeploymentHandler implements IAwxJobObserverListener {

    public final static Logger LOG = LoggerFactory.getLogger(ServiceUndeploymentHandler.class);

    private final NotificationServiceClient notificationServiceClient;

    private final ConsulServicesApiClient consulServicesApiClient;

    private final KeycloakUtil keycloakUtil;

    private final ServiceOfferingVersionHandler serviceOfferingVersionHandler;

    private final ConnectedAssetAdministrationShellManager aasManager;

    private Map<AwxJobObserver, UndeploymentJobRun> observedAwxJobsToUndeploymentJobDetails = new HashMap<>();

    public ServiceUndeploymentHandler(
            AwxJobObserverInitializer awxJobObserverInitializer,
            AwxJobExecutor awxJobExecutor,
            NotificationServiceClient notificationServiceClient,
            ConsulServicesApiClient consulServicesApiClient,
            KeycloakUtil keycloakUtil,
            ResourceManagementApiClientInitializer resourceManagementApiClientInitializer,
            ServiceOfferingVersionHandler serviceOfferingVersionHandler,
            ServiceInstancesConsulClient serviceInstancesConsulClient,
            @Value("${basyx.aas-registry.url}") String aasRegistryUrl
    ) {
        super(resourceManagementApiClientInitializer, serviceInstancesConsulClient, awxJobObserverInitializer, awxJobExecutor);
        this.notificationServiceClient = notificationServiceClient;
        this.consulServicesApiClient = consulServicesApiClient;
        this.keycloakUtil = keycloakUtil;
        this.serviceOfferingVersionHandler = serviceOfferingVersionHandler;
        IAASRegistry registry = new AASRegistryProxy(aasRegistryUrl);
        this.aasManager = new ConnectedAssetAdministrationShellManager(registry);
    }

    public void deleteService(KeycloakPrincipal keycloakPrincipal, List<CatalogService> consulService)
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

        var serviceHoster = this.getServiceHoster(keycloakPrincipal, serviceInstance.getCapabilityServiceId());

        var awxCapabilityAction = this.getAwxDeployCapabilityAction(ActionType.UNDEPLOY, serviceHoster.getCapabilityService().getCapability());
        var awxGitRepoOfProject = awxCapabilityAction.getAwxRepo();
        var awxGitBranchOfProject = awxCapabilityAction.getAwxBranch();
        var awxPlaybook = awxCapabilityAction.getPlaybook();

        Map<String, Object> extraVarsMap = new HashMap<>() {{
            put("service_id", serviceInstance.getId().toString());
            put("resource_id", serviceInstance.getCapabilityServiceId());
            put("keycloak_token", keycloakPrincipal.getKeycloakSecurityContext().getTokenString());
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
        var awxJobId = awxJobExecutor.executeJob(new AwxCredential(keycloakPrincipal), awxGitRepoOfProject, awxGitBranchOfProject, awxPlaybook, extraVars);
        var awxJobObserver = awxJobObserverInitializer.init(awxJobId, jobTarget, jobGoal, this);
        this.observedAwxJobsToUndeploymentJobDetails.put(awxJobObserver, new UndeploymentJobRun(keycloakPrincipal, serviceInstance.getId(), serviceInstance.getResourceId()));
        this.notificationServiceClient.postJobObserver(keycloakPrincipal, awxJobId, jobTarget, jobGoal);
    }

    private void runSubmodelGarbageCollection(UUID resourceId) {
        IIdentifier iIdentifier = new CustomId(resourceId.toString());
        Map<String, ISubmodel> submodelMap;
        try {
            submodelMap = aasManager.retrieveSubmodels(iIdentifier);
        } catch (org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException e) {
            LOG.info("Garbage collection stopped because no submodels were available in AAS with ID " + resourceId);
            return;
        }

        for (Map.Entry<String, ISubmodel> entry: submodelMap.entrySet()) {
            try {
                LOG.info("Get information from submodel with ID = " + entry.getKey());
                Submodel submodel = ((ConnectedSubmodel) entry.getValue()).getLocalCopy();
            } catch(ResourceNotFoundException e) {
                LOG.info("Delete submodel with ID = " + entry.getKey() + " from AAS with ID = " + resourceId);
//                aasManager.deleteSubmodel(iIdentifier, new CustomId());
            }
        }
    }

    @Override
    public void onJobStateChanged(AwxJobObserver sender, JobState newState) {
    }

    @Override
    public void onJobStateFinished(AwxJobObserver sender, JobFinalState finalState) {
        if (this.observedAwxJobsToUndeploymentJobDetails.containsKey(sender)) {
            var jobDetails = this.observedAwxJobsToUndeploymentJobDetails.get(sender);
            var keycloakPrincipal = jobDetails.getKeycloakPrincipal();
            var userUuid = KeycloakTokenUtil.getUserUuid(keycloakPrincipal);
            var serviceInstanceId = jobDetails.getServiceInstanceId();
            var resourceId = jobDetails.getResourceId();

            switch (finalState) {
                case SUCCESSFUL -> {
                    // Remove role for service instance in Keycloak
                    var serviceKeycloakRoleName = "service_" + serviceInstanceId;
                    this.keycloakUtil.deleteRealmRole(keycloakPrincipal, serviceKeycloakRoleName);

                    // Remove Consul service of service instance
                    try {
                        var serviceInstance = this.serviceInstancesConsulClient.getServiceInstance(serviceInstanceId);
                        this.serviceInstancesConsulClient.deregisterConsulServiceForServiceInstance(serviceInstance);
                        notificationServiceClient.postNotification(keycloakPrincipal, Category.Services, JobTarget.SERVICE, JobGoal.DELETE);

                    } catch (ConsulLoginFailedException | ServiceInstanceNotFoundException e) {
                        LOG.error(e.getMessage());
                    }
                    runSubmodelGarbageCollection(resourceId);
                }

                default -> {
                    LOG.info("Service undeployed for service '" + serviceInstanceId + "' and user '" + userUuid + "' " +
                            "failed, because job '" + sender.jobId + "' " + finalState);
                }
            }
        }
    }
}
