package org.eclipse.slm.resource_management.service.rest.update;

public class FirmwareVersionDetails {

    private String version;

    private String date;

    private String installationUri;

    private String installationChecksum;

    private String softwareNameplateSubmodelId;

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

    public static class Builder {
        private String version;
        private String date;
        private String installationUri;
        private String installationChecksum;
        private String softwareNameplateSubmodelId;

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

        public FirmwareVersionDetails build() {
            return new FirmwareVersionDetails(this);
        }

        public Builder softwareNameplateSubmodelId(String softwareNameplateSubmodelId) {
            this.softwareNameplateSubmodelId = softwareNameplateSubmodelId;
            return this;
        }
    }
}
