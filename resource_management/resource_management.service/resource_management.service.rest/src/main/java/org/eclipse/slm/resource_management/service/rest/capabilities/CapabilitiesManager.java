package org.eclipse.slm.resource_management.service.rest.capabilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.common.awx.client.AwxClient;
import org.eclipse.slm.common.awx.client.AwxCredential;
import org.eclipse.slm.common.awx.client.AwxProjectUpdateFailedException;
import org.eclipse.slm.common.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.common.awx.client.observer.AwxJobObserverInitializer;
import org.eclipse.slm.common.awx.client.observer.IAwxJobObserverListener;
import org.eclipse.slm.common.awx.model.ExtraVars;
import org.eclipse.slm.common.awx.model.JobTemplate;
import org.eclipse.slm.common.awx.model.Survey;
import org.eclipse.slm.common.awx.model.SurveyItem;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakUtil;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.notification_service.model.*;
import org.eclipse.slm.notification_service.service.client.NotificationServiceClient;
import org.eclipse.slm.resource_management.model.capabilities.*;
import org.eclipse.slm.resource_management.model.actions.AwxAction;
import org.eclipse.slm.resource_management.model.actions.ActionType;
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityService;
import org.eclipse.slm.resource_management.model.consul.capability.MultiHostCapabilityService;
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.model.consul.capability.SingleHostCapabilityService;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.persistence.api.CapabilityJpaRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.eclipse.slm.notification_service.model.JobGoal.ADD;
import static org.eclipse.slm.notification_service.model.JobGoal.DELETE;

@Component
public class CapabilitiesManager implements IAwxJobObserverListener {
    private final static Logger LOG = LoggerFactory.getLogger(CapabilitiesManager.class);
    private final CapabilitiesConsulClient capabilitiesConsulClient;
    private final SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient;
    private final AwxJobExecutor awxJobExecutor;
    private final NotificationServiceClient notificationServiceClient;
    private final AwxJobObserverInitializer awxJobObserverInitializer;
    private final AwxClient awxClient;
    private final CapabilityJpaRepository capabilityJpaRepository;
    private final MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient;
    private final SingleHostCapabilitiesVaultClient singleHostCapabilitiesVaultClient;
    private final KeycloakUtil keycloakUtil;
    private final CapabilityUtil capabilityUtil;
    private final ObjectMapper objectMapper;

    private final List<String> jobTemplateCredentialNames = List.of("Consul", "HashiCorp Vault", "Minio");
    public Map<AwxJobObserver, CapabilityJob> awxJobObserverToJobDetails = new HashMap<AwxJobObserver, CapabilityJob>();

    @Value("${awx.default-execution-environment}")
    private String defaultExecutionEnvironment;

    @Autowired
    public CapabilitiesManager(
        CapabilitiesConsulClient capabilitiesConsulClient,
        SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient,
        MultiHostCapabilitiesConsulClient multiHostCapabilitiesConsulClient,
        SingleHostCapabilitiesVaultClient singleHostCapabilitiesVaultClient,
        AwxJobExecutor awxJobExecutor,
        NotificationServiceClient notificationServiceClient,
        AwxJobObserverInitializer awxJobObserverInitializer,
        AwxClient awxClient,
        CapabilityJpaRepository capabilityJpaRepository,
        KeycloakUtil keycloakUtil,
        CapabilityUtil capabilityUtil,
        ObjectMapper objectMapper
    ) {
        this.singleHostCapabilitiesConsulClient = singleHostCapabilitiesConsulClient;
        this.multiHostCapabilitiesConsulClient = multiHostCapabilitiesConsulClient;
        this.capabilitiesConsulClient = capabilitiesConsulClient;
        this.singleHostCapabilitiesVaultClient = singleHostCapabilitiesVaultClient;
        this.awxJobExecutor = awxJobExecutor;
        this.notificationServiceClient = notificationServiceClient;
        this.awxJobObserverInitializer = awxJobObserverInitializer;
        this.awxClient = awxClient;
        this.capabilityJpaRepository = capabilityJpaRepository;
        this.keycloakUtil = keycloakUtil;
        this.capabilityUtil = capabilityUtil;
        this.objectMapper = objectMapper;
    }

