package org.eclipse.slm.service_management.service.rest.service_instances

import java.util.*

class AvailableServiceInstanceVersionChange(

    val serviceOfferingVersionId: UUID,

    val version: String,

    val versionDate: Date,

    val changeType: AvailableServiceInstanceVersionChangeType
) {
}
