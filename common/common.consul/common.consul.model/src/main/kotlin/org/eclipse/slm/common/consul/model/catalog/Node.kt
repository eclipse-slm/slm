package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * A representation of a node in the Consul catalog.
 * For more information, see the <a href="https://developer.hashicorp.com/consul/api-docs/catalog#sample-response-1">Consul API docs</a>.
 */
open class Node(
    /** The name of the node */
    @field:JsonProperty("Node")
    open var nodeName: String,

    /** The unique ID of the node */
    @field:JsonProperty("ID")
    open var id: UUID? = null,

    /** The address of the node */
    @field:JsonProperty("Address")
    open var address: String? = null,

    /** The datacenter of the node */
    @field:JsonProperty("Datacenter")
    var datacenter: String? = null,

    /** The list of explicit LAN and WAN IP addresses for the node */
    @field:JsonProperty("TaggedAddresses")
    open var taggedAddresses: TaggedAddresses? = null,

    /** User-defined metadata key/value pairs for the node */
    @field:JsonProperty("Meta")
    open var meta: Map<String, String>? = null
) {
    constructor(id: UUID, nodeName: String)
            : this(nodeName, id, null, null, null, null)

    companion object {
        @JvmStatic
        fun builder(node: String) = Builder(node)
    }

    class Builder(private val nodeName: String) {
        private var id: UUID? = null
        private var address: String? = null
        private var datacenter: String? = null
        private var taggedAddresses: TaggedAddresses? = null
        private var meta: Map<String, String>? = null

        fun id(id: UUID?) = apply { this.id = id }
        fun address(address: String?) = apply { this.address = address }
        fun datacenter(datacenter: String?) = apply { this.datacenter = datacenter }
        fun taggedAddresses(taggedAddresses: TaggedAddresses?) = apply { this.taggedAddresses = taggedAddresses }
        fun meta(meta: Map<String, String>?) = apply { this.meta = meta }

        fun build() = Node(
            nodeName = nodeName,
            id = id,
            address = address,
            datacenter = datacenter,
            taggedAddresses = taggedAddresses,
            meta = meta
        )
    }
}