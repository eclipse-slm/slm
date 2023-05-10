package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class CatalogNode {
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

    @JsonProperty("NodeMeta")
    var nodeMeta: Map<String, String>? = null

    @JsonProperty("CreateIndex")
    var createIndex: Long? = null

    @JsonProperty("ModifyIndex")
    var modifyIndex: Long? = null

    @JsonProperty("Service")
    var service: Service? = null

    @JsonProperty("Checks")
    var checks: List<Check> = emptyList()

    @JsonProperty("SkipNodeUpdate")
    var isSkipNodeUpdate = false

    override fun toString(): String {
        return "CatalogNode [ node='$node', services='$service' ]"
    }

    open class Service(id: UUID? = null) {
        @JsonProperty("ID")
        var id: UUID = id ?: UUID.randomUUID()

        @JsonProperty("Address")
        var address: String? = null

        @JsonProperty("Service")
        open var service: String? = null

        @JsonProperty("Tags")
        open var tags: List<String> = ArrayList()

        @JsonProperty("Port")
        var port: Int? = null

        @JsonProperty("Meta")
        open var serviceMeta: Map<String, String> = HashMap()

        override fun toString(): String {
            return "Service { id='$id', service='$service', tags=$tags, address=$address }"
        }
    }

    class Check {
        @JsonProperty("CheckID")
        var checkId: String? = null

        @JsonProperty("Name")
        var name: String? = null

        @JsonProperty("Definition")
        var definition: HttpCheckDefinition? = null

        @JsonProperty("Status", access = JsonProperty.Access.WRITE_ONLY)
        var status: String? = null
    }

    class HttpCheckDefinition () {
        @JsonProperty("HTTP")
        var http: String? = null

        @JsonProperty("Interval")
        var interval: String? = null
    }
}
