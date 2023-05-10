package org.eclipse.slm.common.consul.model.acl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Policy() {

    @JsonProperty("Name")
    var name: String = ""

    @JsonProperty("Description")
    var description: String = ""

    @JsonProperty("Datacenters")
    var datacenters: List<String> = ArrayList<String>()

    @JsonProperty("ID", required = false)
    var id: String? = null

    @JsonProperty("Rules")
    var rules: String? = null

    @JsonProperty("CreateIndex", required = false)
    var createIndex: Long? = null

    @JsonProperty("ModifyIndex", required = false)
    var modifyIndex: Long? = null

    constructor(name: String, description: String, datacenters: List<String>) : this() {
        this.name = name
        this.description = description
        this.datacenters = datacenters
    }

    constructor(name: String, description: String, rules: String, datacenters: List<String>) : this(name, description, datacenters) {
        this.rules = rules
    }

}
