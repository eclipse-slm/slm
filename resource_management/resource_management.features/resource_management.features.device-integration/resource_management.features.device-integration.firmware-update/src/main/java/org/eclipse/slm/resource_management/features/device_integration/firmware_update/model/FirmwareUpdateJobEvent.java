package org.eclipse.slm.resource_management.features.device_integration.firmware_update.model;

public enum FirmwareUpdateJobEvent {
    PREPARATION_TRIGGERED,
    PREPARATION_COMPLETED,
    PREPARATION_FAILED,
    ACTIVATION_TRIGGERED,
    ACTIVATION_COMPLETED,
    ACTIVATION_FAILED,
    CANCEL_TRIGGERED,
    CANCEL_COMPLETED,
    CANCEL_FAILED
}
