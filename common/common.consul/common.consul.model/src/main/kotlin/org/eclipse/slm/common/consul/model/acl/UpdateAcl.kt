package org.eclipse.slm.common.consul.model.acl

import com.fasterxml.jackson.annotation.JsonProperty

class UpdateAcl {
    @JsonProperty("ID")
    var id: String? = null

    @JsonProperty("Name")
    var name: String? = null

    @JsonProperty("Type")
    var type: AclType? = null

    @JsonProperty("Rules")
    var rules: String? = null
    override fun toString(): String {
        return "UpdateAcl{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", rules='" + rules + '\'' +
                '}'
    }
}
