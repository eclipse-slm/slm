package org.eclipse.slm.resource_management.service.rest.update;

import java.util.List;

public class UpdateInformation {

    private List<FirmwareVersionDetails> availableFirmwareVersions;

    private FirmwareVersionDetails currentFirmwareVersion;

    private FirmwareVersionDetails latestFirmwareVersion;

    private FirmwareUpdateStatus firmwareUpdateStatus;

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
}
