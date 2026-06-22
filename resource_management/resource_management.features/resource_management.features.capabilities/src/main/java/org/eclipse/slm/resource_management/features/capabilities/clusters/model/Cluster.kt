package org.eclipse.slm.resource_management.features.capabilities.clusters.model

import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService
import java.util.*

class Cluster {
    var id: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    var name: String? = ""
    var clusterType: String = ""
    var clusterMemberTypes: List<ClusterMemberType> = emptyList()
    var memberResourceIds: List<UUID> = emptyList()
    var memberMapping: Map<UUID, String> = emptyMap()
    var metaData: Map<String, String>? = emptyMap()
    var capabilityService: MultiHostCapabilityService? = null
    var managed: Boolean = false

    constructor(multiHostCapabilityService: MultiHostCapabilityService, memberResourceIds: List<UUID>, metaData: Map<String, String>) {
        this.id = multiHostCapabilityService.serviceId
        this.name = multiHostCapabilityService.serviceName
        this.clusterType = multiHostCapabilityService.capability.name
        this.memberMapping = multiHostCapabilityService.memberMapping ?: emptyMap()
        this.clusterMemberTypes = multiHostCapabilityService.capability.clusterMemberTypes
        this.memberResourceIds = memberResourceIds
        this.managed = multiHostCapabilityService.managed
        this.capabilityService = multiHostCapabilityService
        this.metaData = multiHostCapabilityService.meta + metaData
    }

    constructor()
}
