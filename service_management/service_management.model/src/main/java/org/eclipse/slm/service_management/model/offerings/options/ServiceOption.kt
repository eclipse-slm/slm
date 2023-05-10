package org.eclipse.slm.service_management.model.offerings.options

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable


open class ServiceOption (

    @JsonProperty("relation")
    var relation: String,

    @JsonProperty("key")
    var key: String,

    @JsonProperty("name")
    var name: String,

    @JsonProperty("description")
    var description: String,

    @JsonProperty("optionType")
    var optionType: ServiceOptionType,

    @JsonProperty("defaultValue")
    var defaultValue: Any?,

    @JsonProperty("valueType")
    var valueType: ServiceOptionValueType,

    @JsonProperty("valueOptions", required = false)
    var valueOptions: List<String>? = listOf(),

    @JsonProperty("required")
    var required: Boolean,

    @JsonProperty("editable")
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
