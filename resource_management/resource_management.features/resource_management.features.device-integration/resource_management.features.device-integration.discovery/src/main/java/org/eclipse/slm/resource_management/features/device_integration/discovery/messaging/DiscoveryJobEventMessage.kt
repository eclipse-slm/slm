package org.eclipse.slm.resource_management.features.device_integration.discovery.messaging

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.resource_management.features.device_integration.discovery.dto.DiscoveryJobDTO
import java.io.Serializable

class DiscoveryJobEventMessage @JsonCreator constructor(

    @field:JsonProperty("discoveryJob")
    val discoveryJob: DiscoveryJobDTO,

    @field:JsonProperty("eventType")
    val eventType: DiscoveryJobEventType

) : org.eclipse.slm.common.messaging.AbstractEventMessage<DiscoveryJobEventType>(EXCHANGE_NAME, ROUTING_KEY_PREFIX, DiscoveryJobEventType.CHANGED), Serializable {

    companion object {
        const val EXCHANGE_NAME: String = "resources"
        const val ROUTING_KEY_PREFIX: String = "discovery."
    }
}
