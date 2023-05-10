package org.eclipse.slm.service_management.model.offerings.responses

import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.service_management.model.offerings.ServiceOffering
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
