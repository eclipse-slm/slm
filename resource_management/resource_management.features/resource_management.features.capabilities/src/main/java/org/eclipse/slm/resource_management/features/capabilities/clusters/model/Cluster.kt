package org.eclipse.slm.resource_management.features.capabilities.clusters.model

import org.eclipse.slm.common.consul.model.catalog.Node
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService
import java.util.*
import kotlin.collections.plus

class Cluster {
    var id: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    var name: String? = ""
    var clusterType: String = ""
    var clusterMemberTypes: List<ClusterMemberType> = emptyList()
    var nodes: List<Node> = emptyList()
    var memberMapping: Map<UUID, String> = emptyMap()

    var metaData: Map<String, String>? = emptyMap()
    var capabilityService: MultiHostCapabilityService? = null
    var managed: Boolean = false

    constructor(multiHostCapabilityService: MultiHostCapabilityService, nodes: List<Node>) {
        this.id = multiHostCapabilityService.id
        this.name = multiHostCapabilityService.service
        this.clusterType = multiHostCapabilityService.capability!!.name
        this.memberMapping = multiHostCapabilityService.memberMapping!!
        this.clusterMemberTypes = multiHostCapabilityService.capability!!.clusterMemberTypes
        this.nodes = nodes
        this.managed = multiHostCapabilityService.managed

        this.capabilityService = multiHostCapabilityService
        this.metaData = capabilityService!!.serviceMeta
    }

    constructor(multiHostCapabilityService: MultiHostCapabilityService, nodes: List<Node>, metaData: Map<String, String>) {
        this.id = multiHostCapabilityService.id
        this.name = multiHostCapabilityService.service
        this.clusterType = multiHostCapabilityService.capability!!.name
        this.memberMapping = multiHostCapabilityService.memberMapping!!
        this.clusterMemberTypes = multiHostCapabilityService.capability!!.clusterMemberTypes
        this.nodes = nodes
        this.managed = multiHostCapabilityService.managed

        this.capabilityService = multiHostCapabilityService
        this.metaData = capabilityService!!.serviceMeta + metaData
    }

    constructor() {
    }
}
