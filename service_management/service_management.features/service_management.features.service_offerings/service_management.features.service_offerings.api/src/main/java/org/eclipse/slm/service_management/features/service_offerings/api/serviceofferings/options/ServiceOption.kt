package org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.options

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable


open class ServiceOption (

    @param:JsonProperty("relation")
    var relation: String,

    @param:JsonProperty("key")
    var key: String,

    @param:JsonProperty("name")
    var name: String,

    @param:JsonProperty("description")
    var description: String,

    @param:JsonProperty("optionType")
    var optionType: ServiceOptionType,

    @param:JsonProperty("defaultValue")
    var defaultValue: Any?,

    @param:JsonProperty("valueType")
    var valueType: ServiceOptionValueType,

    @param:JsonProperty("valueOptions", required = false)
    var valueOptions: List<String>? = listOf(),

    @param:JsonProperty("required")
    var required: Boolean,

    @param:JsonProperty("editable")
    var editable: Boolean,

    ): Serializable {

    constructor(
        relation: String,
        key: String,
        name: String,
        description: String,
        optionType: ServiceOptionType,
        defaultValue: Any?,
        valueType: ServiceOptionValueType,
        required: Boolean,
        editable: Boolean)
    : this(relation, key, name, description, optionType, defaultValue, valueType, listOf<String>(), required, editable)
    {
    }

    @get:JsonIgnore
    val id: String get() {
        return if (this.relation.isEmpty()) {
            this.key
        } else {
            this.relation + "|" + this.key
        }
    }
}
