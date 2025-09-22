package org.eclipse.slm.resource_management.features.capabilities.jobs.messaging

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.common.messaging.AbstractEventMessage
import org.eclipse.slm.resource_management.features.capabilities.dto.CapabilityJobDTO
import java.io.Serializable

class CapabilityJobEventMessage @JsonCreator constructor(

    @JsonProperty("capabilityJob") val capabilityJob: CapabilityJobDTO?

) :
    org.eclipse.slm.common.messaging.AbstractEventMessage<CapabilityJobEventType>(EXCHANGE_NAME, ROUTING_KEY_PREFIX, CapabilityJobEventType.CHANGED), Serializable {

    companion object {
        const val EXCHANGE_NAME: String = "resources"
        const val ROUTING_KEY_PREFIX: String = "capability-job."
    }

}
