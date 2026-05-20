package org.eclipse.slm.service_management.features.service_offerings.api.offerings.options

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class ServiceOptionCategory (

    @JsonProperty("id")
    var id: Long,

    @JsonProperty("name")
    var name: String,

    @JsonProperty("serviceOptions")
    var serviceOptions: List<ServiceOption> = ArrayList()

) : Serializable {
}
