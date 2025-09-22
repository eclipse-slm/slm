package org.eclipse.slm.resource_management.features.capabilities.jobs;

import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService;

public interface CapabilityJobExecutorListener {

    void onCapabilityInstalled(CapabilityJob capabilityJob, CapabilityService capabilityService);

    void onCapabilityUninstalled(CapabilityJob capabilityJob, CapabilityService capabilityService);

    void onError(CapabilityJob capabilityJob, CapabilityService capabilityService, Exception e);

}
