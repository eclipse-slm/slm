package org.eclipse.slm.resource_management.model.resource

import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.resource_management.model.capabilities.Capability
import org.eclipse.slm.resource_management.model.capabilities.DeploymentCapability
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityService
import java.util.*

class BasicResource
(
    @JsonProperty("id")
    var id: UUID
)
{
    var assetId: String? = null

    var location: Location? = null

    var ip: String? = null

    var hostname: String? = null

    var firmwareVersion: String? = null;

    var project: String? = null

    var clusterMember = false

    var capabilityServices: List<CapabilityService>? = null

    var remoteAccessService: RemoteAccessService? = null

    constructor(id: UUID, hostname: String, ip: String) : this(id) {
        this.hostname = hostname;
        this.ip = ip;
    }
}
