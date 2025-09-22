package org.eclipse.slm.resource_management.features.device_integration.firmware_update.model;

public class FirmwareVersionDetails {

    private String version;

    private String date;

    private String installationUri;

    private String installationChecksum;

    private String softwareNameplateSubmodelId;

    private FirmwareUpdateFile firmwareUpdateFile;

    public FirmwareVersionDetails(String version, String date, String installationUri, String installationChecksum, String softwareNameplateSubmodelId) {
        this.version = version;
        this.date = date;
        this.installationUri = installationUri;
        this.installationChecksum = installationChecksum;
    }

    private FirmwareVersionDetails(Builder builder) {
        this.version = builder.version;
        this.date = builder.date;
        this.installationUri = builder.installationUri;
        this.installationChecksum = builder.installationChecksum;
        this.softwareNameplateSubmodelId = builder.softwareNameplateSubmodelId;
        this.firmwareUpdateFile = builder.firmwareUpdateFile;
    }

    public String getVersion() {
        return version;
    }

    public String getDate() {
        return date;
    }

    public String getInstallationUri() {
        return installationUri;
    }

    public String getInstallationChecksum() {
        return installationChecksum;
    }

    public String getSoftwareNameplateSubmodelId() {
        return softwareNameplateSubmodelId;
    }

    public FirmwareUpdateFile getFirmwareUpdateFile() {
        return firmwareUpdateFile;
    }

    public static class Builder {
        private String version;
        private String date;
        private String installationUri;
        private String installationChecksum;
        private String softwareNameplateSubmodelId;
        private FirmwareUpdateFile firmwareUpdateFile;

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder date(String date) {
            this.date = date;
            return this;
        }

        public Builder installationUri(String installationUri) {
            this.installationUri = installationUri;
            return this;
        }

        public Builder installationChecksum(String installationChecksum) {
            this.installationChecksum = installationChecksum;
            return this;
        }

        public Builder softwareNameplateSubmodelId(String softwareNameplateSubmodelId) {
            this.softwareNameplateSubmodelId = softwareNameplateSubmodelId;
            return this;
        }

        public Builder firmwareUpdateFile(FirmwareUpdateFile firmwareUpdateFile) {
            this.firmwareUpdateFile = firmwareUpdateFile;
            return this;
        }

        public FirmwareVersionDetails build() {
            return new FirmwareVersionDetails(this);
        }
    }
}
