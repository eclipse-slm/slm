package org.eclipse.slm.resource_management.features.capabilities.jobs;

import jakarta.annotation.PostConstruct;
import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.model.exceptions.EventNotAcceptedException;
import org.eclipse.slm.common.vault.client.VaultCredential;
import org.eclipse.slm.resource_management.common.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.common.resources.ResourceDTO;
import org.eclipse.slm.resource_management.common.resources.ResourceEventInternalListener;
import org.eclipse.slm.resource_management.common.resources.ResourcesManager;
import org.eclipse.slm.resource_management.features.capabilities.CapabilitiesService;
import org.eclipse.slm.resource_management.features.capabilities.exceptions.CapabilityRuntimeException;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus;
import org.eclipse.slm.resource_management.features.capabilities.persistence.SingleHostCapabilitiesConsulClient;
import org.eclipse.slm.resource_management.features.capabilities.persistence.SingleHostCapabilitiesVaultClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class CapabilityJobServiceImpl implements CapabilityJobService, CapabilityJobStateMachineListener, CapabilityJobExecutorListener, ResourceEventInternalListener {

    private final static Logger LOG = LoggerFactory.getLogger(CapabilityJobServiceImpl.class);

    private final ResourcesManager resourcesManager;

    private final CapabilitiesService capabilitiesService;

    private final CapabilityJobExecutorFactory capabilityJobExecutorFactory;

    private final CapabilityJobJpaRepository capabilityJobJpaRepository;

    private final CapabilityJobStateMachineFactory capabilityJobStateMachineFactory;

    private final SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient;

    private final SingleHostCapabilitiesVaultClient singleHostCapabilitiesVaultClient;

    private final KeycloakAdminClient keycloakAdminClient;

    private final Map<UUID, JwtAuthenticationToken> capabilityJobIdToJwtAuthToken = new HashMap<>();

    public CapabilityJobServiceImpl(ResourcesManager resourcesManager,
                                    CapabilitiesService capabilitiesService,
                                    CapabilityJobExecutorFactory capabilityJobExecutorFactory,
                                    CapabilityJobJpaRepository capabilityJobJpaRepository,
                                    CapabilityJobStateMachineFactory capabilityJobStateMachineFactory,
                                    SingleHostCapabilitiesConsulClient singleHostCapabilitiesConsulClient,
                                    SingleHostCapabilitiesVaultClient singleHostCapabilitiesVaultClient,
                                    KeycloakAdminClient keycloakAdminClient) {
        this.resourcesManager = resourcesManager;
        this.capabilitiesService = capabilitiesService;
        this.capabilityJobExecutorFactory = capabilityJobExecutorFactory;
        this.capabilityJobJpaRepository = capabilityJobJpaRepository;
        this.capabilityJobStateMachineFactory = capabilityJobStateMachineFactory;
        this.singleHostCapabilitiesConsulClient = singleHostCapabilitiesConsulClient;
        this.singleHostCapabilitiesVaultClient = singleHostCapabilitiesVaultClient;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @PostConstruct
    public void init() {
        var allCapabilityJobs = this.capabilityJobJpaRepository.findAll();
        for (var capabilityJob : allCapabilityJobs) {
            StateMachine<CapabilityJobState, CapabilityJobEvent> capabilityJobStateMachine = null;
            try {
                capabilityJobStateMachine = this.capabilityJobStateMachineFactory.create(capabilityJob, this);
                if (!CapabilityJobState.getStableStates().contains(capabilityJobStateMachine.getState().getId())) {
                    LOG.info("Capability job with id: " + capabilityJob.getId() + " was in unstable state '" + capabilityJob.getState() + "' " +
                            "during system restart, setting it to FAILED");
                    capabilityJob.addLogMessage("Set job to FAILED state because it was in unstable state '" +  capabilityJob.getState() + "' during system restart");
                    capabilityJob = this.capabilityJobJpaRepository.save(capabilityJob);
                    this.changeStateOfCapabilityJobStateMachine(capabilityJob, capabilityJobStateMachine, CapabilityJobEvent.ERROR_OCCURRED);
                }
            } catch (Exception e) {
                LOG.error("Error while initializing capability job state machine for capability job with id: " + capabilityJob.getId(), e);
            }
        }
    }

    //region CapabilityJobService
    @Override
    public List<CapabilityJob> getCapabilityJobsOfResource(UUID resourceId) {
        var capabilityJobsOfResource = this.capabilityJobJpaRepository.findByResourceId(resourceId);
        return capabilityJobsOfResource;
    }

    @Override
    public void initCapabilityJob(JwtAuthenticationToken jwtAuthenticationToken, UUID resourceId, UUID capabilityId, boolean skipInstall, Map<String, String> configParameters, boolean force) throws Exception {
        try {
            // Check if resource exists
            var resource = this.resourcesManager.getResourceByIdOrThrow(resourceId);
            // Check if capability exists
            var capability = this.capabilitiesService.getCapabilityByIdOrThrow(capabilityId);
            // Check if capability is already installed on resource
            var isCapabilityInstalledOnResource = this.capabilitiesService.isCapabilityInstalledOnResource(resourceId, capabilityId);
            if (isCapabilityInstalledOnResource) {
                throw new CapabilityAlreadyInstalledException(resource.getId(), capability.getId(), capability.getName());
            }
            // Check if uncompleted capability job already exists for the same capability
            var matchingCapabilityJobsOfResource = this.capabilityJobJpaRepository.findByResourceIdAndCapabilityId(resourceId, capabilityId);
            for (var capabilityJob : matchingCapabilityJobsOfResource) {
                var capabilityJobStateMachine = this.capabilityJobStateMachineFactory.create(capabilityJob, this);
                if (!capabilityJobStateMachine.isComplete()) {
                    if (force) {
                        LOG.info("Capability job with id: " + capabilityJob.getId() + " was in uncompleted state '" + capabilityJob.getState() + "' " +
                                ", setting it to FAILED because init of new capability job was forced");
                        this.changeStateOfCapabilityJobStateMachine(capabilityJob, capabilityJobStateMachine, CapabilityJobEvent.ERROR_OCCURRED);
                        capabilityJob.addLogMessage("Set job to FAILED state because it was in uncompleted state '" +  capabilityJob.getState() + "' " +
                                "during system restart and init of new capability job was forced");
                        this.capabilityJobJpaRepository.save(capabilityJob);
                    }
                    else {
                        throw new CapabilityJobRuntimeException("Uncompleted capability job already exists for resource [id=" + resourceId + "] and capability [id=" + capabilityId + "]");
                    }
                }
            }
            // Create capability service
            var capabilityServiceStatus = CapabilityServiceStatus.INSTALL;
            if (skipInstall) {
                capabilityServiceStatus = CapabilityServiceStatus.READY;
            }
            var newCapabilityService = this.singleHostCapabilitiesConsulClient.addSingleHostCapabilityToNode(
                    new ConsulCredential(),
                    capability,
                    resourceId,
                    capabilityServiceStatus,
                    skipInstall,
                    configParameters
            );
            // Create Keycloak role for capability service and assign it to users of resource
            this.singleHostCapabilitiesVaultClient.addSingleHostCapabilityServiceSecrets(
                    new VaultCredential(),
                    newCapabilityService,
                    resourceId,
                    configParameters
            );
            // Create and store capability job
            var capabilityJob = new CapabilityJob(UUID.randomUUID(), resourceId, capabilityId, skipInstall);
            capabilityJob.setConfigParameters(configParameters);
            capabilityJob = this.capabilityJobJpaRepository.save(capabilityJob);
            // Create state machine for capability job and trigger transition
            var capabilityJobStateMachine = this.capabilityJobStateMachineFactory.create(capabilityJob, this);
            if (skipInstall) {
                this.changeStateOfCapabilityJobStateMachine(capabilityJob, capabilityJobStateMachine, CapabilityJobEvent.INSTALL_SKIPPED);
            } else {
                this.capabilityJobIdToJwtAuthToken.put(capabilityJob.getId(), jwtAuthenticationToken);
                this.changeStateOfCapabilityJobStateMachine(capabilityJob, capabilityJobStateMachine, CapabilityJobEvent.INSTALL_TRIGGERED);
            }
        }
        catch (Exception e) {
            LOG.error("Error while initializing capability job for resource [id=" + resourceId + "] and capability [id=" + capabilityId + "]", e);
            this.cleanupCapabilityServiceOfResource(resourceId, capabilityId);
            throw new CapabilityRuntimeException(capabilityId, "Error while initializing capability job for resource [id=" + resourceId +
                    "] and capability [id=" + capabilityId + "]: " + e.getMessage());
        }
    }

    @Override
    public void uninstallCapability(JwtAuthenticationToken jwtAuthenticationToken, UUID resourceId, UUID capabilityId) throws Exception {
        var matchingCapabilityJobsOfResource = this.capabilityJobJpaRepository.findByResourceIdAndCapabilityId(resourceId, capabilityId);
        CapabilityJob uncompletedCapabilityJob = null;
        for (var capabilityJob : matchingCapabilityJobsOfResource) {
            var capabilityJobStateMachine = this.capabilityJobStateMachineFactory.create(capabilityJob, this);
            if (!capabilityJobStateMachine.isComplete()) {
                uncompletedCapabilityJob = capabilityJob;
                break;
            }
        }

        if (uncompletedCapabilityJob == null) {
            throw new CapabilityJobRuntimeException("Can not uninstall capability because no uncompleted capability job found for resource [id=" +
                    resourceId + "] and capability [id=" + capabilityId + "]");
        }

        var capabilityJobStateMachine = this.capabilityJobStateMachineFactory.create(uncompletedCapabilityJob, this);
        if (uncompletedCapabilityJob.getSkipInstall()) {
            this.changeStateOfCapabilityJobStateMachine(uncompletedCapabilityJob, capabilityJobStateMachine, CapabilityJobEvent.UNINSTALL_SKIPPED);
        }
        else {
            this.capabilityJobIdToJwtAuthToken.put(uncompletedCapabilityJob.getId(), jwtAuthenticationToken);
            this.changeStateOfCapabilityJobStateMachine(uncompletedCapabilityJob, capabilityJobStateMachine, CapabilityJobEvent.UNINSTALL_TRIGGERED);
        }
    }
    //endregion CapabilityJobService

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

    private void handleInstallingStateEntry(CapabilityJob capabilityJob) throws Exception {
        try {
            var jwtAuthenticationToken = this.capabilityJobIdToJwtAuthToken.get(capabilityJob.getId());
            this.capabilityJobIdToJwtAuthToken.remove(capabilityJob.getId());
            var capabilityId = capabilityJob.getCapabilityId();
            var capability = this.capabilitiesService.getCapabilityByIdOrThrow(capabilityId);
            var capabilityService = this.singleHostCapabilitiesConsulClient.getCapabilityServiceOfResourceByCapabilityId(capabilityId, capabilityJob.getResourceId());

            var capabilityJobExecutor = this.capabilityJobExecutorFactory.create(capabilityJob, capabilityService, this);
            capabilityJobExecutor.installCapabilityOnResource(jwtAuthenticationToken, capabilityJob.getResourceId(), capabilityJob.getConfigParameters());
            LOG.info("Installation of capability [id= " + capabilityJob.getId() + "] on resource [id= " + capabilityJob.getResourceId() + "] started");
        } catch (Exception e) {
            LOG.error("Error while installing capability with id: " + capabilityJob.getId(), e);
            var capabilityJobStateMachine = this.capabilityJobStateMachineFactory.create(capabilityJob, this);
            this.changeStateOfCapabilityJobStateMachine(capabilityJob, capabilityJobStateMachine, CapabilityJobEvent.ERROR_OCCURRED);
        }
    }

    private void handleUninstallingStateEntry(CapabilityJob capabilityJob) throws Exception {
        try {
            var jwtAuthenticationToken = this.capabilityJobIdToJwtAuthToken.get(capabilityJob.getId());
            this.capabilityJobIdToJwtAuthToken.remove(capabilityJob.getId());
            var capabilityId = capabilityJob.getCapabilityId();
            var capability = this.capabilitiesService.getCapabilityByIdOrThrow(capabilityId);

            var capabilityService = this.singleHostCapabilitiesConsulClient.getCapabilityServiceOfResourceByCapabilityId(capabilityId, capabilityJob.getResourceId());
            // If capability service is managed, skip install and remove capability service from node
            if (capabilityService.getManaged()) {
                this.singleHostCapabilitiesConsulClient.removeSingleHostCapabilityFromNode(new ConsulCredential(), capability, capabilityJob.getResourceId());

                var keycloakRealmRoleName = singleHostCapabilitiesVaultClient.deleteSingleHostCapabilityServiceSecrets(new VaultCredential(), capabilityJob.getResourceId(), capabilityService);
                this.keycloakAdminClient.deleteRealmRole(keycloakRealmRoleName);
            }

            var capabilityJobExecutor = this.capabilityJobExecutorFactory.create(capabilityJob, capabilityService, this);
            capabilityJobExecutor.uninstallCapabilityFromResource(jwtAuthenticationToken, capabilityJob.getResourceId());

            LOG.info("Deinstallation of capability [id= " + capabilityJob.getId() + "] on resource [id= " + capabilityJob.getResourceId() + "] started");
        } catch (Exception e) {
            LOG.error("Error while installing capability with id: " + capabilityJob.getId(), e);
            var capabilityJobStateMachine = this.capabilityJobStateMachineFactory.create(capabilityJob, this);
            this.changeStateOfCapabilityJobStateMachine(capabilityJob, capabilityJobStateMachine, CapabilityJobEvent.ERROR_OCCURRED);
        }
    }

    private boolean changeStateOfCapabilityJobStateMachine(CapabilityJob capabilityJob,
                                                            StateMachine<CapabilityJobState, CapabilityJobEvent> capabilityJobStateMachine,
                                                            CapabilityJobEvent event) {
        Message<CapabilityJobEvent> message = MessageBuilder.withPayload(event)
                .setHeader("capabilityJob", capabilityJob)
                .build();

        var result = capabilityJobStateMachine.sendEvent(Mono.just(message)).blockFirst();

        if (result.getResultType().equals(StateMachineEventResult.ResultType.DENIED)) {
            var currentState = result.getRegion().getState().getId();
            throw new EventNotAcceptedException(currentState.toString(), event.toString());
        }

        return true;
    }


    private void handleUninstalledStateEntry(CapabilityJob capabilityJob) {
        try {
            if (!this.capabilitiesService.isCapabilityInstalledOnResource(capabilityJob.getResourceId(), capabilityJob.getCapabilityId())) {
                LOG.warn("Capability job [id=" + capabilityJob.getId() + "] is in state UNINSTALLED, but capability does not exist anymore, trying to cleanup anyway");
                this.cleanupCapabilityServiceOfResource(capabilityJob.getResourceId(), capabilityJob.getCapabilityId());
            } else {
                this.cleanupCapabilityServiceOfResource(capabilityJob.getResourceId(), capabilityJob.getCapabilityId());
                LOG.info("Capability [id=" + capabilityJob.getId() + "] successfully uninstalled from resource [id= " + capabilityJob.getResourceId() + "]");
            }
        } catch (ResourceNotFoundException e) {
            LOG.warn("Resource [id=" + capabilityJob.getResourceId() + "] does not exist => Skip removal of capability");
        } catch (ConsulLoginFailedException e) {
            LOG.error("Unable to remove capability [id=" + capabilityJob.getId() + "] from resource [id= " + capabilityJob.getResourceId() + "], " +
                    "because login to Consul failed: " + e.getMessage());
        } catch (IllegalAccessException e) {
            LOG.error("Unable to remove capability [id=" + capabilityJob.getId() + "] from resource [id= " + capabilityJob.getResourceId() + "], " +
                    "because access to Consul is not allowed: " + e.getMessage());
        }
    }

    private void cleanupCapabilityServiceOfResource(UUID resourceId, UUID capabilityId) throws ConsulLoginFailedException, IllegalAccessException {
        var capabilityService = singleHostCapabilitiesConsulClient.getCapabilityServiceOfResourceByCapabilityId(capabilityId, resourceId);
        if(capabilityService == null) {
            LOG.info("Resource [id=" + resourceId + " has no CapabilityService => Skip removal of capability");
            return;
        }

        this.singleHostCapabilitiesVaultClient.deleteSingleHostCapabilityServiceSecrets(
                new VaultCredential(),
                resourceId,
                capabilityService
        );

        this.singleHostCapabilitiesConsulClient.removeSingleHostCapabilityFromNode(
                new ConsulCredential(),
                capabilityService.getCapability(),
                resourceId);
    }

    //region CapabilityJobStateMachineListener
    @Override
    public void onStateEntry(CapabilityJob capabilityJob, CapabilityJobState enteredState) {
        try {
            switch (enteredState) {
                case INSTALLING -> {
                    this.handleInstallingStateEntry(capabilityJob);
                }

                case INSTALLED -> {
                    var capabilityService = this.singleHostCapabilitiesConsulClient
                            .getCapabilityServiceOfResourceByCapabilityId(capabilityJob.getCapabilityId(), capabilityJob.getResourceId());
                    this.updateCapabilityServiceStatus(capabilityJob.getResourceId(), capabilityService, CapabilityServiceStatus.READY);
                    LOG.info("Capability [id= " + capabilityJob.getId() + "] successfully installed on resource [id= " + capabilityJob.getResourceId() + "]");
                }

                case UNINSTALLING -> {
                    this.handleUninstallingStateEntry(capabilityJob);
                }

                case UNINSTALLED -> {
                    this.handleUninstalledStateEntry(capabilityJob);
                }

                case FAILED -> {
                    LOG.error("Capability job [id= " + capabilityJob.getId() + "] failed, trying to cleanup capability service");
                    this.capabilityJobIdToJwtAuthToken.remove(capabilityJob.getId());
                    this.cleanupCapabilityServiceOfResource(capabilityJob.getResourceId(), capabilityJob.getCapabilityId());
                }

                default -> {
                    LOG.warn("Capability job '{}' entered state '{}'", enteredState, capabilityJob.getId());
                }
            }
        }
        catch (Exception e) {
            LOG.error("Error while handling state entry for capability job [id= " + capabilityJob.getId() + "] in state " + enteredState, e);
        }
    }
    //endregion CapabilityJobStateMachineListener

    //region CapabilityJobExecutorListener
    @Override
    public void onCapabilityInstalled(CapabilityJob capabilityJob, CapabilityService capabilityService) {
        try {
            var capabilityJobStateMachine = this.capabilityJobStateMachineFactory.create(capabilityJob, this);
            this.changeStateOfCapabilityJobStateMachine(capabilityJob, capabilityJobStateMachine, CapabilityJobEvent.INSTALL_COMPLETED);
        } catch (Exception e) {
            LOG.error("Error while handling capability installation for capability job [id= " + capabilityJob.getId() + "]", e);
        }
    }

    @Override
    public void onCapabilityUninstalled(CapabilityJob capabilityJob, CapabilityService capabilityService) {
        try {
            var capabilityJobStateMachine = this.capabilityJobStateMachineFactory.create(capabilityJob, this);
            this.changeStateOfCapabilityJobStateMachine(capabilityJob, capabilityJobStateMachine, CapabilityJobEvent.UNINSTALL_COMPLETED);
        } catch (Exception e) {
            LOG.error("Error while handling capability uninstallation for capability job [id= " + capabilityJob.getId() + "]", e);
        }
    }

    @Override
    public void onError(CapabilityJob capabilityJob, CapabilityService capabilityService, Exception jobExecutorException) {
        try {
            var capabilityJobStateMachine = this.capabilityJobStateMachineFactory.create(capabilityJob, this);
            this.changeStateOfCapabilityJobStateMachine(capabilityJob, capabilityJobStateMachine, CapabilityJobEvent.ERROR_OCCURRED);
        } catch (Exception e) {
            LOG.error("Error while handling capability installation or uninstallation for capability job [id= " + capabilityJob.getId() + "]", e);
        }
    }
    //endregion CapabilityJobExecutorListener

    //region ResourceEventInternalListener
    @Override
    public void onResourceCreated(ResourceDTO resourceDTO) {
    }

    @Override
    public void onResourceUpdated(ResourceDTO resourceDTO) {
    }

    @Override
    public void onResourceDeleted(ResourceDTO resourceDTO) {
        // Cleanup all capability jobs of resource
        var capabilityJobsOfResource = this.capabilityJobJpaRepository.findByResourceId(resourceDTO.getId());
        this.capabilityJobJpaRepository.deleteAll(capabilityJobsOfResource);
        LOG.debug("Deleted " + capabilityJobsOfResource.size() + " capability jobs of resource [id=" + resourceDTO.getId() + "] because resource was deleted");
    }
    //endregion ResourceEventInternalListener


}
