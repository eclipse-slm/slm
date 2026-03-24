package org.eclipse.slm.resource_management.features.capabilities.clusters

import org.eclipse.slm.resource_management.features.capabilities.clusters.model.ClusterMemberType
import java.util.*

class ScaleUpOperation(
    resourceId: UUID,

    val clusterMemberType: ClusterMemberType
) : ScaleOperation(resourceId)
{
}
