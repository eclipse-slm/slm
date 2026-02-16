package org.eclipse.slm.common.vault.model.pki

import com.fasterxml.jackson.annotation.JsonProperty

data class GenerateCACertRequest(

    @field:JsonProperty("common_name")
    val commonName: String,

    @field:JsonProperty("issuer_name")
    val issuerName: String

)