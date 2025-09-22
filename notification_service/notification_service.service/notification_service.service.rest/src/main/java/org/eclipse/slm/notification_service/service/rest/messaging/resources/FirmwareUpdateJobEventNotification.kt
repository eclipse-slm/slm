package org.eclipse.slm.notification_service.service.rest.messaging.resources

import io.swagger.v3.oas.annotations.media.Schema
import org.eclipse.slm.notification_service.model.EventNotification
import org.eclipse.slm.notification_service.model.EventType
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.dto.FirmwareUpdateJobDTO
import java.util.*

class FirmwareUpdateJobEventNotification(

    userId: String,

    timestamp: Date,

    override val eventType: FirmwareUpdateJobEventType,

    val firmwareUpdateJob: FirmwareUpdateJobDTO,

    ) : EventNotification(userId, timestamp) {
}

@Schema
enum class FirmwareUpdateJobEventType : EventType {
    CHANGED
}
