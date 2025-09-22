package org.eclipse.slm.notification_service.service.rest.messaging.resources

import io.swagger.v3.oas.annotations.media.Schema
import org.eclipse.slm.notification_service.model.EventNotification
import org.eclipse.slm.notification_service.model.EventType
import org.eclipse.slm.resource_management.features.capabilities.dto.CapabilityJobDTO
import java.util.*

class CapabilityJobEventNotification(

    userId: String,

    timestamp: Date,

    override val eventType: CapabilityJobEventType,

    val capabilityJob: CapabilityJobDTO,

    ) : EventNotification(userId, timestamp) {
}

@Schema
enum class CapabilityJobEventType : EventType {
    CHANGED
}
