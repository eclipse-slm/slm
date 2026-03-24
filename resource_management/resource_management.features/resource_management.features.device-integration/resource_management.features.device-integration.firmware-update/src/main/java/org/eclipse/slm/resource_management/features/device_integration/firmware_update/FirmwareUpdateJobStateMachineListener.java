package org.eclipse.slm.resource_management.features.device_integration.firmware_update;


import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJobState;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJob;

public interface FirmwareUpdateJobStateMachineListener {

    void onStateEntry(FirmwareUpdateJob firmwareUpdateJob, FirmwareUpdateJobState enteredState);

}
