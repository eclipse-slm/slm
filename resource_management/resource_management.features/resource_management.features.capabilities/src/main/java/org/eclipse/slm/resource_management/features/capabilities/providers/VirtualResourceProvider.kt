package org.eclipse.slm.resource_management.features.providers

import org.eclipse.slm.resource_management.features.capabilities.model.VirtualizationCapability
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService

class VirtualResourceProvider(capabilityService: CapabilityService) :
    Provider(capabilityService, VirtualizationCapability::class.java)
