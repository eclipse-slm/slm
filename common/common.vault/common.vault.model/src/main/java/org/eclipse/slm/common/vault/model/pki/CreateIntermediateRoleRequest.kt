package org.eclipse.slm.common.vault.model.pki

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreateIntermediateRoleRequest(
    @field:JsonProperty("allowed_domains")
    val allowedDomains: List<String>? = null,

    @field:JsonProperty("allow_any_name")
    val allowAnyName: Boolean,

    @field:JsonProperty("issuer_ref")
    val issuerRef: String,

    @field:JsonProperty("max_ttl")
    val maxTtl: String
)