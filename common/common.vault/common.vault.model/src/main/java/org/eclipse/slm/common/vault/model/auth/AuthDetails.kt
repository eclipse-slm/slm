package org.eclipse.slm.common.vault.model.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AuthDetails(
    @field:JsonProperty("client_token")
    val clientToken: String,

    @field:JsonProperty("accessor")
    val accessor: String,

    @field:JsonProperty("policies")
    val policies: List<String>,

    @field:JsonProperty("lease_duration")
    val leaseDuration: Int,

    @field:JsonProperty("renewable")
    val renewable: Boolean
)