package org.eclipse.slm.resource_management.common.resources

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class BasicResource
(
    @JsonProperty("id")
    var id: UUID
)
{
    var assetId: String? = null

    var locationId: UUID? = null

    var ip: String? = null

    var hostname: String? = null

    var firmwareVersion: String? = null;

    var clusterMember = false

    var capabilityServiceIds: List<UUID> = emptyList()

    var remoteAccessIds: List<UUID> = emptyList()

    var driverId: String? = null

    constructor(id: UUID, hostname: String, ip: String) : this(id) {
        this.hostname = hostname;
        this.ip = ip;
    }
}
