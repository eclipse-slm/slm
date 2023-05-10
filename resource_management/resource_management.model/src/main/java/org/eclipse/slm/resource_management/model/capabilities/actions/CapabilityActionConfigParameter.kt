package org.eclipse.slm.resource_management.model.capabilities.actions

import com.fasterxml.jackson.annotation.JsonProperty

class CapabilityActionConfigParameter(

    @JsonProperty("name")
    val name: String,

    @JsonProperty("prettyName")
    val prettyName: String,

    @JsonProperty("description")
    val description: String,

    @JsonProperty("valueType")
    val valueType: CapabilityActionConfigParameterValueType,

    @JsonProperty("defaultValue")
    val defaultValue: String?,

    @JsonProperty("requiredType")
    val requiredType: CapabilityActionConfigParameterRequiredType,

    @JsonProperty("secret")
    val secret: Boolean = false,
) {
}
