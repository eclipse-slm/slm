package org.eclipse.slm.common.consul.model.acl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/** Represents an ACL role in Consul
 *
 *  For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/roles#json-request-body-schema">Consul API docs</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Role(

    /** Policy identifier. */
    @field:JsonProperty("ID", required = false)
    val id: String? = null,

    /** Specifies a name for the ACL role. The name can contain alphanumeric characters, dashes -, and underscores _. This name must be unique. */
    @field:JsonProperty("Name")
    var name: String = "",

    /** Free form human readable description of the role. */
    @field:JsonProperty("Description")
    var description: String? = null,

    /** The list of policies that should be applied to the role. A PolicyLink is an object with an "ID" and/or "Name" field to specify a policy. With the
     *  PolicyLink, roles can be linked to policies either by the policy name or by the policy ID. When policies are linked by name they will be internally
     *  resolved to the policy ID. With linking roles internally by IDs, Consul enables policy renaming without breaking tokens.
     */
    @field:JsonProperty("Policies", required = false)
    var policies: List<PolicyLink>? = emptyList()
)
