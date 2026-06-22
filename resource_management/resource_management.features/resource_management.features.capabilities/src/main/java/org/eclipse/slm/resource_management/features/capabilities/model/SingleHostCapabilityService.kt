package org.eclipse.slm.resource_management.features.capabilities.model

import com.fasterxml.jackson.annotation.JsonTypeName
import java.util.*

@JsonTypeName("SingleHostCapabilityService")
class SingleHostCapabilityService(
    resourceId: UUID,
    serviceId: UUID,
    capability: Capability,
    status: CapabilityServiceStatus,
    isManaged: Boolean,
    configParameter: MutableMap<String, String>,
) : CapabilityService(resourceId, serviceId, capability, status, isManaged, configParameter)
