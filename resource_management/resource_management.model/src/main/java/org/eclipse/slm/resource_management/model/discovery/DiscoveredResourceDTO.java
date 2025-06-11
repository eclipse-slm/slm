package org.eclipse.slm.resource_management.model.discovery;

import java.util.UUID;

public class DiscoveredResourceDTO {

    private String id;

    private UUID resourceId;

    private String name = "N/A";

    private String serialNumber = "N/A";

    private String manufacturerName = "N/A";

    private String productName = "N/A";

    private String ipAddress = "N/A";

    private String macAddress = "N/A";

    private String firmwareVersion = "N/A";

    private boolean ignored = false;

    private UUID discoveryJobId;

    private String resultId;

    private boolean onboarded = false;

    public DiscoveredResourceDTO() {
    }

    private DiscoveredResourceDTO(Builder builder) {
        this.id = builder.id;
        this.resourceId = builder.resourceId;
        this.name = builder.name;
        this.serialNumber = builder.serialNumber;
        this.manufacturerName = builder.manufacturerName;
        this.productName = builder.productName;
        this.ipAddress = builder.ipAddress;
        this.macAddress = builder.macAddress;
        this.firmwareVersion = builder.firmwareVersion;
        this.ignored = builder.ignored;
        this.discoveryJobId = builder.discoveryJobId;
        this.resultId = builder.resultId;
        this.onboarded = builder.onboarded;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public void setResourceId(UUID resourceId) {
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public UUID getDiscoveryJobId() {
        return discoveryJobId;
    }

    public void setDiscoveryJobId(UUID discoveryJobId) {
        this.discoveryJobId = discoveryJobId;
    }

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public boolean isOnboarded() {
        return onboarded;
    }

    public void setOnboarded(boolean onboarded) {
        this.onboarded = onboarded;
    }

    public static class Builder {

        private String id;
        private UUID resourceId;
        private String name = "N/A";;
        private String serialNumber = "N/A";;
        private String manufacturerName = "N/A";;
        private String productName = "N/A";;
        private String ipAddress = "N/A";;
        private String macAddress = "N/A";;
        private String firmwareVersion = "N/A";;
        private boolean ignored = false;
        private UUID discoveryJobId;
        private String resultId;
        private boolean onboarded;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder resourceId(UUID resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder serialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        public Builder manufacturerName(String manufacturerName) {
            this.manufacturerName = manufacturerName;
            return this;
        }

        public Builder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder macAddress(String macAddress) {
            this.macAddress = macAddress;
            return this;
        }

        public Builder firmwareVersion(String firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
            return this;
        }

        public Builder ignored(boolean ignored) {
            this.ignored = ignored;
            return this;
        }

        public Builder discoveryJobId(UUID discoveryJobId) {
            this.discoveryJobId = discoveryJobId;
            return this;
        }

        public Builder resultId(String resultId) {
            this.resultId = resultId;
            return this;
        }

        public Builder resultId(Boolean onboarded) {
            this.onboarded = onboarded;
            return this;
        }

        public DiscoveredResourceDTO build() {
            return new DiscoveredResourceDTO(this);
        }
    }

}
