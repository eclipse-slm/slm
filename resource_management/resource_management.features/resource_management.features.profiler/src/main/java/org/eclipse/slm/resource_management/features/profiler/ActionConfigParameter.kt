package org.eclipse.slm.resource_management.features.profiler

import com.fasterxml.jackson.annotation.JsonProperty

class ActionConfigParameter(

    @JsonProperty("name")
    val name: String,

    @JsonProperty("prettyName")
    val prettyName: String,

    @JsonProperty("description")
    val description: String,

    @JsonProperty("valueType")
    val valueType: ActionConfigParameterValueType,

    @JsonProperty("defaultValue")
    val defaultValue: String?,

    @JsonProperty("requiredType")
    val requiredType: ActionConfigParameterRequiredType,

    @JsonProperty("secret")
    val secret: Boolean = false,
) {
}
