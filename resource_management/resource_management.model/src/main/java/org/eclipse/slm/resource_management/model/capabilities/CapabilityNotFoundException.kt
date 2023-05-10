package org.eclipse.slm.resource_management.model.capabilities

import java.util.*

class CapabilityNotFoundException(capabilityId: UUID) : Exception("Capability [id='$capabilityId'] not found")
