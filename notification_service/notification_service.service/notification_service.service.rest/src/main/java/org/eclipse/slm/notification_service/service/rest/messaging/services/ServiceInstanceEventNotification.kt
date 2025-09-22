package org.eclipse.slm.notification_service.service.rest.messaging.resources

import io.swagger.v3.oas.annotations.media.Schema
import org.eclipse.slm.notification_service.model.EventNotification
import org.eclipse.slm.notification_service.model.EventType
import org.eclipse.slm.service_management.model.services.ServiceInstance
import java.util.*

class ServiceInstanceEventNotification(

    userId: String,

    timestamp: Date,

    override val eventType: ServiceInstanceEventType,

    val serviceInstance: ServiceInstance,

    ) : EventNotification(userId, timestamp) {
}

@Schema
enum class ServiceInstanceEventType : EventType {
    CREATED,
    UPDATED,
    DELETED
}
