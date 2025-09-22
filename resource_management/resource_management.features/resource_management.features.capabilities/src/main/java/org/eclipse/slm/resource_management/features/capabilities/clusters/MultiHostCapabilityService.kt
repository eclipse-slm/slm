package org.eclipse.slm.resource_management.features.capabilities.clusters

import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.common.consul.model.catalog.CatalogNode
import org.eclipse.slm.resource_management.features.capabilities.model.Capability
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus
import java.util.*

@JsonTypeName("MultiHostCapabilityService")
class MultiHostCapabilityService : CapabilityService {

    constructor() : super()
    constructor(
        capability: Capability,
        memberMapping: MutableMap<UUID, String>,
        status: CapabilityServiceStatus,
        managed: Boolean
    ) : super(capability, status) {
        this.memberMapping = memberMapping
        this.managed = managed
    }

    constructor(
            capability: Capability,
            memberMapping: MutableMap<UUID, String>,
            serviceId: UUID
    ) : super(capability, serviceId) {
        this.memberMapping = memberMapping
    }

    constructor(
        capability: Capability,
        memberMapping: MutableMap<UUID, String>,
        serviceId: UUID,
        status: CapabilityServiceStatus,
        managed: Boolean
    ) : super(capability, serviceId, status) {
        this.memberMapping = memberMapping
        this.managed = managed
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
        var meta = serviceMeta.toMutableMap()

        var clusterMemberTypeName = memberMapping!![nodeId]

        if (clusterMemberTypeName != null) {
            meta["clusterMemberType"] = clusterMemberTypeName
        }

        return meta as HashMap<String, String>;
    }

    fun getMapOfNodeIdsAndCatalogServices(): HashMap<UUID, CatalogNode.Service> {
        var consulNodeServiceMap = HashMap<UUID, CatalogNode.Service>();

        memberMapping!!.forEach{ (key, value) ->
            val catalogService = CatalogNode.Service(this.id)

            catalogService.service = this.service
            catalogService.tags = this.getTagsByNodeId(key)
            catalogService.serviceMeta = this.getServiceMetaByNodeId(key)

            consulNodeServiceMap[key] = catalogService
        }

        return consulNodeServiceMap;
    }
    fun applyScaleUp(scaleUpOperation : ScaleUpOperation) {
        this.memberMapping!![scaleUpOperation.resourceId] = scaleUpOperation.clusterMemberType.name
    }
}
