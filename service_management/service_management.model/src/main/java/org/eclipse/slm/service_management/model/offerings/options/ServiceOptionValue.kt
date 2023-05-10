package org.eclipse.slm.service_management.model.offerings.options

import com.fasterxml.jackson.annotation.JsonProperty

data class ServiceOptionValue (

    @JsonProperty("serviceOptionId")
    val serviceOptionId: String,

    @JsonProperty("value")
    var value: Any
)
{
}
