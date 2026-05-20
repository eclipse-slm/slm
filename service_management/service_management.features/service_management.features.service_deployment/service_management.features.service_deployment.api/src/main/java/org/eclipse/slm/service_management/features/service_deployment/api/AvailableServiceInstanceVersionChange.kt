package org.eclipse.slm.service_management.features.service_deployment.api

import java.util.*

class AvailableServiceInstanceVersionChange(

    val serviceOfferingVersionId: UUID,

    val version: String,

    val versionDate: Date,

    val changeType: AvailableServiceInstanceVersionChangeType
) {
}
