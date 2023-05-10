package org.eclipse.slm.common.consul.model.acl

import com.fasterxml.jackson.annotation.JsonProperty

class Acl {
    @JsonProperty("CreateIndex")
    var createIndex: Long = 0

    @JsonProperty("ModifyIndex")
    var modifyIndex: Long = 0

    @JsonProperty("ID")
    var id: String? = null

    @JsonProperty("Name")
    var name: String? = null

    @JsonProperty("Type")
    var type: AclType? = null

    @JsonProperty("Rules")
    var rules: String? = null

    public constructor(){}

    override fun toString(): String {
        return "Acl{" +
                "createIndex=" + createIndex +
                ", modifyIndex=" + modifyIndex +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", rules='" + rules + '\'' +
                '}'
    }
}
