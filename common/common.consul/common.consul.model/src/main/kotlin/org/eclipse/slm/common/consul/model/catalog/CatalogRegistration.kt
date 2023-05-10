package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class CatalogRegistration {
    class Service {
        @JsonProperty("ID")
        var id: UUID? = null

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
        override fun toString(): String {
            return "Service{" +
                    "id='" + id + '\'' +
                    ", service='" + service + '\'' +
                    ", tags=" + tags +
                    ", address='" + address + '\'' +
                    ", meta=" + meta +
                    ", port=" + port +
                    '}'
        }
    }

    enum class CheckStatus {
        UNKNOWN, PASSING, WARNING, CRITICAL
    }

    class Check {
        @JsonProperty("Node")
        var node: String? = null

        @JsonProperty("CheckID")
        var checkId: String? = null

        @JsonProperty("Name")
        var name: String? = null

        @JsonProperty("Notes")
        var notes: String? = null

        @JsonProperty("Status")
        var status: CheckStatus? = null

        @JsonProperty("ServiceID")
        var serviceId: String? = null
        override fun toString(): String {
            return "Check{" +
                    "node='" + node + '\'' +
                    ", checkId='" + checkId + '\'' +
                    ", name='" + name + '\'' +
                    ", notes='" + notes + '\'' +
                    ", status=" + status +
                    ", serviceId='" + serviceId + '\'' +
                    '}'
        }
    }

    @JsonProperty("Datacenter")
    var datacenter: String? = null

    @JsonProperty("Node")
    var node: String? = null

    @JsonProperty("Address")
    var address: String? = null

    @JsonProperty("Service")
    var service: Service? = null

    @JsonProperty("Check")
    var check: Check? = null

    @JsonProperty("NodeMeta")
    var nodeMeta: Map<String, String>? = null

    @JsonProperty("SkipNodeUpdate")
    var isSkipNodeUpdate = false

    @JsonProperty("TaggedAddresses")
    var taggedAddresses: Map<String, String>? = null
    override fun toString(): String {
        return "CatalogRegistration{" +
                "datacenter='" + datacenter + '\'' +
                ", node='" + node + '\'' +
                ", address='" + address + '\'' +
                ", service=" + service +
                ", check=" + check +
                ", nodeMeta=" + nodeMeta +
                ", skipNodeUpdate=" + isSkipNodeUpdate +
                ", taggedAddresses=" + taggedAddresses +
                '}'
    }
}
