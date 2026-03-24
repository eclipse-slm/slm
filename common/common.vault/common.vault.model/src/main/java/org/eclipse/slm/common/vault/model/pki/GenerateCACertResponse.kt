package org.eclipse.slm.common.vault.model.pki

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class GenerateCACertResponse(

    @field:JsonProperty("csr")
    val csr: String?,

    @field:JsonProperty("private_key")
    val privateKey: String?,

    @field:JsonProperty("private_key_type")
    val privateKeyType: String?

)