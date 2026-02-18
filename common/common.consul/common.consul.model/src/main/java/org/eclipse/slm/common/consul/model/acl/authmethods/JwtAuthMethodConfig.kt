package org.eclipse.slm.common.consul.model.acl.authmethods

import com.fasterxml.jackson.annotation.JsonProperty

/** Configuration for JWT auth method.
 *
 * For more information see <a href="https://developer.hashicorp.com/consul/docs/secure/acl/auth-method/jwt">Consul API Docs</a>
 * */
data class JwtAuthMethodConfig(
    /** Discovery URL for the OIDC provider. */
    @field:JsonProperty("OIDCDiscoveryURL")
    val oidcDiscoveryUrl: String? = null,

    /** Claim mappings from identity token claims to Consul metadata. */
    @field:JsonProperty("ClaimMappings")
    val claimMappings: Map<String, String>? = null,

    /** List claim mappings from identity token claims to Consul metadata lists. */
    @field:JsonProperty("ListClaimMappings")
    val listClaimMappings: Map<String, String>? = null
) : AuthMethodConfig()

