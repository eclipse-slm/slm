package org.eclipse.slm.service_management.features.service_deployment.api.events

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.common.messaging.AbstractEventMessage
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance
import java.io.Serializable

// No @JvmOverloads: Kotlin copies @JsonCreator onto the generated overload too, and Jackson then
// rejects the type with "Conflicting property-based creators". ServiceInstanceEventMessageSender
// provides the two-argument convenience overload instead.
class ServiceInstanceEventMessage @JsonCreator constructor(

    @JsonProperty("serviceInstance") val serviceInstance: ServiceInstance?,

    @JsonProperty("eventType") eventType: ServiceInstanceEventType?,

    @JsonProperty("ownerGroups") val ownerGroups: Set<String>? = null

) :
    AbstractEventMessage<ServiceInstanceEventType>(EXCHANGE_NAME, ROUTING_KEY_PREFIX, eventType), Serializable {

    companion object {
        const val EXCHANGE_NAME: String = "services"
        const val ROUTING_KEY_PREFIX: String = "instance."
    }

}
