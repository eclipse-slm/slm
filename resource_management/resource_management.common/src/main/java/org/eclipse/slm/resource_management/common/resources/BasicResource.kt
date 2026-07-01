package org.eclipse.slm.resource_management.common.resources

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Transient
import java.util.*

@Entity
class BasicResource
(
    @field:JsonProperty("id")
    @field:Id
    @field:Column(name = "uuid", length = 36, unique = true, nullable = false)
    var id: UUID
)
{
    constructor() : this(UUID.randomUUID())

    @field:Column(name = "asset_id")
    var assetId: String? = null

    @field:Column(name = "location_id")
    var locationId: UUID? = null

    @field:Column(name = "ip")
    var ip: String? = null

    @field:Column(name = "hostname")
    var hostname: String? = null

    var firmwareVersion: String? = null
    @field:Column(name = "firmware_version")
    var firmwareVersion: String? = null

    @field:Transient
    var clusterMember = false

    @field:Transient
    var capabilityServiceIds: List<UUID> = emptyList()

    @field:Transient
    var remoteAccessIds: List<UUID> = emptyList()

    @field:Column(name = "driver_id")
    var driverId: String? = null

    constructor(id: UUID, hostname: String, ip: String) : this(id) {
        this.hostname = hostname
        this.ip = ip
    }
}
