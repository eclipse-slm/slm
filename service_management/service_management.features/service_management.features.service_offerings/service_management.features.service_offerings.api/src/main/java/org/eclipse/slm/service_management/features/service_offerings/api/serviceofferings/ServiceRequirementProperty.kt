package org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

class RequirementProperty: Serializable
{
    @JsonProperty("name")
    var name: String? = ""

    @JsonProperty("semanticId")
    var semanticId: String? = ""

    @JsonProperty("value")
    var value: String? = ""

    @JsonProperty("parentSubmodelsSemanticIds")
    var parentSubmodelsSemanticIds: List<String> = ArrayList()
}
