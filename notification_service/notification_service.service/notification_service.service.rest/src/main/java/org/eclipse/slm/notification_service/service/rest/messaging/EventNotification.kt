package org.eclipse.slm.notification_service.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.eclipse.slm.notification_service.model.EventNotification.Companion.CAPABILITY_JOB_EVENT_CLASS
import org.eclipse.slm.notification_service.model.EventNotification.Companion.DISCOVERY_EVENT_CLASS
import org.eclipse.slm.notification_service.model.EventNotification.Companion.FIRMWARE_UPDATE_JOB_EVENT_CLASS
import org.eclipse.slm.notification_service.model.EventNotification.Companion.RESOURCE_EVENT_CLASS
import org.eclipse.slm.notification_service.model.EventNotification.Companion.SERVICE_INSTANCE_EVENT_CLASS
import org.eclipse.slm.notification_service.model.resources.DiscoveryJobEventNotification
import org.eclipse.slm.notification_service.service.rest.messaging.resources.CapabilityJobEventNotification
import org.eclipse.slm.notification_service.service.rest.messaging.resources.FirmwareUpdateJobEventNotification
import org.eclipse.slm.notification_service.service.rest.messaging.resources.ResourceEventNotification
import org.eclipse.slm.notification_service.service.rest.messaging.resources.ServiceInstanceEventNotification
import java.util.*

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ResourceEventNotification::class, name = RESOURCE_EVENT_CLASS),
    JsonSubTypes.Type(value = DiscoveryJobEventNotification::class, name = DISCOVERY_EVENT_CLASS),
    JsonSubTypes.Type(value = CapabilityJobEventNotification::class, name = CAPABILITY_JOB_EVENT_CLASS),
    JsonSubTypes.Type(value = FirmwareUpdateJobEventNotification::class, name = FIRMWARE_UPDATE_JOB_EVENT_CLASS),
    JsonSubTypes.Type(value = ServiceInstanceEventNotification::class, name = SERVICE_INSTANCE_EVENT_CLASS)
)
abstract class EventNotification (

    override val userId: String,

    val timestamp: Date,

) : IEventNotification {

    companion object {
        const val RESOURCE_EVENT_CLASS = "RESOURCE_EVENT"
        const val DISCOVERY_EVENT_CLASS = "DISCOVERY_EVENT"
        const val CAPABILITY_JOB_EVENT_CLASS = "CAPABILITY_JOB_EVENT"
        const val FIRMWARE_UPDATE_JOB_EVENT_CLASS = "FIRMWARE_UPDATE_JOB_EVENT"
        const val SERVICE_INSTANCE_EVENT_CLASS = "SERVICE_INSTANCE_EVENT"
    }

    override fun toString(): String {
        return "EventNotification{" +
                "userId='" + this.userId + "', " +
                "timestamp='" + this.timestamp + "', " +
                "eventType='" + this.eventType + "'}"
    }

}

enum class EVENT_CLASS {
    RESOURCE_EVENT,
    DISCOVERY_EVENT,
    CAPABILITY_JOB_EVENT,
    FIRMWARE_UPDATE_JOB_EVENT,
    SERVICE_INSTANCE_EVENT,
}