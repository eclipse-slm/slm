package org.eclipse.slm.common.vault.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class MountConfig(

    @field:JsonProperty("default_lease_ttl")
    val defaultLeaseTtl: Int,

    @field:JsonProperty("force_no_cache")
    val forceNoCache: Boolean,

    @field:JsonProperty("max_lease_ttl")
    val maxLeaseTtl: Int
)