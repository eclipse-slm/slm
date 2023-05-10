package org.eclipse.slm.common.consul.model.agent

import com.fasterxml.jackson.annotation.JsonProperty

class Service {
    @JsonProperty("ID")
    var id: String? = null

    @JsonProperty("Service")
    var service: String? = null

    @JsonProperty("Tags")
    var tags: List<String>? = null

    @JsonProperty("Address")
    var address: String? = null

    @JsonProperty("Meta")
    var meta: Map<String, String>? = null

    @JsonProperty("Port")
    var port: Int? = null

    @JsonProperty("EnableTagOverride")
    var enableTagOverride: Boolean? = null

    @JsonProperty("CreateIndex")
    var createIndex: Long? = null

    @JsonProperty("ModifyIndex")
    var modifyIndex: Long? = null
    override fun toString(): String {
        return "Service{" +
                "id='" + id + '\'' +
                ", service='" + service + '\'' +
                ", tags=" + tags +
                ", address='" + address + '\'' +
                ", meta=" + meta +
                ", port=" + port +
                ", enableTagOverride=" + enableTagOverride +
                ", createIndex=" + createIndex +
                ", modifyIndex=" + modifyIndex +
                '}'
    }
}
