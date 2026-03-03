package org.eclipse.slm.common.consul.model.acl.roles

import com.fasterxml.jackson.annotation.JsonProperty

data class RoleUpdateRequest(
    @field:JsonProperty("ID")
    /** If specified, this field must be an exact match with the id path parameter. */
    val id: String? = null,

    @field:JsonProperty("Name")
    /** Specifies a name for the ACL role. The name can only contain alphanumeric characters as well as - and _ and must be unique. */
    val name: String,

    @field:JsonProperty("Description")
    /** Free form human readable description of the role. */
    val description: String? = null,

    @field:JsonProperty("Policies")
    /** The list of policies that should be applied to the role. */
    val policies: List<PolicyLink>? = null,

    @field:JsonProperty("Namespace")
    /** <ENTERPRISE FEATURE> Specifies the namespace of the role you update. This field takes precedence over the ns query parameter, one of several other methods to specify the namespace. */
    val namespace: String? = null,

    @field:JsonProperty("Partition")
    /**  <ENTERPRISE FEATURE> The admin partition to use. If not provided, the partition is inferred from the request's ACL token, or defaults to the default partition. */
    val partition: String? = null
)