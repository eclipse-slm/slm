package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.slm.awx.client.observer.*;
import org.eclipse.slm.awx.model.ExtraVars;
import org.eclipse.slm.common.consul.client.ConsulClient;
import org.eclipse.slm.common.consul.client.ConsulClientFactory;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.eclipse.slm.resource_management.features.capabilities.model.SingleHostCapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.service.client.ResourceManagementClientFactory;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.CapabilityServiceNotFoundException;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.eclipse.slm.service_management.features.service_deployment.impl.DeploymentJobRun;
import org.eclipse.slm.service_management.features.service_deployment.impl.KubernetesManifestFile;
import org.eclipse.slm.service_management.features.service_deployment.api.deploymentdefinitions.dockercompose.DockerComposeFile;
import org.eclipse.slm.service_management.features.service_deployment.api.deploymentdefinitions.dockercompose.DockerComposeFileParser;
import org.eclipse.slm.service_management.features.service_deployment.api.deploymentdefinitions.kubernetes.KubernetesManifestFileParser;
import org.eclipse.slm.service_management.features.service_deployment.impl.deployment.dockercontainer.DockerContainerServiceOfferingOrderUtil;
import org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances.ServiceInstanceEventMessageSender;
import org.eclipse.slm.service_management.features.service_deployment.impl.ServiceInstanceEventType;
import org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances.ServiceInstancesConsulClient;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions.ServiceOptionNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.codesys.CodesysDeploymentDefinition;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrder;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions.ServiceOfferingVersion;
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrderResult;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.docker.compose.DockerComposeDeploymentDefinition;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.docker.container.DockerContainerDeploymentDefinition;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.exceptions.InvalidServiceOfferingDefinitionException;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.util.*;

@Component
public class ServiceDeploymentHandler  extends AbstractServiceDeploymentHandler implements IAwxJobObserverListener {

    private final static Logger LOG = LoggerFactory.getLogger(ServiceDeploymentHandler.class);

    private final ServiceInstanceEventMessageSender serviceInstanceEventMessageSender;

    private final ConsulClientFactory consulClientFactory;
    private final ConsulClient consulAdminClient;

    private final KeycloakAdminClient keycloakAdminClient;

    private final ServiceOrderJpaRepository serviceOrderJpaRepository;

    private Map<AwxJobObserver, DeploymentJobRun> observedAwxJobsToDeploymentJobDetails = new HashMap<>();


    public ServiceDeploymentHandler(AwxJobObserverInitializer awxJobObserverInitializer,
                                    AwxJobExecutor awxJobExecutor,
                                    ConsulClientFactory consulClientFactory,
                                    KeycloakAdminClient keycloakAdminClient,
                                    ResourceManagementClientFactory resourceManagementClientFactory,
                                    ServiceOrderJpaRepository serviceOrderJpaRepository,
                                    ServiceInstancesConsulClient serviceInstancesConsulClient,
                                    ServiceInstanceEventMessageSender serviceInstanceEventMessageSender) {
        super(resourceManagementClientFactory, serviceInstancesConsulClient, awxJobObserverInitializer, awxJobExecutor);
        this.consulClientFactory = consulClientFactory;
        this.consulAdminClient = consulClientFactory.createAdminClient();
        this.keycloakAdminClient = keycloakAdminClient;
        this.serviceOrderJpaRepository = serviceOrderJpaRepository;
        this.serviceInstanceEventMessageSender = serviceInstanceEventMessageSender;
    }

