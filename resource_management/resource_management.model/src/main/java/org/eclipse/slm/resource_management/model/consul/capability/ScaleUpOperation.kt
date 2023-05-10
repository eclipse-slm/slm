package org.eclipse.slm.resource_management.model.consul.capability

import org.eclipse.slm.resource_management.model.cluster.ClusterMemberType
import java.util.*

class ScaleUpOperation(
    resourceId: UUID,

    val clusterMemberType: ClusterMemberType) : ScaleOperation(resourceId)
{
}
