package org.eclipse.slm.resource_management.features.importer;

import org.eclipse.slm.resource_management.common.remote_access.ConnectionType;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ImportDevice {

    public UUID resourceId;
    public String assetId;
    public String hostname;
    public String ipAddress;
    public ConnectionType connectionType;
    public Integer connectionPort;
    public String username;
    public String password;
    public UUID locationId;
    public List<ImportCapability> capabilities;
    public String firmwareVersion;

    private ImportDevice(Builder builder) {
        this.resourceId = builder.resourceId;
        this.assetId = builder.assetId;
        this.hostname = builder.hostname;
        this.ipAddress = builder.ipAddress;
        this.connectionType = builder.connectionType;
        this.connectionPort = builder.connectionPort;
        this.username = builder.username;
        this.password = builder.password;
        this.locationId = builder.locationId;
        this.capabilities = builder.capabilities;
        this.firmwareVersion = builder.firmwareVersion;
    }

    public static class Builder {
        private UUID resourceId;
        private String assetId;
        private String hostname;
        private String ipAddress;
        private ConnectionType connectionType;
        private Integer connectionPort;
        private String username;
        private String password;
        private UUID locationId;
        private List<ImportCapability> capabilities = Collections.emptyList();
        private String firmwareVersion;

        public Builder resourceId(UUID resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder assetId(String assetId) {
            this.assetId = assetId;
            return this;
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder connectionType(ConnectionType connectionType) {
            this.connectionType = connectionType;
            return this;
        }

        public Builder connectionPort(Integer connectionPort) {
            this.connectionPort = connectionPort;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder locationId(UUID locationId) {
            this.locationId = locationId;
            return this;
        }

        public Builder capabilities(List<ImportCapability> capabilities) {
            this.capabilities = capabilities;
            return this;
        }

        public Builder firmwareVersion(String firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
            return this;
        }

        public ImportDevice build() {
            return new ImportDevice(this);
        }
    }
}