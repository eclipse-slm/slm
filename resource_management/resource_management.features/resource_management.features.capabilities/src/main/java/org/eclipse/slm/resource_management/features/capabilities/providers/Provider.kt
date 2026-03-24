package org.eclipse.slm.resource_management.features.capabilities.providers

import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService

open class Provider(

    @param:JsonProperty("capabilityService")
    val capabilityService: CapabilityService? = null,

    @param:JsonProperty("capabilityClass")
    val capabilityClass: String = ""

) {

    constructor(capabilityService : CapabilityService, capabilityClass: Class<*>)
            : this(capabilityService, capabilityClass.simpleName) {
    }

}
