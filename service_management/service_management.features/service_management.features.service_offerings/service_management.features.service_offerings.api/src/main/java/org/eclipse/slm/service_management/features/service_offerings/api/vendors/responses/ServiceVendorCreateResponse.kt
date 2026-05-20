package org.eclipse.slm.service_management.features.service_offerings.api.vendors.responses

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class ServiceVendorCreateResponse (

    @JsonProperty("serviceVendorId")
    val serviceVendorId: UUID

)
{

}
