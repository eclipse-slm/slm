package org.eclipse.slm.resource_management.features.device_integration.firmware_update.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class UpdateInformationResource {

    private List<FirmwareVersionDetails> availableFirmwareVersions;

    private FirmwareVersionDetails currentFirmwareVersion;

    private FirmwareVersionDetails latestFirmwareVersion;

    private FirmwareUpdateStatus firmwareUpdateStatus;

    private List<FirmwareUpdateJob> firmwareUpdateJobs = new ArrayList<>();

    public List<FirmwareVersionDetails> getAvailableFirmwareVersions() {
        return availableFirmwareVersions;
    }

    public void setAvailableFirmwareVersions(List<FirmwareVersionDetails> availableFirmwareVersions) {
        this.availableFirmwareVersions = availableFirmwareVersions;
    }

    public FirmwareVersionDetails getCurrentFirmwareVersion() {
        return currentFirmwareVersion;
    }

    public void setCurrentFirmwareVersion(FirmwareVersionDetails currentFirmwareVersion) {
        this.currentFirmwareVersion = currentFirmwareVersion;
    }

    public FirmwareVersionDetails getLatestFirmwareVersion() {
        return latestFirmwareVersion;
    }

    public void setLatestFirmwareVersion(FirmwareVersionDetails latestFirmwareVersion) {
        this.latestFirmwareVersion = latestFirmwareVersion;
    }

    public FirmwareUpdateStatus getFirmwareUpdateStatus() {
        return firmwareUpdateStatus;
    }

    public void setFirmwareUpdateStatus(FirmwareUpdateStatus firmwareUpdateStatus) {
        this.firmwareUpdateStatus = firmwareUpdateStatus;
    }

    public List<FirmwareUpdateJob> getFirmwareUpdateJobs() {
        return firmwareUpdateJobs;
    }

    public void setFirmwareUpdateJobs(List<FirmwareUpdateJob> firmwareUpdateJobs) {
        this.firmwareUpdateJobs = firmwareUpdateJobs;
    }

    @JsonProperty("isUpdateInProgress")
    public boolean isUpdateInProgress() {
        if (firmwareUpdateJobs.isEmpty()) {
            return false;
        } else {
            firmwareUpdateJobs.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            FirmwareUpdateJob latestJob = firmwareUpdateJobs.get(0);
            return !FirmwareUpdateJobState.getEndStates().contains(latestJob.getState());
        }
    }
}
