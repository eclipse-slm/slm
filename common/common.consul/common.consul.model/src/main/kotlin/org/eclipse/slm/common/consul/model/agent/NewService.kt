package org.eclipse.slm.common.consul.model.agent

import com.fasterxml.jackson.annotation.JsonProperty

class NewService {
    class Check {
        @JsonProperty("Script")
        var script: String? = null

        @JsonProperty("DockerContainerID")
        var dockerContainerID: String? = null

        @JsonProperty("Shell")
        var shell: String? = null

        @JsonProperty("Interval")
        var interval: String? = null

        @JsonProperty("TTL")
        var ttl: String? = null

        @JsonProperty("HTTP")
        var http: String? = null

        @JsonProperty("Method")
        var method: String? = null

        @JsonProperty("Header")
        var header: Map<String, List<String>>? = null

        @JsonProperty("TCP")
        var tcp: String? = null

        @JsonProperty("Timeout")
        var timeout: String? = null

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
            return "Check{" +
                    "script='" + script + '\'' +
                    ", dockerContainerID='" + dockerContainerID + '\'' +
                    ", shell='" + shell + '\'' +
                    ", interval='" + interval + '\'' +
                    ", ttl='" + ttl + '\'' +
                    ", http='" + http + '\'' +
                    ", method='" + method + '\'' +
                    ", header=" + header +
                    ", tcp='" + tcp + '\'' +
                    ", timeout='" + timeout + '\'' +
                    ", deregisterCriticalServiceAfter='" + deregisterCriticalServiceAfter + '\'' +
                    ", tlsSkipVerify=" + tlsSkipVerify +
                    ", status='" + status + '\'' +
                    ", grpc='" + grpc + '\'' +
                    ", grpcUseTLS=" + grpcUseTLS +
                    '}'
        }
    }

    @JsonProperty("ID")
    var id: String? = null

    @JsonProperty("Name")
    var name: String? = null

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

    @JsonProperty("Check")
    var check: Check? = null

    @JsonProperty("Checks")
    var checks: List<Check>? = null
    override fun toString(): String {
        return "NewService{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", tags=" + tags +
                ", address='" + address + '\'' +
                ", meta=" + meta +
                ", port=" + port +
                ", enableTagOverride=" + enableTagOverride +
                ", check=" + check +
                ", checks=" + checks +
                '}'
    }
}
