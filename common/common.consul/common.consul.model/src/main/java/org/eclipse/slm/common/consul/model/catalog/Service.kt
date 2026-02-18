package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/** Represents a service registered in the Consul catalog.
 *
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/catalog#sample-response-3">Consul API docs</a>.
 */
open class Service (
    /** The unique ID of the node */
    @field:JsonProperty("ID")
    var nodeId: UUID?,

    /** The name of the Consul node on which the service is registered */
    @field:JsonProperty("Node")
    val nodeName: String? = null,

    /** The IP address of the Consul node on which the service is registered */
    @field:JsonProperty("Address")
    var address: String? = null,

    /** The data center of the Consul node on which the service is registered */
    @field:JsonProperty("Datacenter")
    val datacenter: String? = null,

    /** The list of explicit LAN and WAN IP addresses for the agent */
    @field:JsonProperty("TaggedAddresses")
    val taggedAddresses: TaggedAddresses? = null,

    /** User-defined metadata key/value pairs for the node */
    @field:JsonProperty("NodeMeta")
    val nodeMeta: Map<String, String>? = null,

    /** The IP address of the service host — if empty, node address should be used */
    @field:JsonProperty("ServiceAddress")
    var serviceAddress: String? = null,

    /** Indicates whether service tags can be overridden on this service */
    @field:JsonProperty("ServiceEnableTagOverride")
    val serviceEnableTagOverride: Boolean? = null,

    /** Unique service instance identifier */
    @field:JsonProperty("ServiceID")
    val serviceId: UUID? = null,

    /** Name of the service */
    @field:JsonProperty("ServiceName")
    open val serviceName: String? = null,

    /** List of user-defined metadata key/value pairs for the service */
    @field:JsonProperty("ServiceMeta")
    open var serviceMeta: Map<String, String> = emptyMap(),

    /** The port number of the service */
    @field:JsonProperty("ServicePort")
    var servicePort: Int? = null,

    /** List of tags for the service */
    @field:JsonProperty("ServiceTags")
    open var serviceTags: List<String> = emptyList(),

    /** Map of explicit LAN and WAN addresses for the service instance (address and port) */
    @field:JsonProperty("ServiceTaggedAddresses")
    var serviceTaggedAddresses: TaggedAddresses? = null,

    /** The kind of service, usually "" */
    @field:JsonProperty("ServiceKind")
    var serviceKind: String? = null,

    /** The proxy config as specified in service mesh Proxies */
    @field:JsonProperty("ServiceProxy")
    var serviceProxy: ServiceProxy? = null,

    /** The service mesh settings */
    @field:JsonProperty("ServiceConnect")
    var serviceConnect: ServiceConnect? = null,

    /** The Consul Enterprise namespace of this service instance */
    @field:JsonProperty("Namespace")
    var namespace: String? = null
) {
    constructor(serviceId: UUID) : this(
        nodeId = null,
        nodeName = null,
        address = null,
        datacenter = null,
        taggedAddresses = null,
        nodeMeta = null,
        serviceAddress = null,
        serviceEnableTagOverride = null,
        serviceId = serviceId,
        serviceName = null,
        serviceMeta = emptyMap(),
        servicePort = null,
        serviceTags = emptyList(),
        serviceTaggedAddresses = null,
        serviceKind = null,
        serviceProxy = null,
        serviceConnect = null,
        namespace = null
    )

    class Builder(
    ) {
        private var nodeId: UUID? = null
        private var nodeName: String? = null
        private var address: String? = null
        private var datacenter: String? = null
        private var taggedAddresses: TaggedAddresses? = null
        private var nodeMeta: Map<String, String>? = null
        private var serviceAddress: String? = null
        private var serviceEnableTagOverride: Boolean? = null
        private var serviceId: UUID? = null
        private var serviceName: String? = null
        private var serviceMeta: Map<String, String>? = null
        private var servicePort: Int? = null
        private var serviceTags: List<String>? = null
        private var serviceTaggedAddresses: TaggedAddresses? = null
        private var serviceKind: String? = null
        private var serviceProxy: ServiceProxy? = null
        private var serviceConnect: ServiceConnect? = null
        private var namespace: String? = null

        fun nodeId(nodeId: UUID?) = apply { this.nodeId = nodeId }
        fun nodeName(nodeName: String?) = apply { this.nodeName = nodeName }
        fun address(address: String?) = apply { this.address = address }
        fun datacenter(datacenter: String?) = apply { this.datacenter = datacenter }
        fun taggedAddresses(taggedAddresses: TaggedAddresses?) = apply { this.taggedAddresses = taggedAddresses }
        fun nodeMeta(nodeMeta: Map<String, String>?) = apply { this.nodeMeta = nodeMeta }
        fun serviceAddress(serviceAddress: String?) = apply { this.serviceAddress = serviceAddress }
        fun serviceEnableTagOverride(serviceEnableTagOverride: Boolean?) = apply { this.serviceEnableTagOverride = serviceEnableTagOverride }
        fun serviceId(serviceId: UUID?) = apply { this.serviceId = serviceId }
        fun serviceName(serviceName: String?) = apply { this.serviceName = serviceName }
        fun serviceMeta(serviceMeta: Map<String, String>?) = apply { this.serviceMeta = serviceMeta }
        fun servicePort(servicePort: Int?) = apply { this.servicePort = servicePort }
        fun serviceTags(serviceTags: List<String>?) = apply { this.serviceTags = serviceTags }
        fun serviceTaggedAddresses(serviceTaggedAddresses: TaggedAddresses?) = apply { this.serviceTaggedAddresses = serviceTaggedAddresses }
        fun serviceKind(serviceKind: String?) = apply { this.serviceKind = serviceKind }
        fun serviceProxy(serviceProxy: ServiceProxy?) = apply { this.serviceProxy = serviceProxy }
        fun serviceConnect(serviceConnect: ServiceConnect?) = apply { this.serviceConnect = serviceConnect }
        fun namespace(namespace: String?) = apply { this.namespace = namespace }

        fun build() = Service(
            nodeId,
            nodeName,
            address,
            datacenter,
            taggedAddresses,
            nodeMeta,
            serviceAddress,
            serviceEnableTagOverride,
            serviceId,
            serviceName,
            serviceMeta ?: emptyMap(),
            servicePort,
            serviceTags ?: emptyList(),
            serviceTaggedAddresses,
            serviceKind,
            serviceProxy,
            serviceConnect,
            namespace
        )
    }
}
