package org.eclipse.slm.common.consul.model.agent

import com.fasterxml.jackson.annotation.JsonProperty

class NewCheck {
    @JsonProperty("ID")
    var id: String? = null

    @JsonProperty("Name")
    var name: String? = null

    @JsonProperty("ServiceID")
    var serviceId: String? = null

    @JsonProperty("Notes")
    var notes: String? = null

    @get:Deprecated("Please use Args parameter instead")
    @set:Deprecated("Please use Args parameter instead")
    @JsonProperty("Script")
    @Deprecated("Please use Args parameter instead")
    var script: String? = null

    @JsonProperty("Args")
    var args: List<String>? = null

    @JsonProperty("HTTP")
    var http: String? = null

    @JsonProperty("Method")
    var method: String? = null

    @JsonProperty("Header")
    var header: Map<String, List<String>>? = null

    @JsonProperty("TCP")
    var tcp: String? = null

    @JsonProperty("DockerContainerID")
    var dockerContainerID: String? = null

    @JsonProperty("Shell")
    var shell: String? = null

    @JsonProperty("Interval")
    var interval: String? = null

    @JsonProperty("Timeout")
    var timeout: String? = null

    @JsonProperty("TTL")
    var ttl: String? = null

    @JsonProperty("DeregisterCriticalServiceAfter")
    var deregisterCriticalServiceAfter: String? = null

    @JsonProperty("TLSSkipVerify")
    var tlsSkipVerify: Boolean? = null

    @JsonProperty("Status")
    var status: String? = null

    @JsonProperty("GRPC")
    var grpc: String? = null

    @JsonProperty("GRPCUseTLS")
    var grpcUseTLS: Boolean? = null

    override fun toString(): String {
        return "NewCheck{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", notes='" + notes + '\'' +
                ", script='" + script + '\'' +
                ", args=" + args +
                ", http='" + http + '\'' +
                ", method='" + method + '\'' +
                ", header=" + header +
                ", tcp='" + tcp + '\'' +
                ", dockerContainerID='" + dockerContainerID + '\'' +
                ", shell='" + shell + '\'' +
                ", interval='" + interval + '\'' +
                ", timeout='" + timeout + '\'' +
                ", ttl='" + ttl + '\'' +
                ", deregisterCriticalServiceAfter='" + deregisterCriticalServiceAfter + '\'' +
                ", tlsSkipVerify=" + tlsSkipVerify +
                ", status='" + status + '\'' +
                ", grpc='" + grpc + '\'' +
                ", grpcUseTLS=" + grpcUseTLS +
                '}'
    }
}
