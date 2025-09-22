package org.eclipse.slm.resource_management.features.device_integration.firmware_update.messaging

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.common.messaging.AbstractEventMessage
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.dto.FirmwareUpdateJobDTO
import java.io.Serializable

class FirmwareUpdateJobEventMessage @JsonCreator constructor(

    @JsonProperty("firmwareUpdateJob") val firmwareUpdateJob: FirmwareUpdateJobDTO?

) :
    AbstractEventMessage<FirmwareUpdateJobEventType>(EXCHANGE_NAME, ROUTING_KEY_PREFIX, FirmwareUpdateJobEventType.CHANGED), Serializable {

    companion object {
        const val EXCHANGE_NAME: String = "resources"
        const val ROUTING_KEY_PREFIX: String = "firmware-update-job."
    }

}
