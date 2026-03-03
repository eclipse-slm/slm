package org.eclipse.slm.resource_management.features.capabilities.providers

import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService

open class Provider() {
    var capabilityService: CapabilityService? = null;
    var capabilityClass: String = "";
    constructor(capabilityService : CapabilityService, capabilityClass: Class<*>) : this() {
        this.capabilityService = capabilityService
        this.capabilityClass = capabilityClass.simpleName
    }
}
