package org.eclipse.slm.notification_service.model.resources

import org.eclipse.slm.notification_service.model.EventNotification
import org.eclipse.slm.notification_service.model.EventType
import org.eclipse.slm.resource_management.features.device_integration.discovery.dto.DiscoveryJobDTO
import java.util.Date

class DiscoveryJobEventNotification(

    userId: String,

    timestamp: Date,

    override val eventType: DiscoveryJobEventType,

    val discoveryJob: DiscoveryJobDTO,

    ) : EventNotification(userId, timestamp) {
}

enum class DiscoveryJobEventType : EventType {
    CHANGED,
}