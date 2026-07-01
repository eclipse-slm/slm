package org.eclipse.slm.resource_management.features.capabilities.clusters

import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.common.consul.model.catalog.NodeService
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

    fun getTagsByNodeId(nodeId: UUID): ArrayList<String> {
        var serviceTags = ArrayList(tags)
        var clusterMemberTypeName = memberMapping!![nodeId]
        var clusterMemberTypes = capability!!.clusterMemberTypes
        var clusterMemberType = clusterMemberTypes.firstOrNull { it.name.equals(clusterMemberTypeName) }

        serviceTags.add(clusterMemberTypeName!!)
        serviceTags.add(clusterMemberType!!.prettyName!!)

        return serviceTags
    }

    fun getServiceMetaByNodeId(nodeId: UUID): HashMap<String, String> {
        var meta = meta.toMutableMap()

        var clusterMemberTypeName = memberMapping!![nodeId]

        if (clusterMemberTypeName != null) {
            meta["clusterMemberType"] = clusterMemberTypeName
        }

        return meta as HashMap<String, String>
    }

    fun getMapOfNodeIdsAndCatalogServices(): HashMap<UUID, NodeService> {
        var consulNodeServiceMap = HashMap<UUID, NodeService>()

        memberMapping!!.forEach{ (key, value) ->
            val catalogService = NodeService(this.id, this.serviceName, this.taggedAddresses,
                this.getTagsByNodeId(key),
                this.getServiceMetaByNodeId(key))

            consulNodeServiceMap[key] = catalogService
        }

        return consulNodeServiceMap
    }
    fun applyScaleUp(scaleUpOperation : ScaleUpOperation) {
        this.memberMapping!![scaleUpOperation.resourceId] = scaleUpOperation.clusterMemberType.name
    }
}
