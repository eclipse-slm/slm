package org.eclipse.slm.resource_management.features.device_integration.discovery.driver;

import org.eclipse.slm.resource_management.features.device_integration.discovery.model.DiscoveryJob;

public interface DiscoveryJobListener {

    void onDiscoveryCompleted(DiscoveryJob completedDiscoveryJob);

}
