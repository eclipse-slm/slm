package org.eclipse.slm.service_management.model.offerings

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

class ServiceRequirementLogic: Serializable {
    @JsonProperty("type")
    var type: RequirementLogicType? = null

    @JsonProperty("properties")
    var properties: List<RequirementProperty> = ArrayList()
}
