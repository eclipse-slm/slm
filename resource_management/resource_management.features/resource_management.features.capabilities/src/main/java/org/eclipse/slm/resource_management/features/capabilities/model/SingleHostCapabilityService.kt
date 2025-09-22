package org.eclipse.slm.resource_management.features.capabilities.model

import com.fasterxml.jackson.annotation.JsonTypeName
import java.util.UUID

@JsonTypeName("SingleHostCapabilityService")
class SingleHostCapabilityService : CapabilityService {

    var consulNodeId : UUID? = null

    constructor(capability: Capability, consulNodeId: UUID) : super(capability) {
        this.consulNodeId = consulNodeId
    }

    constructor(
        capability: Capability,
        consulNodeId: UUID,
        status: CapabilityServiceStatus,
        isManaged: Boolean,
        configParameter: MutableMap<String,String>?
    ) : super(capability, status)
    {
        this.consulNodeId = consulNodeId
        super.managed = isManaged
        if (configParameter != null) {
            super.customMeta = configParameter
        }
    }

    constructor(
        capability: Capability,
        consulNodeId: UUID,
        serviceId: UUID,
        status: CapabilityServiceStatus,
        isManaged: Boolean
    ) :
            super(capability, serviceId, status)
    {
        this.consulNodeId = consulNodeId
        super.managed = isManaged
    }

    constructor(
        capability: Capability,
        consulNodeId: UUID,
        serviceId: UUID,
        servicePort: Int,
        status: CapabilityServiceStatus,
        isManaged: Boolean,
        configParameter: MutableMap<String, String>
    ) :
            super(capability, serviceId, status)
    {
        this.consulNodeId = consulNodeId
        super.managed = isManaged
        super.customMeta = configParameter
        super.port = servicePort
    }

    constructor() : super()
}