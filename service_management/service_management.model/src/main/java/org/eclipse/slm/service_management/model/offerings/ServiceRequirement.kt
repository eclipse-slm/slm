package org.eclipse.slm.service_management.model.offerings

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
