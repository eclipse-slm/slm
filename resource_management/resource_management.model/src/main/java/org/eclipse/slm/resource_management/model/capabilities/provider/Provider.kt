package org.eclipse.slm.resource_management.model.capabilities.provider

import org.eclipse.slm.resource_management.model.consul.capability.CapabilityService

open class Provider() {
    var capabilityService: CapabilityService? = null;
    var capabilityClass: String = "";
    constructor(capabilityService : CapabilityService, capabilityClass: Class<*>) : this() {
        this.capabilityService = capabilityService
        this.capabilityClass = capabilityClass.simpleName
    }
}
