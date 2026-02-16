package org.eclipse.slm.common.consul.model.acl.authmethods

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/** Request to create or update an ACL auth method (e.g., OIDC).
 *
 *  For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/auth-methods#json-request-body-schema">Consul API Docs</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AuthMethodRequest(
    /** Specifies a name for the ACL auth method. */
    @field:JsonProperty("Name")
    val name: String,

    /** Specifies the auth method type (e.g., "oidc"). */
    @field:JsonProperty("Type")
    val type: String? = null,

    /** Free form human readable description of the auth method. */
    @field:JsonProperty("Description")
    val description: String? = null,

    /** Configuration object for the auth method. */
    @field:JsonProperty("Config")
    val config: AuthMethodConfig? = null,

    /** Maximum token time-to-live for tokens issued by this auth method. */
    @field:JsonProperty("MaxTokenTTL")
    val maxTokenTtl: String? = null
)

