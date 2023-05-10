package org.eclipse.slm.service_management.model.offerings

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
