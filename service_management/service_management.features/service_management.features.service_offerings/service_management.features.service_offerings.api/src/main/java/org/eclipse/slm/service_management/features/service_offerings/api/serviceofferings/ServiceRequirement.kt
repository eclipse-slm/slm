package org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

class ServiceRequirement : Serializable {

    @JsonProperty("key")
    var key : String? = ""

    @JsonProperty("name")
    var name: String? = ""

    @JsonProperty("logics")
    var logics: List<ServiceRequirementLogic> = ArrayList()
}
