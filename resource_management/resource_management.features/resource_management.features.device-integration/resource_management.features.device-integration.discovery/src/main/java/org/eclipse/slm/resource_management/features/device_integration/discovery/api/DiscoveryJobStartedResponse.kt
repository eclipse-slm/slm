package org.eclipse.slm.resource_management.features.device_integration.discovery.api

import java.util.*

class DiscoveryJobStartedResponse {

    var discoveryJobId: UUID? = null

    constructor()

    constructor(discoveryJobId: UUID?) {
        this.discoveryJobId = discoveryJobId
    }

}
