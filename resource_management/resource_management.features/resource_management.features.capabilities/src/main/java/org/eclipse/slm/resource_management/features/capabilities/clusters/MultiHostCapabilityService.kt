package org.eclipse.slm.resource_management.features.capabilities.clusters

import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.resource_management.features.capabilities.model.Capability
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus
import java.util.*

@JsonTypeName("MultiHostCapabilityService")
class MultiHostCapabilityService : CapabilityService {

    constructor(
        resourceId: UUID,
        serviceId: UUID,
        capability: Capability,
        memberMapping: MutableMap<UUID, String>,
        status: CapabilityServiceStatus,
        managed: Boolean,
        customMetadata: Map<String, String>
    ) : super(resourceId, serviceId, capability, status, managed, customMetadata) {
        this.memberMapping = memberMapping
    }

    //<NodeID, MemberTypeName>
    var memberMapping: MutableMap<UUID, String>? = null

    fun applyScaleUp(scaleUpOperation: ScaleUpOperation) {
        this.memberMapping!![scaleUpOperation.resourceId] = scaleUpOperation.clusterMemberType.name
    }
}
