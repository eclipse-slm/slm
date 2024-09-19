package org.eclipse.slm.resource_management.model.cluster

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class ClusterCreateRequest(
    @JsonProperty("clusterTypeId")
    val clusterTypeId: UUID,
)
{
    var skipInstall: Boolean = false

    var clusterMembers: Map<UUID, String> = emptyMap()

    var configParameterValues: Map<String, String> = emptyMap()
}
