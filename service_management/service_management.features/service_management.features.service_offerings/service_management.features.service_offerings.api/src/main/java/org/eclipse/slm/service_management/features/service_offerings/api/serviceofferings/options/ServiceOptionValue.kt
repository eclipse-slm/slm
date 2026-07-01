package org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.options

import com.fasterxml.jackson.annotation.JsonProperty

data class ServiceOptionValue (

    @JsonProperty("serviceOptionId")
    val serviceOptionId: String,

    @JsonProperty("value")
    var value: Any
)
{
}
