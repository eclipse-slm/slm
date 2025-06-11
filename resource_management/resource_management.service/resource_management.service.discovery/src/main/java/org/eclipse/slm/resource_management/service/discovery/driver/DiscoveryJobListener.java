package org.eclipse.slm.resource_management.service.discovery.driver;

import org.eclipse.slm.resource_management.model.discovery.DiscoveryJob;

public interface DiscoveryJobListener {

    void onDiscoveryCompleted(DiscoveryJob completedDiscoveryJob);

}
