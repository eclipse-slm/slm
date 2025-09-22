package org.eclipse.slm.notification_service.service.rest.messaging.resources

import io.swagger.v3.oas.annotations.media.Schema
import org.eclipse.slm.notification_service.model.EventNotification
import org.eclipse.slm.notification_service.model.EventType
import org.eclipse.slm.resource_management.common.resources.ResourceDTO
import java.util.*

class ResourceEventNotification(

    userId: String,

    timestamp: Date,

    override val eventType: ResourceEventType,

    val resource: ResourceDTO,

    ) : EventNotification(userId, timestamp) {
}

@Schema
enum class ResourceEventType : EventType {
    CREATED,
    UPDATED,
    DELETED
}
