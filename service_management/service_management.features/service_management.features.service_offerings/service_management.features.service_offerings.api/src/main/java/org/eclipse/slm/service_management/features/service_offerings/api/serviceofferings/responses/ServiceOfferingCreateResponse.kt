package org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.responses

import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.ServiceOffering
import java.util.*

class ServiceOfferingCreateResponse {

    @JsonProperty("serviceOfferingId")
    var serviceOfferingId: UUID? = null

    constructor() {
    }

    constructor(serviceOffering: ServiceOffering) {
        serviceOfferingId = serviceOffering.id
    }
}
