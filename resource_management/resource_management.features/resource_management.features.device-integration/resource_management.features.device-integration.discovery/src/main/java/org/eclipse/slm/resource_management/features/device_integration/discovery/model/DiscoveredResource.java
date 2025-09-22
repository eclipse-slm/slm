package org.eclipse.slm.resource_management.features.device_integration.discovery.model;

import java.util.UUID;

public class DiscoveredResource {

    private String id;

    private UUID resourceId;

    private String name = "N/A";

    private String serialNumber = "N/A";

    private String manufacturerName = "N/A";

    private String productName = "N/A";

    private String ipAddress = "N/A";

    private String macAddress = "N/A";

    private String firmwareVersion = "N/A";

    private String connectionParameters = "";

    private boolean ignored = false;

    public DiscoveredResource() {
    }

    private DiscoveredResource(Builder builder) {
        this.id = builder.id;
        this.resourceId = builder.resourceId;
        this.name = builder.name;
        this.serialNumber = builder.serialNumber;
        this.manufacturerName = builder.manufacturerName;
        this.productName = builder.productName;
        this.ipAddress = builder.ipAddress;
        this.macAddress = builder.macAddress;
        this.firmwareVersion = builder.firmwareVersion;
        this.connectionParameters = builder.connectionParameters;
        this.ignored = builder.ignored;
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

    public String getConnectionParameters() {
        return connectionParameters;
    }

    public void setConnectionParameters(String connectionParameters) {
        this.connectionParameters = connectionParameters;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
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
        private String connectionParameters = "";
        private boolean ignored = false;

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

        public Builder connectionParameters(String connectionParameters) {
            this.connectionParameters = connectionParameters;
            return this;
        }

        public Builder ignored(boolean ignored) {
            this.ignored = ignored;
            return this;
        }

        public DiscoveredResource build() {
            return new DiscoveredResource(this);
        }
    }

}
