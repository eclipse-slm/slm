package org.eclipse.slm.common.consul.model.agent

import com.fasterxml.jackson.annotation.JsonProperty

class Member {
    @JsonProperty("Name")
    var name: String? = null

    @JsonProperty("Addr")
    var address: String? = null

    @JsonProperty("Port")
    var port: Int? = null

    @JsonProperty("Tags")
    var tags: Map<String, String>? = null

    @JsonProperty("Status")
    var status = 0

    @JsonProperty("ProtocolMin")
    var protocolMin = 0

    @JsonProperty("ProtocolMax")
    var protocolMax = 0

    @JsonProperty("ProtocolCur")
    var protocolCur = 0

    @JsonProperty("DelegateMin")
    var delegateMin = 0

    @JsonProperty("DelegateMax")
    var delegateMax = 0

    @JsonProperty("DelegateCur")
    var delegateCur = 0
    override fun toString(): String {
        return "Member{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", tags=" + tags +
                ", status=" + status +
                ", protocolMin=" + protocolMin +
                ", protocolMax=" + protocolMax +
                ", protocolCur=" + protocolCur +
                ", delegateMin=" + delegateMin +
                ", delegateMax=" + delegateMax +
                ", delegateCur=" + delegateCur +
                '}'
    }
}
