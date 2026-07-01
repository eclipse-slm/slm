package org.eclipse.slm.notification_service.service.app.messaging.services

import io.swagger.v3.oas.annotations.media.Schema
import org.eclipse.slm.notification_service.service.app.messaging.EventNotification
import org.eclipse.slm.notification_service.model.EventType
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance
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
