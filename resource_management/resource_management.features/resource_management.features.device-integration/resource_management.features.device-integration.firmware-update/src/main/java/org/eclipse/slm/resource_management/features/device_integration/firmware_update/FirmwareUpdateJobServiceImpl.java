package org.eclipse.slm.resource_management.features.device_integration.firmware_update;

import org.eclipse.slm.common.model.exceptions.EventNotAcceptedException;
import org.eclipse.slm.common.utils.files.FilesUtil;
import org.eclipse.slm.resource_management.common.resources.ResourcesManager;
import org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver.DriverRegistryClient;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.driver.FirmwareUpdateDriverClientFactory;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.driver.FirmwareUpdateJobListener;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.exceptions.FirmwareUpdateAlreadyInProgressException;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.exceptions.FirmwareUpdateJobNotFoundException;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJobEvent;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJob;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class FirmwareUpdateJobServiceImpl implements FirmwareUpdateJobService, FirmwareUpdateJobStateMachineListener, FirmwareUpdateJobListener {

    private final static Logger LOG = LoggerFactory.getLogger(FirmwareUpdateJobServiceImpl.class);

    private final FirmwareUpdateJobJpaRepository firmwareUpdateJobJpaRepository;

    private final FirmwareUpdateJobStateMachineFactory firmwareUpdateJobStateMachineFactory;

    private final ResourcesManager resourcesManager;

    private final FirmwareUpdateManager firmwareUpdateManager;

    private final FirmwareUpdateDriverClientFactory firmwareUpdateDriverClientFactory;

    private final DriverRegistryClient driverRegistryClient;

    public FirmwareUpdateJobServiceImpl(FirmwareUpdateJobJpaRepository firmwareUpdateJobJpaRepository,
                                        FirmwareUpdateJobStateMachineFactory firmwareUpdateJobStateMachineFactory,
                                        ResourcesManager resourcesManager, FirmwareUpdateManager firmwareUpdateManager,
                                        FirmwareUpdateDriverClientFactory firmwareUpdateDriverClientFactory,
                                        DriverRegistryClient driverRegistryClient) {
        this.firmwareUpdateJobJpaRepository = firmwareUpdateJobJpaRepository;
        this.firmwareUpdateJobStateMachineFactory = firmwareUpdateJobStateMachineFactory;
        this.resourcesManager = resourcesManager;
        this.firmwareUpdateManager = firmwareUpdateManager;
        this.firmwareUpdateDriverClientFactory = firmwareUpdateDriverClientFactory;
        this.driverRegistryClient = driverRegistryClient;
    }

    //region FirmwareUpdateJobService
    @Override
    public List<FirmwareUpdateJob> getFirmwareUpdateJobsOfResource(UUID resourceId) {
        var firmwareUpdateJobs = this.firmwareUpdateJobJpaRepository.findByResourceIdOrderByCreatedAtDesc(resourceId);

        return firmwareUpdateJobs;
    }

    @Override
    public void initFirmwareUpdate(UUID resourceId, String softwareNameplateId, String userId) throws Exception {
        // Check if firmware update is already in progress for the resource (by checking the state of the latest job)
        var firmwareUpdateJobsOfResource = this.firmwareUpdateJobJpaRepository.findByResourceIdOrderByCreatedAtDesc(resourceId);
        if (!firmwareUpdateJobsOfResource.isEmpty()
                && !FirmwareUpdateJobState.getEndStates().contains(firmwareUpdateJobsOfResource.get(0).getState())) {
            throw new FirmwareUpdateAlreadyInProgressException(resourceId);
        }
        // Check if resource exists
        var resource = this.resourcesManager.getResourceByIdOrThrow(resourceId);
        // Get driver to check if it is available
        var driverId = resource.getDriverId();
        var driverInfo = this.driverRegistryClient.getRegisteredDriver(driverId);
        // Create and store firmware update job
        var firmwareUpdateJob = new FirmwareUpdateJob(UUID.randomUUID(), resourceId, driverId, softwareNameplateId, userId);
        firmwareUpdateJob = this.firmwareUpdateJobJpaRepository.save(firmwareUpdateJob);
        // Create state machine for firmware update job and trigger preparation
        var firmwareUpdateJobStateMachine = this.firmwareUpdateJobStateMachineFactory.create(firmwareUpdateJob, this);
        this.changeStateOfFirmwareUpdateStateMachine(firmwareUpdateJob, firmwareUpdateJobStateMachine, FirmwareUpdateJobEvent.PREPARATION_TRIGGERED);
    }

    @Override
    public void activateFirmwareUpdate(UUID firmwareUpdateJobId) throws Exception {
        var firmwareUpdateJob = this.firmwareUpdateJobJpaRepository.findById(firmwareUpdateJobId)
                .orElseThrow(() -> new FirmwareUpdateJobNotFoundException(firmwareUpdateJobId));

        this.changeStateOfFirmwareUpdateJobOrThrow(firmwareUpdateJob, FirmwareUpdateJobEvent.ACTIVATION_TRIGGERED);
    }

    @Override
    public void cancelFirmwareUpdate(UUID firmwareUpdateJobId) throws Exception {
        var firmwareUpdateJob = this.firmwareUpdateJobJpaRepository.findById(firmwareUpdateJobId)
                .orElseThrow(() -> new FirmwareUpdateJobNotFoundException(firmwareUpdateJobId));

        this.changeStateOfFirmwareUpdateJobOrThrow(firmwareUpdateJob, FirmwareUpdateJobEvent.CANCEL_TRIGGERED);
    }
    //endregion FirmwareUpdateJobService

    private void triggerFirmwareUpdatePreparation(FirmwareUpdateJob firmwareUpdateJob) {
        try {
            var driverInfo = this.driverRegistryClient.getRegisteredDriver(firmwareUpdateJob.getDriverId());
            var updateDriverClient = firmwareUpdateDriverClientFactory.createDriverClient(driverInfo);

            var updateFileInputStream = firmwareUpdateManager.getFirstUpdateFileOfSoftwareNameplate(firmwareUpdateJob.getSoftwareNameplateId(), null);
            var updateFileBytes = FilesUtil.inputStreamToByteArray(updateFileInputStream);

            var connectionParameters = resourcesManager.getConnectionParametersOfResource(firmwareUpdateJob.getResourceId());

            updateDriverClient.prepareFirmwareUpdate(firmwareUpdateJob.getId(), connectionParameters, updateFileBytes, this);
            LOG.debug("State | Entry: PREPARING | Firmware update preparation started");
        } catch (Exception e) {
            LOG.error("Error during firmware update preparation for job {}: {}", firmwareUpdateJob.getId(), e.getMessage(), e);
            this.changeStateOfFirmwareUpdateJob(firmwareUpdateJob, FirmwareUpdateJobEvent.PREPARATION_FAILED);
        }
    }

    private void triggerFirmwareUpdateActivation(FirmwareUpdateJob firmwareUpdateJob) {
        try {
            var driverInfo = this.driverRegistryClient.getRegisteredDriver(firmwareUpdateJob.getDriverId());
            var updateDriverClient = firmwareUpdateDriverClientFactory.createDriverClient(driverInfo);

            var updateFileInputStream = firmwareUpdateManager.getFirstUpdateFileOfSoftwareNameplate(firmwareUpdateJob.getSoftwareNameplateId(), null);
            var updateFileBytes = FilesUtil.inputStreamToByteArray(updateFileInputStream);

            var connectionParameters = resourcesManager.getConnectionParametersOfResource(firmwareUpdateJob.getResourceId());

            updateDriverClient.activateFirmwareUpdate(firmwareUpdateJob.getId(), connectionParameters, updateFileBytes, this);
            LOG.debug("State | Entry: ACTIVATING | Firmware update activation started");
        } catch (Exception e) {
            LOG.error("Error during firmware update activation for job {}: {}", firmwareUpdateJob.getId(), e.getMessage(), e);
            this.changeStateOfFirmwareUpdateJob(firmwareUpdateJob, FirmwareUpdateJobEvent.ACTIVATION_FAILED);
        }
    }

    private boolean changeStateOfFirmwareUpdateJob(
            UUID firmwareUpdateJobId,
            FirmwareUpdateJobEvent event) {
        var firmwareUpdateJob = this.firmwareUpdateJobJpaRepository.findById(firmwareUpdateJobId)
                .orElseThrow(() -> new FirmwareUpdateJobNotFoundException(firmwareUpdateJobId));

        return this.changeStateOfFirmwareUpdateJob(firmwareUpdateJob, event);
    }

    private boolean changeStateOfFirmwareUpdateJob(
            FirmwareUpdateJob firmwareUpdateJob,
            FirmwareUpdateJobEvent event) {
        try {
            return this.changeStateOfFirmwareUpdateJobOrThrow(firmwareUpdateJob, event);
        } catch (Exception e) {
            LOG.error("Error while changing state of firmware update job {}: {}", firmwareUpdateJob.getId(), e.getMessage(), e);
            return false;
        }
    }

    private boolean changeStateOfFirmwareUpdateJobOrThrow(
            FirmwareUpdateJob firmwareUpdateJob,
            FirmwareUpdateJobEvent event) throws Exception {
        var firmwareUpdateJobStateMachine = firmwareUpdateJobStateMachineFactory.create(firmwareUpdateJob, this);

        return this.changeStateOfFirmwareUpdateStateMachine(firmwareUpdateJob, firmwareUpdateJobStateMachine, event);
    }

    private boolean changeStateOfFirmwareUpdateStateMachine(FirmwareUpdateJob firmwareUpdateJob,
                                                            StateMachine<FirmwareUpdateJobState, FirmwareUpdateJobEvent> firmwareUpdateJobStateMachine,
                                                            FirmwareUpdateJobEvent event) {
        Message<FirmwareUpdateJobEvent> message = MessageBuilder.withPayload(event)
                .setHeader("firmwareUpdateJob", firmwareUpdateJob)
                .build();

        var result = firmwareUpdateJobStateMachine.sendEvent(Mono.just(message)).blockFirst();

        if (result.getResultType().equals(StateMachineEventResult.ResultType.DENIED)) {
            var currentState = result.getRegion().getState().getId();
            throw new EventNotAcceptedException(currentState.toString(), event.toString());
        }

        return true;
    }

    //region FirmwareUpdateJobStateMachineListener
    @Override
    public void onStateEntry(FirmwareUpdateJob firmwareUpdateJob, FirmwareUpdateJobState enteredState) {
        switch (enteredState) {
            case PREPARING -> {
                this.triggerFirmwareUpdatePreparation(firmwareUpdateJob);
            }
            case ACTIVATING -> {
                this.triggerFirmwareUpdateActivation(firmwareUpdateJob);
            }
            case ACTIVATED, CANCELED, FAILED -> {
                LOG.debug("Firmware update job '{}' has reached end state '{}'", enteredState, firmwareUpdateJob.getId());
            }
            default -> {
                LOG.warn("Firmware update job '{}' entered state '{}'", enteredState, firmwareUpdateJob.getId());
            }
        }
    }
    //endregion FirmwareUpdateJobStateMachineListener

    //region FirmwareUpdateJobListener
    @Override
    public void onUpdatePrepared(UUID firmwareUpdateJobId) {
        this.changeStateOfFirmwareUpdateJob(firmwareUpdateJobId, FirmwareUpdateJobEvent.PREPARATION_COMPLETED);
    }

    @Override
    public void onUpdateActivated(UUID firmwareUpdateJobId) {
        var firmwareUpdateJobOptional = this.firmwareUpdateJobJpaRepository.findById(firmwareUpdateJobId);
        if (firmwareUpdateJobOptional.isEmpty()) {
            LOG.error("Firmware update job '{}' not found while handling update failure", firmwareUpdateJobId);
            return;
        }

        var firmwareUpdateJob = firmwareUpdateJobOptional.get();
        var updateInformationOfResource = this.firmwareUpdateManager.getUpdateInformationOfResource(firmwareUpdateJob.getResourceId());
        var firmwareVersionDetailsOptional = updateInformationOfResource.getAvailableFirmwareVersions().stream()
                .filter(fvd -> fvd.getSoftwareNameplateSubmodelId().equals(firmwareUpdateJob.getSoftwareNameplateId()))
                .findAny();
        if (firmwareVersionDetailsOptional.isEmpty()) {
            LOG.error("No firmware version details found for software nameplate '{}' of resource '{}'", firmwareUpdateJob.getSoftwareNameplateId(), firmwareUpdateJob.getResourceId());
            return;
        }
        var newVersion = firmwareVersionDetailsOptional.get().getVersion();

        this.changeStateOfFirmwareUpdateJob(firmwareUpdateJob, FirmwareUpdateJobEvent.ACTIVATION_COMPLETED);
        this.resourcesManager.setFirmwareVersionOfResource(firmwareUpdateJob.getResourceId(), newVersion);
    }

    @Override
    public void onUpdateFailed(UUID firmwareUpdateJobId, String errorMessage) {
        var firmwareUpdateJobOptional = this.firmwareUpdateJobJpaRepository.findById(firmwareUpdateJobId);
        if (firmwareUpdateJobOptional.isEmpty()) {
            LOG.error("Firmware update job '{}' not found while handling update failure: {}", firmwareUpdateJobId, errorMessage);
            return;
        }

        switch (firmwareUpdateJobOptional.get().getState()) {
            case PREPARING -> {
                this.changeStateOfFirmwareUpdateJob(firmwareUpdateJobId, FirmwareUpdateJobEvent.PREPARATION_FAILED);
            }
            case ACTIVATING -> {
                this.changeStateOfFirmwareUpdateJob(firmwareUpdateJobId, FirmwareUpdateJobEvent.ACTIVATION_FAILED);
            }
            case CANCELING -> {
                this.changeStateOfFirmwareUpdateJob(firmwareUpdateJobId, FirmwareUpdateJobEvent.CANCEL_FAILED);
            }
            default -> {
                LOG.error("Firmware update job '{}' failed in an unexpected state: {}", firmwareUpdateJobId, firmwareUpdateJobOptional.get().getState());
            }
        }
    }

    @Override
    public void onUpdateMessage(UUID firmwareUpdateJobId, Date timestamp, String message, String phase, int progress) {
        var firmwareUpdateJobOptional = this.firmwareUpdateJobJpaRepository.findById(firmwareUpdateJobId);
        if (firmwareUpdateJobOptional.isEmpty()) {
            LOG.error("Firmware update job '{}' not found while handling update message: {}", firmwareUpdateJobId, message);
            return;
        }

        ZonedDateTime zonedDateTime = timestamp.toInstant().atZone(ZoneId.systemDefault());
        String formattedTimestamp = zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        String progressString = String.format("Progress: %d%%", progress);

        var firmwareUpdateJob = firmwareUpdateJobOptional.get();
        var messageString = String.format("%-26s %-18s %-16s %s", formattedTimestamp, phase, progressString, message);
        firmwareUpdateJob.addLogMessage(messageString);

        this.firmwareUpdateJobJpaRepository.save(firmwareUpdateJob);
    }
    //endregion FirmwareUpdateJobListener
}
