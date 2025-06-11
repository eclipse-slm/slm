package org.eclipse.slm.resource_management.service.discovery.api;

import java.util.UUID;

public class DiscoveryJobStartedResponse {

    private UUID discoveryJobId;

    public DiscoveryJobStartedResponse() {
    }

    public DiscoveryJobStartedResponse(UUID discoveryJobId) {
        this.discoveryJobId = discoveryJobId;
    }

    public UUID getDiscoveryJobId() {
        return discoveryJobId;
    }

    public void setDiscoveryJobId(UUID discoveryJobId) {
        this.discoveryJobId = discoveryJobId;
    }
}
