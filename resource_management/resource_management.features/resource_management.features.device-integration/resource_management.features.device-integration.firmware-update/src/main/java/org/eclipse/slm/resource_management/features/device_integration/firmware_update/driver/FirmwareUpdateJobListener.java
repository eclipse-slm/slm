package org.eclipse.slm.resource_management.features.device_integration.firmware_update.driver;

import java.util.Date;
import java.util.UUID;

public interface FirmwareUpdateJobListener {

    void onUpdatePrepared(UUID firmwareUpdateJobId);

    void onUpdateActivated(UUID firmwareUpdateJobId);

    void onUpdateFailed(UUID firmwareUpdateJobId, Throwable error);

    void onUpdateMessage(UUID firmwareUpdateJobId, Date timestamp, String message, String phase, int progress);

}
