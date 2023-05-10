package org.eclipse.slm.resource_management.model.capabilities.provider

import org.eclipse.slm.resource_management.model.capabilities.VirtualizationCapability
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityService

class VirtualResourceProvider(capabilityService: CapabilityService) :
    Provider(capabilityService, VirtualizationCapability::class.java)
