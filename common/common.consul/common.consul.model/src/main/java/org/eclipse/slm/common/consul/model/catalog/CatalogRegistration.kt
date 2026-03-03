package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

/** CatalogRegistration represents a request to register a node, service, and/or check with the Consul catalog.
 *
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/catalog#json-request-body-schema">Consul API docs</a>.
 */
data class CatalogRegistration(
    /** An optional UUID to assign to the node. This must be a 36-character UUID-formatted string. */
    @field:JsonProperty("ID")
    val id: String? = null,

    /** Specifies the node ID to register. */
    @field:JsonProperty("Node")
    val nodeName: String,

    /** Specifies the address to register. */
    @field:JsonProperty("Address")
    val address: String?,

    /** Specifies the datacenter, which defaults to the agent's datacenter if not provided. */
    @field:JsonProperty("Datacenter")
    val datacenter: String? = null,

    /** Specifies the tagged addresses. */
    @field:JsonProperty("TaggedAddresses")
    val taggedAddresses: TaggedAddresses? = null,

    /** Specifies arbitrary KV metadata pairs for filtering purposes. */
    @field:JsonProperty("NodeMeta")
    val nodeMeta: Map<String, String>? = null,

    /** Contains an object the specifies the service to register. */
    @field:JsonProperty("Service")
    val service: Service? = null,

    /** Specifies to register a check. */
    @field:JsonProperty("Check")
    val check: Check? = null,

    /** Specifies whether to skip updating the node's information in the registration. */
    @field:JsonProperty("SkipNodeUpdate")
    val skipNodeUpdate: Boolean? = null,

    /** <ENTERPRISE FEATURE> Specifies the namespace of the service and checks you register. */
    @field:JsonProperty("Namespace")
    val namespace: String? = null
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var id: String? = null
        private var nodeName: String? = null
        private var address: String? = null
        private var datacenter: String? = null
        private var taggedAddresses: TaggedAddresses? = null
        private var nodeMeta: Map<String, String>? = null
        private var service: Service? = null
        private var check: Check? = null
        private var skipNodeUpdate: Boolean? = null
        private var namespace: String? = null

        fun id(id: String?) = apply { this.id = id }
        fun nodeName(node: String) = apply { this.nodeName = node }
        fun address(address: String) = apply { this.address = address }
        fun datacenter(datacenter: String?) = apply { this.datacenter = datacenter }
        fun taggedAddresses(taggedAddresses: TaggedAddresses?) = apply { this.taggedAddresses = taggedAddresses }
        fun nodeMeta(nodeMeta: Map<String, String>?) = apply { this.nodeMeta = nodeMeta }
        fun service(service: Service?) = apply { this.service = service }
        fun check(check: Check?) = apply { this.check = check }
        fun skipNodeUpdate(skipNodeUpdate: Boolean?) = apply { this.skipNodeUpdate = skipNodeUpdate }
        fun namespace(namespace: String?) = apply { this.namespace = namespace }

        fun build(): CatalogRegistration {
            if (address == null && skipNodeUpdate == false) {
                throw IllegalArgumentException("Either address must be provided or skipNodeUpdate must be set to true")
            }

            return CatalogRegistration(
                id = id,
                nodeName = requireNotNull(nodeName),
                address = address,
                datacenter = datacenter,
                taggedAddresses = taggedAddresses,
                nodeMeta = nodeMeta,
                service = service,
                check = check,
                skipNodeUpdate = skipNodeUpdate,
                namespace = namespace
            )
        }
    }

    /** Represents a service to be registered in the Consul catalog.
     *
     * For more information see <a href=https://developer.hashicorp.com/consul/api-docs/catalog#service>Consul API docs</a> and
     * <a href="https://developer.hashicorp.com/consul/docs/reference/service#name">Consul service definition reference</a>.
     */
    data class Service(
        /** The name of the service to register. Required. */
        @field:JsonProperty("Service")
        val serviceName: String,

        /** An optional ID for the service. */
        @field:JsonProperty("ID")
        val id: UUID? = null,

        /** Optional tags for the service. */
        @field:JsonProperty("Tags")
        val tags: List<String>? = null,

        /** Optional address for the service. */
        @field:JsonProperty("Address")
        val address: String? = null,

        /** Optional metadata for the service. */
        @field:JsonProperty("Meta")
        val meta: Map<String, String>? = null,

        /** Optional port for the service. */
        @field:JsonProperty("Port")
        val port: Int? = null
    ) {
        companion object {
            @JvmStatic
            fun builder(serviceName: String) = Builder(serviceName)
        }

        class Builder(private val serviceName: String) {
            private var id: UUID? = null
            private var tags: List<String>? = null
            private var address: String? = null
            private var meta: Map<String, String>? = null
            private var port: Int? = null

            fun id(id: UUID?) = apply { this.id = id }
            fun tags(tags: List<String>?) = apply { this.tags = tags }
            fun address(address: String?) = apply { this.address = address }
            fun meta(meta: Map<String, String>?) = apply { this.meta = meta }
            fun port(port: Int?) = apply { this.port = port }

            fun build() = Service(
                serviceName = serviceName,
                id = id,
                tags = tags,
                address = address,
                meta = meta,
                port = port
            )
        }
    }

    /** Represents a health check associated with a service or node in the Consul catalog.
     *
     * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/catalog#check">Consul API docs</a> and
     * <a href="https://developer.hashicorp.com/consul/docs/register/health-check/vm">Consul health check definition</a>.
     */
    data class Check(
        /** The name of the check. Required. */
        @field:JsonProperty("Name")
        val name: String,

        /** The ID of the check. Defaults to the value of Name if omitted. */
        @field:JsonProperty("CheckID")
        val checkId: String? = null,

        /** Human-readable notes for the check. */
        @field:JsonProperty("Notes")
        val notes: String? = null,

        /** The status of the check. Must be one of passing, warning, or critical. */
        @field:JsonProperty("Status")
        val status: String? = null,

        /** Optional definition for TCP and HTTP health checks. */
        @field:JsonProperty("Definition")
        val definition: Map<String, Any>? = null,

        /** Optional ServiceID for service level health check. */
        @field:JsonProperty("ServiceID")
        val serviceId: String? = null
    )
}
