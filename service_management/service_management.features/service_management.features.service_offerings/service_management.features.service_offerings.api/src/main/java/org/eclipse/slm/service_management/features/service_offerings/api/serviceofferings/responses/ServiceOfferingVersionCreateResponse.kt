package org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.responses

import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions.ServiceOfferingVersion
import java.util.*

class ServiceOfferingVersionCreateResponse {

    @JsonProperty("serviceOfferingId")
    var serviceOfferingId: UUID? = null

    @JsonProperty("serviceOfferingVersionId")
    var serviceOfferingVersionId: UUID? = null

    constructor() {
    }

    constructor(serviceOfferingVersion: ServiceOfferingVersion) {
        serviceOfferingId = serviceOfferingVersion.serviceOffering!!.id
        serviceOfferingVersionId = serviceOfferingVersion.id
    }
}