    public DeploymentJobRun deployServiceOfferingToResource(
            JwtAuthenticationToken jwtAuthenticationToken,
            ServiceOfferingVersion serviceOfferingVersion,
            ServiceOrder serviceOrder)
            throws SSLException, JsonProcessingException, ServiceOptionNotFoundException, InvalidServiceOfferingDefinitionException, CapabilityServiceNotFoundException {

        var serviceId = UUID.randomUUID();
        serviceOrder.setServiceInstanceId(serviceId);
        var serviceOfferingDeploymentType = serviceOfferingVersion.getDeploymentType();
        var serviceHoster = this.getServiceHoster(jwtAuthenticationToken, serviceOrder.getDeploymentCapabilityServiceId());
        serviceOrder.setDeploymentCapabilityServiceId(serviceOrder.getDeploymentCapabilityServiceId());
        var awxCapabilityAction = this.getAwxDeployCapabilityAction(ActionType.DEPLOY, serviceHoster.getCapabilityService().getCapability());

        AwxJobObserver awxJobObserver;
        Map<String, String> serviceMetaData = new HashMap<>();
        List<Integer> servicePorts = new ArrayList<>();
        switch (serviceOfferingDeploymentType) {
            case DOCKER_CONTAINER:
            case DOCKER_COMPOSE: {
                var deployableComposeFile = this.getDeployableComposeFile(serviceOfferingVersion, serviceOrder);
                serviceMetaData = this.getServiceMetaData(serviceOfferingVersion, serviceOrder, deployableComposeFile);
                servicePorts = this.getServicePorts(serviceOfferingVersion, serviceOrder, deployableComposeFile);

                HashMap<String, Object> extraVarsMap = new HashMap<>() {{
                    put("service_id", serviceId);
                    put("keycloak_token", jwtAuthenticationToken.getToken().getTokenValue());
                    put("service_name", serviceHoster.getCapabilityService().getServiceName());
                    put("supported_connection_types", awxCapabilityAction.getConnectionTypes());
                    put("docker_compose_file", deployableComposeFile);
                }};
                extraVarsMap = this.addExtraVarsForServiceRepositories(extraVarsMap, serviceOfferingVersion);

                if (serviceHoster.getCapabilityService().getCapability().getName().toLowerCase().contains("transferapp")) {
                    Map<String, String> configYaml = new HashMap<>() {{
                        put("Name", serviceOfferingVersion.getServiceOffering().getName());
                        put("Description", serviceOfferingVersion.getServiceOffering().getShortDescription());
                        put("Version", serviceOfferingVersion.getVersion());
                    }};
                    extraVarsMap.put("config_yml", configYaml);
                }
                var extraVars = new ExtraVars(extraVarsMap);

                awxJobObserver = this.runAwxCapabilityAction(awxCapabilityAction, jwtAuthenticationToken, extraVars, JobGoal.CREATE, this);
                break;
            }
            case KUBERNETES: {
                KubernetesManifestFile deployableManifestFile = this.getDeployableManifestFile(serviceOfferingVersion, serviceOrder);

                HashMap<String, Object> extraVarsMap = new HashMap<>() {{
                    put("resource_id", serviceOrder.getDeploymentCapabilityServiceId());
                    put("service_id", serviceId);
                    put("keycloak_token", jwtAuthenticationToken.getToken().getTokenValue());
                    put("service_name", serviceHoster.getCapabilityService().getServiceName());
                    put("supported_connection_types", awxCapabilityAction.getConnectionTypes());
                    put("manifest_file", KubernetesManifestFileParser.manifestFinalizer(deployableManifestFile));
                }};

                extraVarsMap = this.addExtraVarsForServiceRepositories(extraVarsMap, serviceOfferingVersion);

                awxJobObserver = this.runAwxCapabilityAction(awxCapabilityAction, jwtAuthenticationToken, new ExtraVars(extraVarsMap), JobGoal.CREATE, this);
                break;
            }

            case CODESYS:{
                var codesysDeploymentDefinition = (CodesysDeploymentDefinition)serviceOfferingVersion.getDeploymentDefinition();
                HashMap<String, Object> extraVarsMap = new HashMap<>() {{
                    put("service_id", serviceId);
                    put("keycloak_token", jwtAuthenticationToken.getToken().getTokenValue());
                    put("service_name", serviceHoster.getCapabilityService().getServiceName());
                    put("supported_connection_types", awxCapabilityAction.getConnectionTypes());
                    put("application_path", codesysDeploymentDefinition.getApplicationPath());
                }};

                extraVarsMap = this.addExtraVarsForServiceRepositories(extraVarsMap, serviceOfferingVersion);

                awxJobObserver = this.runAwxCapabilityAction(awxCapabilityAction, jwtAuthenticationToken, new ExtraVars(extraVarsMap), JobGoal.CREATE, this);
            }
            break;
            default:
                throw new NotImplementedException("Deployment Type '" + serviceOfferingDeploymentType + "' not supported");
        }

        UUID resourceId;
        if (serviceHoster.getCapabilityService() instanceof SingleHostCapabilityService) {
            resourceId = ((SingleHostCapabilityService)serviceHoster.getCapabilityService()).getResourceId();
        }
        else {
            resourceId = serviceHoster.getCapabilityService().getResourceId();
        }
        var serviceInstance = new ServiceInstance(
                serviceId,
                new ArrayList<>(),
                serviceMetaData,
                resourceId,
                serviceHoster.getCapabilityService().getServiceId(),
                serviceOfferingVersion.getServiceOffering().getId(),
                serviceOfferingVersion.getId(),
                servicePorts,
                new ArrayList<>()
        );

        var deploymentJobRun = new DeploymentJobRun(awxJobObserver, jwtAuthenticationToken, serviceInstance, serviceOrder);
        this.observedAwxJobsToDeploymentJobDetails.put(awxJobObserver, deploymentJobRun);

        return deploymentJobRun;
    }

