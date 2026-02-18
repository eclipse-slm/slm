package org.eclipse.slm.common.consul.model.acl.policies

import com.fasterxml.jackson.annotation.JsonProperty

/** Request object to update an existing ACL policy in Consul
 *
 *  For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/policies#json-request-body-schema-1">Consul API docs</a>
 */
data class PolicyUpdateRequest(
    @field:JsonProperty("ID")
    /** If specified, this field must be an exact match with the id path parameter. */
    val id: String?,

    @field:JsonProperty("Name")
    /** Specifies a name for the ACL policy. The name can contain alphanumeric characters, dashes -, and underscores _. This name must be unique. */
    val name: String,

    @field:JsonProperty("Description")
    /** Free form human readable description of the policy. */
    val description: String? = null,

    @field:JsonProperty("Rules")
    /** Specifies rules for the ACL policy. The format of the Rules property is detailed in the ACL Rules documentation. */
    val rules: String? = null,

    @field:JsonProperty("Datacenters")
    /** Specifies the datacenters the policy is valid within. When no datacenters are provided the policy is valid in all datacenters including those which do not yet exist but may in the future. */
    val datacenters: List<String>? = null,

    @field:JsonProperty("Namespace")
    /** <ENTERPRISE FEATURE> Specifies the namespace of the policy you create. This field takes precedence over the ns query parameter, one of several other methods to specify the namespace. */
    val namespace: String? = null,

    @field:JsonProperty("Partition")
    /** <ENTERPRISE FEATURE> The admin partition to use. If not provided, the partition is inferred from the request's ACL token, or defaults to the default partition. */
    val partition: String? = null
)