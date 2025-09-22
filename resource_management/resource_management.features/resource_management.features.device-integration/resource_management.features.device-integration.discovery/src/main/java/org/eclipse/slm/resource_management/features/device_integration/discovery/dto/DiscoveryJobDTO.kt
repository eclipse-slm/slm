package org.eclipse.slm.resource_management.features.device_integration.discovery.dto

import org.eclipse.slm.resource_management.features.device_integration.discovery.model.DiscoveredResource
import org.eclipse.slm.resource_management.features.device_integration.discovery.model.DiscoveryJobState
import java.util.Date
import java.util.UUID

open class DiscoveryJobDTO(
    open var id: UUID,

    open var driverId: String,

    open var startDate: Date,

    open var finishDate: Date?,

    open var state: DiscoveryJobState,

    open var discoveryResult: List<DiscoveredResource> = emptyList()
) {
}