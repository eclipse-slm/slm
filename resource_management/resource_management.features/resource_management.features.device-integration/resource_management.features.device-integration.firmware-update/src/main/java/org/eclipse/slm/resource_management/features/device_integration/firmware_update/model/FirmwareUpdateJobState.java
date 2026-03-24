package org.eclipse.slm.resource_management.features.device_integration.firmware_update.model;

import java.util.EnumSet;

public enum FirmwareUpdateJobState {
    CREATED,
    PREPARING,
    PREPARED,
    ACTIVATING,
    ACTIVATED,
    CANCELING,
    CANCELED,
    FAILED;

    public static EnumSet<FirmwareUpdateJobState> getEndStates() {
        return EnumSet.of(ACTIVATED, CANCELED, FAILED);
    }
}