    private Map<String, String> getServiceMetaData(ServiceOfferingVersion serviceOfferingVersion, ServiceOrder serviceOrder,
                                                   DockerComposeFile deployableComposeFile) {
        var serviceMetaData = new HashMap<String, String>();
        switch (serviceOfferingVersion.getDeploymentType()) {
            case DOCKER_CONTAINER:
                var dockerContainerServiceOffering = (DockerContainerDeploymentDefinition) serviceOfferingVersion.getDeploymentDefinition();
                serviceMetaData = DockerContainerServiceOfferingOrderUtil
                        .getServiceMetaData(dockerContainerServiceOffering);
                break;

            case DOCKER_COMPOSE:
                var dockerComposeServiceOffering = (DockerComposeDeploymentDefinition) serviceOfferingVersion.getDeploymentDefinition();
                serviceMetaData = DockerComposeFileParser.getServiceMetaData(deployableComposeFile);
                break;

            case KUBERNETES:
                break;

            default:
                throw new NotImplementedException("Deployment Type '" + serviceOfferingVersion.getDeploymentType() + "' not supported");
        }

        return serviceMetaData;
    }

    private List<Integer> getServicePorts(ServiceOfferingVersion serviceOfferingVersion, ServiceOrder serviceOrder,
                                                   DockerComposeFile deployableComposeFile) {
        List<Integer> servicePorts = new ArrayList<Integer>();
        switch (serviceOfferingVersion.getDeploymentType()) {
            case DOCKER_CONTAINER:
            case DOCKER_COMPOSE:
                servicePorts = DockerComposeFileParser.getServicePorts(deployableComposeFile);
                break;

            case KUBERNETES:
                break;

            default:
                throw new NotImplementedException("Deployment Type '" + serviceOfferingVersion.getDeploymentType() + "' not supported");
        }

        servicePorts.addAll(serviceOfferingVersion.getServicePorts());

        return servicePorts;
    }

    private HashMap<String, Object> addExtraVarsForServiceRepositories(HashMap<String, Object> extraVarsMap, ServiceOfferingVersion serviceOfferingVersion) {
        if (serviceOfferingVersion.getServiceRepositories().size() > 0) {
            var dockerRegistriesVaultPaths = new ArrayList<String>();
            for (var serviceRepositoryId : serviceOfferingVersion.getServiceRepositories()) {
                dockerRegistriesVaultPaths.add("vendor_" + serviceOfferingVersion.getServiceOffering().getServiceVendor().getId() + "/" + serviceRepositoryId);
            }
            extraVarsMap.put("docker_registries_vault_paths", dockerRegistriesVaultPaths);
        }

        return extraVarsMap;
    }

