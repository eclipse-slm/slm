package org.eclipse.slm.common.consul.model.agent

import com.fasterxml.jackson.annotation.JsonProperty

class Check {
    enum class CheckStatus {
        UNKNOWN, PASSING, WARNING, CRITICAL
    }

    @JsonProperty("Node")
    var node: String? = null

    @JsonProperty("CheckID")
    var checkId: String? = null

    @JsonProperty("Name")
    var name: String? = null

    @JsonProperty("Status")
    var status: CheckStatus? = null

    @JsonProperty("Notes")
    var notes: String? = null

    @JsonProperty("Output")
    var output: String? = null

    @JsonProperty("ServiceID")
    var serviceId: String? = null

    @JsonProperty("ServiceName")
    var serviceName: String? = null

    @JsonProperty("ServiceTags")
    var serviceTags: List<String>? = null

    @JsonProperty("CreateIndex")
    var createIndex: Long? = null

    @JsonProperty("ModifyIndex")
    var modifyIndex: Long? = null
    override fun toString(): String {
        return "Check{" +
                "node='" + node + '\'' +
                ", checkId='" + checkId + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", notes='" + notes + '\'' +
                ", output='" + output + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", serviceTags=" + serviceTags +
                ", createIndex=" + createIndex +
                ", modifyIndex=" + modifyIndex +
                '}'
    }
}
