package org.eclipse.slm.common.vault.model.mounts

import com.fasterxml.jackson.annotation.JsonProperty

/** Configuration settings for a Vault secrets engine.
 *
 * https://developer.hashicorp.com/vault/api-docs/system/mounts#configure-mount
 */
data class SecretsEngineConfig(
    @field:JsonProperty("default_lease_ttl")
    val defaultLeaseTtl: String? = null,

    @field:JsonProperty("max_lease_ttl")
    val maxLeaseTtl: String? = null,

    @field:JsonProperty("force_no_cache")
    val forceNoCache: Boolean = false,

    @field:JsonProperty("audit_non_hmac_request_keys")
    val auditNonHmacRequestKeys: List<String>? = null,

    @field:JsonProperty("audit_non_hmac_response_keys")
    val auditNonHmacResponseKeys: List<String>? = null,

    @field:JsonProperty("listing_visibility")
    val listingVisibility: String? = null,

    @field:JsonProperty("passthrough_request_headers")
    val passthroughRequestHeaders: List<String>? = null,

    @field:JsonProperty("allowed_response_headers")
    val allowedResponseHeaders: List<String>? = null
)