    public List<Capability> getCapabilities() {
        return capabilityJpaRepository.findAll();
    }

    public List<Capability> getCapabilities(Optional<CapabilityFilter> capabilityFilter) {
        List<Capability> capabilitiesList = this.getCapabilities();

        if (capabilityFilter.isPresent()) {
            if (capabilityFilter.get().getCapabilityId() != null) {
                capabilitiesList = capabilitiesList.stream()
                        .filter(c -> c.getId().equals(capabilityFilter.get().getCapabilityId()))
                        .collect(Collectors.toList());
            }

            if (capabilityFilter.get().getCapabilityHostType() != null) {
                Predicate<Capability> filterCapabilities =
                        capabilityFilter.get().getCapabilityHostType().equals(CapabilityFilter.CapabilityHostType.SINGLE_HOST) ?
                                c -> c.getClusterMemberTypes().size() == 0 :
                                c -> c.getClusterMemberTypes().size() > 0;

                capabilitiesList = capabilitiesList
                        .stream()
                        .filter(filterCapabilities)
                        .collect(Collectors.toList());
            }
        }

        return capabilitiesList;
    }

    public Optional<Capability> getCapabilityByName(String name) {
        return getCapabilities().stream()
                .filter(c -> c.getName().equals(name)).findFirst();
    }

    public Optional<Capability> getCapabilityById(UUID uuid) {
        return getCapabilities().stream()
                .filter(c -> c.getId().equals(uuid)).findFirst();
    }

    public void addCapability(Capability capability)
            throws ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
        capability.getActions().forEach((capabilityActionType, capabilityAction) -> {
                    if(capabilityAction.getActionClass().equals(AwxAction.class.getSimpleName())) {
                        AwxAction awxCapabilityAction = (AwxAction) capabilityAction;
                        try {
                            var jobTemplateCredentialNames = this.jobTemplateCredentialNames;
                            JobTemplate jobTemplate;
                            if(!awxCapabilityAction.getUsername().equals("") && !awxCapabilityAction.getPassword().equals("")) {
                                jobTemplate = awxClient.createJobTemplateAddExecuteRoleToDefaultTeamAddScmCredential(
                                        awxCapabilityAction.getAwxRepo(),
                                        awxCapabilityAction.getAwxBranch(),
                                        awxCapabilityAction.getPlaybook(),
                                        awxCapabilityAction.getUsername(),
                                        awxCapabilityAction.getPassword(),
                                        jobTemplateCredentialNames,
                                        defaultExecutionEnvironment
                                );
                            } else {
                                jobTemplate = awxClient.createJobTemplateAndAddExecuteRoleToDefaultTeam(
                                        awxCapabilityAction.getAwxRepo(),
                                        awxCapabilityAction.getAwxBranch(),
                                        awxCapabilityAction.getPlaybook(),
                                        jobTemplateCredentialNames,
                                        defaultExecutionEnvironment);
                            }

                            List<SurveyItem> params = awxCapabilityAction.getParameter();
                            if(params != null) {
                                awxClient.createSurvey(
                                        jobTemplate.getId(),
                                        new Survey(
                                            "Survey",
                                        "Survey for "+jobTemplate.getName(),
                                                params
                                        )
                                );
                                awxClient.enableSurvey(jobTemplate.getId());
                            }
                        } catch (AwxProjectUpdateFailedException | SSLException | JsonProcessingException e) {
                            LOG.error("Failed to clone AWX Project from " + awxCapabilityAction.getAwxRepo() + " - " + awxCapabilityAction.getAwxBranch());
                        }
                    }
                });

        capabilityJpaRepository.save(capability);

