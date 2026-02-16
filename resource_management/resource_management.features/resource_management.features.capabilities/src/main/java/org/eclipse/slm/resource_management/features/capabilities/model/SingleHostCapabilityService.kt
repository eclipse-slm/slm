package org.eclipse.slm.resource_management.features.capabilities.model

import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.common.consul.model.catalog.NodeService
import org.eclipse.slm.resource_management.features.capabilities.CapabilityUtil
import java.util.*

@JsonTypeName("SingleHostCapabilityService")
class SingleHostCapabilityService : CapabilityService {

    constructor(
        resourceId: UUID,
        serviceId: UUID,
        capability: Capability,
        status: CapabilityServiceStatus,
        isManaged: Boolean,
        configParameter: MutableMap<String,String>,
    ) : super(resourceId, serviceId, capability, status, )
    {
        super.managed = isManaged
        super.customMeta = configParameter
    }

    companion object {
        @JvmStatic
        fun createFromNodeService(nodeService: NodeService, nodeId: UUID, capability: Capability): SingleHostCapabilityService {
            var singleHostCapabilityService = SingleHostCapabilityService(
                nodeId,
                nodeService.id,
                capability,
                CapabilityServiceStatus.valueOf(nodeService.meta?.get(META_KEY_STATUS)!!),
                nodeService.meta?.get(META_KEY_MANAGED).toBoolean(),
                CapabilityUtil.getCustomMeta(nodeService)
            )
            singleHostCapabilityService.port = nodeService.port

            return singleHostCapabilityService
        }
    }
}