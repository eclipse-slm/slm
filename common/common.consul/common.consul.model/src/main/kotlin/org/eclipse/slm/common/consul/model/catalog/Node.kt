package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

open class Node {
    @JsonProperty("ID")
    var id: UUID? = null

    @JsonProperty("Node")
    var node: String? = null

    @JsonProperty("Address")
    var address: String? = null

    @JsonProperty("Datacenter")
    var datacenter: String? = null

    @JsonProperty("TaggedAddresses")
    var taggedAddresses: TaggedAddresses? = null

    @JsonProperty("Meta")
    open var meta: Map<String, String>? = null

    @JsonProperty("CreateIndex")
    var createIndex: Long? = null

    @JsonProperty("ModifyIndex")
    var modifyIndex: Long? = null
    override fun toString(): String {
        return "Node{" +
                "id='" + id + '\'' +
                ", node='" + node + '\'' +
                ", address='" + address + '\'' +
                ", datacenter='" + datacenter + '\'' +
                ", taggedAddresses=" + taggedAddresses +
                ", meta=" + meta +
                ", createIndex=" + createIndex +
                ", modifyIndex=" + modifyIndex +
                '}'
    }
}

