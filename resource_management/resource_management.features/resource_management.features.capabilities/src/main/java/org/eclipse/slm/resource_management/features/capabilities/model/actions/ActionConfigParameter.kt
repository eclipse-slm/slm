package org.eclipse.slm.resource_management.features.capabilities.model.actions

import com.fasterxml.jackson.annotation.JsonProperty

class ActionConfigParameter(

    @param:JsonProperty("name")
    val name: String,

    @param:JsonProperty("prettyName")
    val prettyName: String,

    @param:JsonProperty("description")
    val description: String,

    @param:JsonProperty("valueType")
    val valueType: ActionConfigParameterValueType,

    @param:JsonProperty("defaultValue")
    val defaultValue: String?,

    @param:JsonProperty("requiredType")
    val requiredType: ActionConfigParameterRequiredType,

    @param:JsonProperty("secret")
    val secret: Boolean = false,
) {
}
