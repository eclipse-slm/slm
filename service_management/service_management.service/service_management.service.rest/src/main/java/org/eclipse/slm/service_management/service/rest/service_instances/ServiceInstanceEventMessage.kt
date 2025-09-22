package org.eclipse.slm.service_management.service.rest.service_instances

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.common.messaging.AbstractEventMessage
import org.eclipse.slm.service_management.model.services.ServiceInstance
import java.io.Serializable

class ServiceInstanceEventMessage @JsonCreator constructor(

    @JsonProperty("serviceInstance") val serviceInstance: ServiceInstance?,

    @JsonProperty("eventType") eventType: ServiceInstanceEventType?

) :
    AbstractEventMessage<ServiceInstanceEventType>(EXCHANGE_NAME, ROUTING_KEY_PREFIX, eventType), Serializable {

    companion object {
        const val EXCHANGE_NAME: String = "services"
        const val ROUTING_KEY_PREFIX: String = "instance."
    }

}
