package org.eclipse.slm.common.vault.model.pki

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SignIntermediateCertResponse(

    @field:JsonProperty("expiration")
    val expiration: String,

    @field:JsonProperty("certificate")
    val certificate: String,

    @field:JsonProperty("issuing_ca")
    val issuingCa: String,

    @field:JsonProperty("ca_chain")
    val caChain: List<String>,

    @field:JsonProperty("serial_number")
    val serialNumber: String

)