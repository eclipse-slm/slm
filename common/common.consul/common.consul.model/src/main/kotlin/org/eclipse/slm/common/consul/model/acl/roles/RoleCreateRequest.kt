package org.eclipse.slm.common.consul.model.acl

import com.fasterxml.jackson.annotation.JsonProperty

/** Request object to create a new ACL role in Consul
 *
 *  For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/roles#json-request-body-schema">Consul API docs</a>
 */
data class RoleCreateRequest(
    @field:JsonProperty("Name")
    /** Specifies a name for the ACL role. The name can contain alphanumeric characters, dashes -, and underscores _. This name must be unique. */
    val name: String,

    @field:JsonProperty("Description")
    /** Free form human readable description of the role. */
    val description: String? = null,

    @field:JsonProperty("Policies")
    /** The list of policies that should be applied to the role. */
    val policies: List<PolicyLink>? = null,

    @field:JsonProperty("Datacenters")
    /** Specifies the datacenters the effective policy is valid within. When no datacenters are provided the effective policy is valid in all datacenters including those which do not yet exist but may in the future. */
    val datacenters: List<String>? = null,

    @field:JsonProperty("Namespace")
    /** <ENTERPRISE FEATURE> Specifies the namespace of the role you create. This field takes precedence over the ns query parameter, one of several other methods to specify the namespace. */
    val namespace: String? = null,

    @field:JsonProperty("Partition")
    /** <ENTERPRISE FEATURE> The admin partition to use. If not provided, the partition is inferred from the request's ACL token, or defaults to the default partition. */
    val partition: String? = null
) {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private var name: String? = null
        private var description: String? = null
        private var policies: List<PolicyLink>? = null
        private var datacenters: List<String>? = null
        private var namespace: String? = null
        private var partition: String? = null

        fun name(name: String) = apply { this.name = name }
        fun description(description: String?) = apply { this.description = description }
        fun policies(policies: List<PolicyLink>?) = apply { this.policies = policies }
        fun datacenters(datacenters: List<String>?) = apply { this.datacenters = datacenters }
        fun namespace(namespace: String?) = apply { this.namespace = namespace }
        fun partition(partition: String?) = apply { this.partition = partition }

        fun build(): RoleCreateRequest {
            val reqName = requireNotNull(name) { "name is required" }
            return RoleCreateRequest(
                name = reqName,
                description = description,
                policies = policies,
                datacenters = datacenters,
                namespace = namespace,
                partition = partition
            )
        }
    }
}