        if (capability.getHealthCheck() != null) {
            singleHostCapabilitiesConsulClient.addSingleHostCapabilityWithHealthCheckToAllConsulNodes(
                    new ConsulCredential(),
                    capability,
                    false,
                    new HashMap<>()
            );
        }
    }

    public boolean deleteCapability(UUID capabilityId) throws ConsulLoginFailedException {
        var capabilityOptional = this.capabilityJpaRepository.findById(capabilityId);
        if (capabilityOptional.isPresent()) {
            deleteCapability(capabilityOptional.get());
            return true;
        }
        else {
            return false;
        }
    }

    private void deleteCapability(Capability capability)
            throws ConsulLoginFailedException {
        this.singleHostCapabilitiesConsulClient.removeCapabilityServiceFromAllConsulNodes(
                new ConsulCredential(),
                capability
        );
        this.capabilityJpaRepository.delete(capability);

        capability.getActions().forEach((capabilityActionType, capabilityAction) -> {
            if (capabilityAction.getActionClass().equals(AwxAction.class.getSimpleName())) {
                AwxAction awxCapabilityAction = (AwxAction) capabilityAction;
                awxClient.deleteJobTemplate(
                        awxCapabilityAction.getAwxRepo(),
                        awxCapabilityAction.getAwxBranch(),
                        awxCapabilityAction.getPlaybook()
                );

                awxClient.deleteProject(
                    awxCapabilityAction.getAwxRepo(),
                    awxCapabilityAction.getAwxBranch()
                );
            }
        });
        LOG.info("Capability with uuid '" + capability.getId()  + "' deleted");
    }

    public List<Capability> getSingleHostCapabilitiesOfResource(
            UUID resourceId,
            ConsulCredential consulCredential
    ) throws ConsulLoginFailedException, ResourceNotFoundException {
        return this.capabilitiesConsulClient.getInstalledSingleHostCapabilitiesOfResource(
                consulCredential,
                resourceId
        );
    }

    public List<DeploymentCapability> getDeploymentCapabilitiesOfResource(
            UUID resourceId,
            ConsulCredential consulCredential
    ) throws ResourceNotFoundException, ConsulLoginFailedException {
        return this.capabilitiesConsulClient.getInstalledDeploymentCapabilitiesOfResource(
                consulCredential,
                resourceId
        );
    }

    public List<VirtualizationCapability> getVirtualizationCapabilitiesOfResource(
            UUID resourceId,
            ConsulCredential consulCredential
    ) throws ConsulLoginFailedException, ResourceNotFoundException {
        return this.capabilitiesConsulClient.getInstalledVirtualizationCapabilitiesOfResource(
                consulCredential,
                resourceId
        );
    }

    public Optional<Capability> getSingleHostCapabilityOfResourceById(
            UUID resourceId,
            UUID capabilityId,
            ConsulCredential consulCredential
    ) throws ResourceNotFoundException, ConsulLoginFailedException {
//        List<DeploymentCapability> deploymentCapabilitiesOfResource = getDeploymentCapabilitiesOfResource(resourceId, consulCredential);
        List<Capability> capabilitiesOfResource = getSingleHostCapabilitiesOfResource(
                resourceId,
                consulCredential
        );

        return capabilitiesOfResource.stream().filter(cap -> cap.getId().equals(capabilityId)).findFirst();
    }

    public List<SingleHostCapabilityService> getSingleHostCapabilityServicesOfResourceById(
            UUID resourceId,
            ConsulCredential consulCredential
    ) throws ConsulLoginFailedException {
        return singleHostCapabilitiesConsulClient.getSingleHostCapabilityServicesOfResource(
                consulCredential,
                resourceId
        );
    }

    public List<MultiHostCapabilityService> getMultiHostCapabilityServicesOfResourceById(
            UUID resourceId,
            ConsulCredential consulCredential
    ) throws ConsulLoginFailedException {
        return multiHostCapabilitiesConsulClient.getMultiHostCapabilityServicesOfResource(
                consulCredential,
                resourceId
        );
    }

    public List<CapabilityService> getCapabilityServicesOfResourceById(
            UUID resourceId,
            ConsulCredential consulCredential
    ) throws ConsulLoginFailedException {
        List<SingleHostCapabilityService> shcs =
                getSingleHostCapabilityServicesOfResourceById(resourceId, consulCredential);

        List<MultiHostCapabilityService> mhcs =
                getMultiHostCapabilityServicesOfResourceById(resourceId, consulCredential);

        List<CapabilityService> capabilityServices = new ArrayList<>();

        capabilityServices.addAll(shcs);
        capabilityServices.addAll(mhcs);

        return capabilityServices;
    }


    public Optional<DeploymentCapability> getDeploymentCapabilityOfResourceById(
            UUID resourceId,
            UUID capabilityId,
            ConsulCredential consulCredential
    ) throws ResourceNotFoundException, ConsulLoginFailedException {
        List<DeploymentCapability> deploymentCapabilitiesOfResource = getDeploymentCapabilitiesOfResource(resourceId, consulCredential);

        return deploymentCapabilitiesOfResource.stream().filter(cap -> cap.getId().equals(capabilityId)).findFirst();
    }

    public int installCapabilityOnResource(
            UUID resourceId,
            CapabilityService newCapabilityService,
            AwxCredential awxCredential,
            ConsulCredential consulCredential,
            String keycloakToken,
            Map<String,String> configParameters
    ) throws SSLException, CapabilityNotFoundException, ConsulLoginFailedException, ResourceNotFoundException, JsonProcessingException {
        UUID capabilityId = newCapabilityService.getCapability().getId();
        if (newCapabilityService.getCapability().getActions().containsKey(ActionType.INSTALL)) {
            var capabilityInstallAction = newCapabilityService.getCapability().getActions().get(ActionType.INSTALL);
            if (capabilityInstallAction instanceof AwxAction) {
                var awxInstallCapabilityAction = (AwxAction) capabilityInstallAction;
                Map<String, String> secretConfigParams = capabilityUtil.getSecretConfigParameter(
                        newCapabilityService.getCapability(),
                        configParameters
                );
                Map<String, String> nonSecretConfigParams = capabilityUtil.getNonSecretConfigParameter(
                        newCapabilityService.getCapability(),
                        configParameters
                );

                Map<String, Object> extraVars = new HashMap<>();
                extraVars.put("keycloak_token", keycloakToken);
                extraVars.put("resource_id", resourceId.toString());
                extraVars.put("service_name", newCapabilityService.getService());
                extraVars.put("supported_connection_types", capabilityInstallAction.getConnectionTypes());
                if(nonSecretConfigParams.size()>0)
                    extraVars.put("consul_service_meta", objectMapper.writeValueAsString(nonSecretConfigParams));
                if(secretConfigParams.size()>0)
                    extraVars.put("vault_service_secrets", objectMapper.writeValueAsString(secretConfigParams));

                int awxJobId = awxJobExecutor.executeJob(
                        awxCredential,
                        awxInstallCapabilityAction.getAwxRepo(),
                        awxInstallCapabilityAction.getAwxBranch(),
                        awxInstallCapabilityAction.getPlaybook(),
                        new ExtraVars(extraVars)
                );
                var awxJobObserver = this.awxJobObserverInitializer.init(
                        awxJobId, JobTarget.DEPLOYMENT_CAPABILITY, ADD, this
                );
                this.awxJobObserverToJobDetails.put(
                        awxJobObserver,
                        new CapabilityJob(
                                resourceId,
                                awxCredential,
                                consulCredential,
                                newCapabilityService
                        )
                );
                return awxJobId;
            } else {
                throw new NotImplementedException("Capability action type [type='"
                        + capabilityInstallAction.getClass() + "'] not implemented");
            }
        } else {
            throw new RuntimeException("Capability [id='" + capabilityId + "'] has no INSTALL action");
        }
    }

    public void installCapabilityOnResource(
            UUID resourceId,
            UUID capabilityId,
            Boolean skipInstall,
            Map<String, String> configParameters,
            KeycloakPrincipal keycloakPrincipal
    ) throws SSLException, ConsulLoginFailedException, ResourceNotFoundException, CapabilityNotFoundException, JsonProcessingException, IllegalAccessException {
        var deploymentCapabilitiesOfResource = getDeploymentCapabilitiesOfResource(
                resourceId,
                new ConsulCredential(keycloakPrincipal)
        );
        var optionalExistingDCOnResource = deploymentCapabilitiesOfResource
                .stream()
                .filter(
                        dc -> dc.getId().equals(capabilityId))
                .findAny();
        if (optionalExistingDCOnResource.isPresent()) {
            LOG.info("Deployment capability [id='" + optionalExistingDCOnResource.get().getId() + "', name='"
                    + optionalExistingDCOnResource.get().getName() +"'] already installed on resource [id='" + resourceId + "']");
            return;
        }

        Optional<Capability> capabilityOptional = this.getCapabilityById(capabilityId);
        if (capabilityOptional.isEmpty()) {
            LOG.error("Failed to install capability [id='" + capabilityId + "'] on resource [id='" +resourceId + "'], " +
                    "because capability not found");
            throw new CapabilityNotFoundException(capabilityId);
        }
        Capability capability = capabilityOptional.get();
        CapabilityServiceStatus capabilityServiceStatus;

        if(!skipInstall) {
            capabilityServiceStatus = CapabilityServiceStatus.INSTALL;
        } else {
            capabilityServiceStatus = CapabilityServiceStatus.READY;
        }

        CapabilityService newCapabilityService = singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                new ConsulCredential(),
                capability,
                resourceId,
                capabilityServiceStatus,
                skipInstall,
                configParameters
        );

        this.notificationServiceClient.postNotification(keycloakPrincipal, Category.Resources, JobTarget.DEPLOYMENT_CAPABILITY, ADD);

        this.keycloakUtil.createRealmRoleAndAssignToUser(
                keycloakPrincipal,
                singleHostCapabilitiesVaultClient.addSingleHostCapabilityServiceSecrets(
                        new VaultCredential(),
                        newCapabilityService,
                        resourceId,
                        configParameters
                )
        );

        int awxJobId;

        if(!skipInstall) {
            awxJobId = installCapabilityOnResource(
                    resourceId,
                    newCapabilityService,
                    new AwxCredential(keycloakPrincipal),
                    new ConsulCredential(keycloakPrincipal),
                    keycloakPrincipal.getKeycloakSecurityContext().getTokenString(),
                    configParameters
            );
            this.notificationServiceClient.postJobObserver(keycloakPrincipal, awxJobId, JobTarget.DEPLOYMENT_CAPABILITY, ADD);
        }
    }

    public void installBaseConfigurationOnResource(
            KeycloakPrincipal keycloakPrincipal,
            UUID resourceId,
            UUID capabilityId
    ) throws CapabilityNotFoundException, ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException, SSLException, JsonProcessingException {


        Optional<Capability> capabilityOptional = this.getCapabilityById(capabilityId);
        if (capabilityOptional.isEmpty()) {
            LOG.error("Failed to install capability [id='" + capabilityId + "'] on resource [id='" +resourceId + "'], " +
                    "because capability not found");
            throw new CapabilityNotFoundException(capabilityId);
        }
        Capability capability = capabilityOptional.get();

        CapabilityService newCapabilityService = singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                new ConsulCredential(),
                capability,
                resourceId,
                CapabilityServiceStatus.INSTALL
        );

        int awxJobId = installCapabilityOnResource(
                resourceId,
                newCapabilityService,
                new AwxCredential(keycloakPrincipal),
                new ConsulCredential(),
                keycloakPrincipal.getKeycloakSecurityContext().getTokenString(),
                new HashMap<>()

        );
        this.notificationServiceClient.postJobObserver(keycloakPrincipal, awxJobId, JobTarget.BASE_CONFIGURATION_CAPABILITY, ADD);
    }

    private void updateCapabilityServiceStatus(
            UUID resourceId,
            CapabilityService capabilityService,
            CapabilityServiceStatus newCapabilityServiceStatus
    ) throws ConsulLoginFailedException {
        capabilityService.setStatus(newCapabilityServiceStatus);

        singleHostCapabilitiesConsulClient.updateCapabilityService(
                new ConsulCredential(),
                resourceId,
                capabilityService
        );
    }

    public int uninstallCapabilityFromResource(
            UUID resourceId,
            CapabilityService capabilityService,
            AwxCredential awxCredential,
            ConsulCredential consulCredential,
            String keycloakToken
    ) throws SSLException, ConsulLoginFailedException, ResourceNotFoundException {
        Capability capability = capabilityService.getCapability();

        var uninstallAction = (AwxAction) capability.getActions().get(ActionType.UNINSTALL);
        Map<String, Object> extraVars = new HashMap<>();
        extraVars.put("keycloak_token", keycloakToken);
        extraVars.put("resource_id", resourceId.toString());
        extraVars.put("service_name", capabilityService.getService());
        extraVars.put("supported_connection_types", uninstallAction.getConnectionTypes());

        int awxJobId = awxJobExecutor.executeJob(
                awxCredential,
                uninstallAction.getAwxRepo(),
                uninstallAction.getAwxBranch(),
                uninstallAction.getPlaybook(),
                new ExtraVars(extraVars)
        );

        capabilityService.setStatus(CapabilityServiceStatus.UNINSTALL);
        singleHostCapabilitiesConsulClient.updateCapabilityService(
                new ConsulCredential(),
                resourceId,
                capabilityService
        );

        var awxJobObserver = this.awxJobObserverInitializer.init(
                awxJobId,
                JobTarget.DEPLOYMENT_CAPABILITY,
                DELETE,
                this
        );
        this.awxJobObserverToJobDetails.put(
                awxJobObserver,
                new CapabilityJob(
                        resourceId,
                        awxCredential,
                        consulCredential,
                        capabilityService
                )
        );

        return awxJobId;
    }

    public void uninstallCapabilityFromResource(
            UUID resourceId,
            UUID capabilityId,
            KeycloakPrincipal keycloakPrincipal
    ) throws SSLException, ConsulLoginFailedException, ResourceNotFoundException, IllegalAccessException {
        ConsulCredential consulCredential = new ConsulCredential(keycloakPrincipal);
        Optional<Capability> capabilityOptional = null;
        try {
            capabilityOptional = this.getSingleHostCapabilityOfResourceById(
                    resourceId,
                    capabilityId,
                    consulCredential
            );
        } catch (ResourceNotFoundException | ConsulLoginFailedException e) {
            LOG.info("Resource with UUID = " + resourceId + " does not exist => Skip removal of capability");
            return;
        }

        CapabilityService capabilityService = null;

        if(capabilityOptional.isEmpty()) {
            LOG.info("Resource with UUID = "+resourceId+" has no capability with UUID = " + capabilityId + " => Skip removal of capability");
            return;
        } else {
            capabilityService = singleHostCapabilitiesConsulClient.getCapabilityServiceOfResourceByCapabilityId(
              consulCredential,
              capabilityId,
              resourceId
            );
        }

        if(capabilityService == null) {
            LOG.info("Resource with UUID = "+resourceId+" has no CapabilityService => Skip removal of capability");
            return;
        }

        if(!capabilityService.getManaged()) {
            int awxJobId = uninstallCapabilityFromResource(
                    resourceId,
                    capabilityService,
                    new AwxCredential(keycloakPrincipal),
                    consulCredential,
                    keycloakPrincipal.getKeycloakSecurityContext().getTokenString()
            );

            if (awxJobId != -1)
                this.notificationServiceClient.postJobObserver(
                        keycloakPrincipal,
                        awxJobId,
                        JobTarget.DEPLOYMENT_CAPABILITY,
                        DELETE
                );
        } else {
            singleHostCapabilitiesConsulClient.removeSingleHostCapabilityFromNode(
                    consulCredential,
                    capabilityOptional.get(),
                    resourceId
            );
            //TODO Does not work? keycloakPrincipal has not proper rights?
            this.keycloakUtil.deleteRealmRoleAsAdmin(
                    keycloakPrincipal,
                    singleHostCapabilitiesVaultClient.deleteSingleHostCapabilityServiceSecrets(
                            new VaultCredential(),
                            capabilityService
                    )
            );
            this.notificationServiceClient.postNotification(
                    keycloakPrincipal,
                    Category.Resources,
                    JobTarget.DEPLOYMENT_CAPABILITY,
                    DELETE
            );
        }
    }

    @Override
    public void onJobStateChanged(AwxJobObserver sender, JobState newState) {
    }

    @Override
    public void onJobStateFinished(
            AwxJobObserver awxJobObserver,
            JobFinalState finalState
    ) {

        JobGoal jobGoal = awxJobObserver.jobGoal;
        CapabilityJob jobDetails = this.awxJobObserverToJobDetails.get(awxJobObserver);
        CapabilityService capabilityService = jobDetails.getCapabilityService();
        Capability capability = capabilityService.getCapability();

        if (finalState == JobFinalState.SUCCESSFUL)
        {
            try
            {
                switch (jobGoal)
                {
                    case ADD:
                        if (capability.getHealthCheck() == null) {
//                            singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
//                                    new ConsulCredential(),
//                                    capability,
//                                    jobDetails.getResourceId()
//                            );
                        }

                        updateCapabilityServiceStatus(
                                jobDetails.getResourceId(),
                                capabilityService,
                                CapabilityServiceStatus.READY
                        );

                        LOG.info("Successfully added capability '" + capability.getName() + "'" +
                                "to resource '" + jobDetails.getResourceId() + "'");
                        break;

                    case DELETE:
                        singleHostCapabilitiesVaultClient.deleteSingleHostCapabilityServiceSecrets(
                                new VaultCredential(),
                                capabilityService
                        );

                        if (capabilityService.getCapability().getHealthCheck() == null) {
                            singleHostCapabilitiesConsulClient.removeSingleHostCapabilityFromNode(
                                    jobDetails.consulCredential,
                                    capabilityService.getCapability(),
                                    jobDetails.getResourceId()
                            );
                        }

                        LOG.info("Successfully removed deployment capability '"  + capability.getName() +"'" +
                                "from resource '" + jobDetails.getResourceId() + "'");
                        break;

                    default:
                        LOG.error("Unsupported JobGoal '" + jobGoal + "'");
                        break;
                }
            } catch (ConsulLoginFailedException e)
            {
                LOG.error("Failed to login to Consul: " + e.getMessage());
            } catch (ResourceNotFoundException e) {
                LOG.warn("Resource [id = '"+jobDetails.getResourceId()+"'] not found => Skip removal of deployment capability [id = '"+capability.getId()+"']");
            }
        }
        else
        {
            try {
                updateCapabilityServiceStatus(
                        jobDetails.getResourceId(),
                        capabilityService,
                        CapabilityServiceStatus.FAILED
                );
            } catch (ConsulLoginFailedException e) {
                LOG.error("Failed to login to Consul: " + e.getMessage());
            }
            LOG.error("Failed to add deployment capability '"  + jobDetails.getCapabilityService().getCapability().getName() + "'" +
                    "to resource '" + jobDetails.getResourceId() + "'");
        }

        this.awxJobObserverToJobDetails.remove(awxJobObserver);
    }
}
