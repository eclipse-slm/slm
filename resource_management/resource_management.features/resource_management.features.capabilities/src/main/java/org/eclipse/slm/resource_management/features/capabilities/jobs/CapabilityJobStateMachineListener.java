package org.eclipse.slm.resource_management.features.capabilities.jobs;

public interface CapabilityJobStateMachineListener {

    void onStateEntry(CapabilityJob firmwareUpdateJob, CapabilityJobState enteredState);

}
