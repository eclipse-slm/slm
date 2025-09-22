package org.eclipse.slm.resource_management.features.importer;

import java.util.UUID;

public class ImportCapability {

    public ImportCapability(UUID capabilityId, boolean skipInstall) {
        this.capabilityId = capabilityId;
        this.skipInstall = skipInstall;
    }

    private UUID capabilityId;

    private boolean skipInstall;

    public boolean isSkipInstall() {
        return skipInstall;
    }

    public void setSkipInstall(boolean skipInstall) {
        this.skipInstall = skipInstall;
    }

    public UUID getCapabilityId() {
        return capabilityId;
    }

    public void setCapabilityId(UUID capabilityId) {
        this.capabilityId = capabilityId;
    }
}
