package org.eclipse.slm.resource_management.service.importer;

        import org.eclipse.slm.resource_management.model.resource.ConnectionType;

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

                public Builder firmwareVersion(String firmwareVersion) {
                    this.firmwareVersion = firmwareVersion;
                    return this;
                }

                public ImportDevice build() {
                    return new ImportDevice(this);
                }
            }
        }