    private DockerComposeFile getDeployableComposeFile(ServiceOfferingVersion serviceOfferingVersion, ServiceOrder serviceOrder)
            throws JsonProcessingException, ServiceOptionNotFoundException, InvalidServiceOfferingDefinitionException {
        DockerComposeFile deployableComposeFile = null;
        switch (serviceOfferingVersion.getDeploymentType())
        {
            case DOCKER_CONTAINER:
                deployableComposeFile = DockerContainerServiceOfferingOrderUtil
                        .generateDockerComposeFile(serviceOfferingVersion, serviceOrder);
                break;

            case DOCKER_COMPOSE:
                deployableComposeFile = DockerComposeFileParser.generateDeployableComposeFileForServiceOffering(
                        serviceOfferingVersion, serviceOrder.getServiceOptionValues());
                break;
        }

        if (deployableComposeFile == null) {
            throw new RuntimeException("Unable to create deployable Docker Compose File for order of service offering '"
                    + serviceOfferingVersion.getId() + "' version '" + serviceOfferingVersion.getVersion() + "'");
        }
        else {
            return deployableComposeFile;
        }
    }


    private KubernetesManifestFile getDeployableManifestFile(ServiceOfferingVersion serviceOfferingVersion, ServiceOrder serviceOrder)
            throws InvalidServiceOfferingDefinitionException {
        KubernetesManifestFile deployableManifestFile = null;
        switch (serviceOfferingVersion.getDeploymentType())
        {
            case KUBERNETES:
                deployableManifestFile = KubernetesManifestFileParser.generateDeployableManifestFileForServiceOffering(
                        serviceOfferingVersion, serviceOrder.getServiceOptionValues()
                );
                break;
        }

        if (deployableManifestFile == null) {
            throw new RuntimeException("Unable to create deployable Docker Compose File for order of service offering '"
                    + serviceOfferingVersion.getId() + "' version '" + serviceOfferingVersion.getVersion() + "'");
        }
        else {
            return deployableManifestFile;
        }
    }


    @Override
    public void onJobStateChanged(AwxJobObserver sender, JobState newState) {
    }

    @Override
    public void onJobStateFinished(AwxJobObserver sender, JobFinalState finalState) {
        if (this.observedAwxJobsToDeploymentJobDetails.containsKey(sender))
        {
            var jobDetails = this.observedAwxJobsToDeploymentJobDetails.get(sender);
            var jwtAuthenticationToken = jobDetails.getJwtAuthenticationToken();
            var userUuid = KeycloakTokenUtil.getUserUuid(jwtAuthenticationToken);
            var serviceInstance = jobDetails.getServiceInstance();
            var serviceOrder = jobDetails.getServiceOrder();
            // TODO: Get full path owner group id from REST call that initiated the service deployment instead of reconstructing / assuming it here
            var fullPathOwnerGroupId = "/users/" + userUuid;

            switch (finalState) {
                case SUCCESSFUL -> {
                    LOG.info("Service '" + serviceInstance.getId() + "' deployed for user '" + userUuid + "'");

                    // Add role for new service instance in Keycloak
                    var serviceKeycloakRoleName = "service_" + serviceInstance.getId();
                    var userId = jwtAuthenticationToken.getToken().getSubject();
                    this.keycloakAdminClient.createRealmRoleAndAssignToUser(userId, serviceKeycloakRoleName);

                    // Add consul service for new service instance
                    this.serviceInstancesConsulClient.registerConsulServiceForServiceInstance(serviceInstance, fullPathOwnerGroupId);

                    serviceOrder.setServiceOrderResult(ServiceOrderResult.SUCCESSFULL);
                    this.serviceInstanceEventMessageSender.sendMessage(serviceInstance, ServiceInstanceEventType.CREATED);
                }

                default -> {
                    serviceOrder.setServiceOrderResult(ServiceOrderResult.FAILED);
                    LOG.info("Service not deployed for user '" + userUuid + "', because job '" + sender.jobId +"' " + finalState);
                }
            }

            this.serviceOrderJpaRepository.save(serviceOrder);
            this.observedAwxJobsToDeploymentJobDetails.remove(sender);
        }
    }
}

