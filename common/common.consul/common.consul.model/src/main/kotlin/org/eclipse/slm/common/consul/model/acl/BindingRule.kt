package org.eclipse.slm.common.consul.model.acl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class BindingRule {
    @JsonProperty("Description")
    var description: String = ""

    @JsonProperty("AuthMethod")
    var authMethod: String = ""

    @JsonProperty("Selector")
    var selector: String = ""

    @JsonProperty("BindType")
    var bindType: String = ""

    @JsonProperty("BindName")
    var bindName: String = ""

    @JsonProperty("ID", required = false)
    var id: String? = null

    @JsonProperty("CreateIndex", required = false)
    var createIndex: Long? = null

    @JsonProperty("ModifyIndex", required = false)
    var modifyIndex: Long? = null

    constructor(){}
    constructor(description: String, authMethod: String, selector: String, bindType: String, bindName: String){
        this.description = description
        this.authMethod = authMethod
        this.selector = selector
        this.bindType = bindType
        this.bindName = bindName
    }
}
