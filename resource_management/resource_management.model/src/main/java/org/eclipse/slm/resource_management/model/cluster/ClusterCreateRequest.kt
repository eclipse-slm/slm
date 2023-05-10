package org.eclipse.slm.resource_management.model.cluster

import java.util.*

class ClusterCreateRequest(
    val clusterTypeId: UUID,
)
{
    var skipInstall: Boolean = false

    var clusterMembers: Map<UUID, String> = emptyMap()

    var configParameterValues: Map<String, String> = emptyMap()
}
