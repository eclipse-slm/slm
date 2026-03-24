package org.eclipse.slm.resource_management.common.resources

import com.fasterxml.jackson.annotation.JsonProperty

data class ResourceUpdateRequest(
    @field:JsonProperty("product")
    val product: String? = null,
    @field:JsonProperty("vendor")
    val vendor: String? = null,
    @field:JsonProperty("hostname")
    val hostname: String? = null,
    @field:JsonProperty("ip")
    val ip: String? = null,
    @field:JsonProperty("assetId")
    val assetId: String? = null
)
