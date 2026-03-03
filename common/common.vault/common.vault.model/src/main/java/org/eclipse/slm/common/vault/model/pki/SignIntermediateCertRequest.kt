package org.eclipse.slm.common.vault.model.pki

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SignIntermediateCertRequest(

    @field:JsonProperty("csr")
    val csr: String,

    @field:JsonProperty("format")
    val format: String,

    @field:JsonProperty("ttl")
    val ttl: String

)