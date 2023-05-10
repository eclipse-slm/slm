package org.eclipse.slm.common.consul.model.agent

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class Self {
    enum class LogLevel {
        TRACE, DEBUG, INFO, WARN, ERR
    }

    class Config {
        @JsonProperty("Datacenter")
        var datacenter: String? = null

        @JsonProperty("NodeName")
        var nodeName: String? = null

        @JsonProperty("Revision")
        var revision: String? = null

        @JsonProperty("Server")
        var isServer = false

        @JsonProperty("Version")
        var version: String? = null
        override fun toString(): String {
            return "DebugConfig{" +
                    "datacenter='" + datacenter + "'" +
                    ", nodeName='" + nodeName + "'" +
                    ", revision='" + revision + '\'' +
                    ", server=" + isServer +
                    ", version='" + version + '\'' +
                    '}'
        }
    }

    class DebugConfig {
        @JsonProperty("Bootstrap")
        var isBootstrap = false

        @JsonProperty("DataDir")
        var dataDir: String? = null

        @JsonProperty("DNSRecursor")
        var dnsRecursor: String? = null

        @JsonProperty("DNSDomain")
        var dnsDomain: String? = null

        @JsonProperty("LogLevel")
        var logLevel: LogLevel? = null

        @JsonProperty("NodeID")
        var nodeId: String? = null

        @JsonProperty("ClientAddrs")
        var clientAddresses: Array<String> = emptyArray()

        @JsonProperty("BindAddr")
        var bindAddress: String? = null

        @JsonProperty("LeaveOnTerm")
        var isLeaveOnTerm = false

        @JsonProperty("SkipLeaveOnInt")
        var isSkipLeaveOnInt = false

        @JsonProperty("EnableDebug")
        var isEnableDebug = false

        @JsonProperty("VerifyIncoming")
        var isVerifyIncoming = false

        @JsonProperty("VerifyOutgoing")
        var isVerifyOutgoing = false

        @JsonProperty("CAFile")
        var caFile: String? = null

        @JsonProperty("CertFile")
        var certFile: String? = null

        @JsonProperty("KeyFile")
        var keyFile: String? = null

        @JsonProperty("UiDir")
        var uiDir: String? = null

        @JsonProperty("PidFile")
        var pidFile: String? = null

        @JsonProperty("EnableSyslog")
        var isEnableSyslog = false

        @JsonProperty("RejoinAfterLeave")
        var isRejoinAfterLeave = false
        override fun toString(): String {
            return "Config{" +
                    "bootstrap=" + isBootstrap +
                    ", dataDir='" + dataDir + '\'' +
                    ", dnsRecursor='" + dnsRecursor + '\'' +
                    ", dnsDomain='" + dnsDomain + '\'' +
                    ", logLevel='" + logLevel + '\'' +
                    ", nodeId='" + nodeId + '\'' +
                    ", clientAddresses='" + Arrays.toString(clientAddresses) + '\'' +
                    ", bindAddress='" + bindAddress + '\'' +
                    ", leaveOnTerm=" + isLeaveOnTerm +
                    ", skipLeaveOnInt=" + isSkipLeaveOnInt +
                    ", enableDebug=" + isEnableDebug +
                    ", verifyIncoming=" + isVerifyIncoming +
                    ", verifyOutgoing=" + isVerifyOutgoing +
                    ", caFile='" + caFile + '\'' +
                    ", certFile='" + certFile + '\'' +
                    ", keyFile='" + keyFile + '\'' +
                    ", uiDir='" + uiDir + '\'' +
                    ", pidFile='" + pidFile + '\'' +
                    ", enableSyslog=" + isEnableSyslog +
                    ", rejoinAfterLeave=" + isRejoinAfterLeave +
                    '}'
        }
    }

    @JsonProperty("Config")
    var config: Config? = null

    @JsonProperty("DebugConfig")
    var debugConfig: DebugConfig? = null

    @JsonProperty("Member")
    var member: Member? = null
    override fun toString(): String {
        return "Self{" +
                "config=" + config +
                ", member=" + member +
                '}'
    }
}
