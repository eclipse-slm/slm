package org.eclipse.slm.resource_management.common.resources

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.common.messaging.AbstractEventMessage
import java.io.Serializable

class ResourceEventMessage @JsonCreator constructor(

    @JsonProperty("resource") val resource: ResourceDTO?,

    @JsonProperty("eventType") eventType: ResourceEventType?

) : AbstractEventMessage<ResourceEventType>(EXCHANGE_NAME, ROUTING_KEY_PREFIX, eventType), Serializable {

    companion object {
        const val EXCHANGE_NAME: String = "resources"
        const val ROUTING_KEY_PREFIX: String = "resource."
    }

}
