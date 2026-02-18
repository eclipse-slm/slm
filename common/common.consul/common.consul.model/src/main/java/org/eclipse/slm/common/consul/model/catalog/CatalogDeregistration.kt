package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

/**
 * CatalogDeregistration represents a request to deregister a node, service, or check from the Consul catalog.
 *
 * For more information see <a href=https://developer.hashicorp.com/consul/api-docs/catalog#json-request-body-schema-1>Consul API docs</a>.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CatalogDeregistration(

    /** Specifies the ID of the node. If no other values are provided, this node, all its services, and all its checks are removed. **/
    @field:JsonProperty("Node")
    val node: String,

    /** Specifies the datacenter, which defaults to the agent's datacenter if not provided. **/
    @field:JsonProperty("Datacenter")
    val datacenter: String? = null,

    /** Specifies the ID of the check to remove. **/
    @field:JsonProperty("CheckID")
    val checkId: String? = null,

    /** Specifies the ID of the service to remove. The service and all associated checks will be removed. **/
    @field:JsonProperty("ServiceID")
    val serviceId: UUID? = null,

    /** <ENTERPRISE FEATURE> Specifies the namespace of the service and checks you deregister. This field takes precedence over the ns query parameter, one of
     * several other methods to specify the namespace. You can also specify the namespace in the Service or Check fields; if namespaces are specified in
     * multiple places, they must all be the same.
    */
    @field:JsonProperty("Namespace")
    val namespace: String? = null,

    /** <ENTERPRISE FEATURE> Specifies the admin partition of the service and checks you deregister. */
    @field:JsonProperty("Partition")
    val partition: String? = null
) {
    companion object {
        fun builder(node: String) = Builder(node)
    }

    class Builder(private val node: String) {
        private var datacenter: String? = null
        private var checkId: String? = null
        private var serviceId: UUID? = null
        private var namespace: String? = null
        private var partition: String? = null

        fun datacenter(datacenter: String?) = apply { this.datacenter = datacenter }
        fun checkId(checkId: String?) = apply { this.checkId = checkId }
        fun serviceId(serviceId: UUID?) = apply { this.serviceId = serviceId }
        fun namespace(namespace: String?) = apply { this.namespace = namespace }
        fun partition(partition: String?) = apply { this.partition = partition }

        fun build() = CatalogDeregistration(
            node = node,
            datacenter = datacenter,
            checkId = checkId,
            serviceId = serviceId,
            namespace = namespace,
            partition = partition
        )
    }
}
