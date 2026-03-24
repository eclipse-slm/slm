package org.eclipse.slm.resource_management.common.resources

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
open class ResourceDTO(
    open var id: UUID,

    open var assetId: String? = null,

    open var locationId: UUID? = null,

    open var ip: String? = null,

    open var hostname: String? = null,

    open var firmwareVersion: String? = null,

    open var clusterMember: Boolean = false,

    open var capabilityServiceIds: List<UUID> = emptyList(),

    open var remoteAccessIds: List<UUID> = emptyList(),

    open var driverId: String? = null
) {

    @JsonProperty
    fun remoteAccessAvailable(): Boolean {
        return remoteAccessIds.isNotEmpty()
    }

}
