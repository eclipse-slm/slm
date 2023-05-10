package org.eclipse.slm.common.consul.model.acl

import com.fasterxml.jackson.annotation.JsonProperty

class PolicyLink {
    @JsonProperty("Name")
    var name: String = ""
    @JsonProperty("ID", required = false)
    var id: String? = null

    public constructor(){}

    constructor(name: String){
        this.name = name
    }
}
