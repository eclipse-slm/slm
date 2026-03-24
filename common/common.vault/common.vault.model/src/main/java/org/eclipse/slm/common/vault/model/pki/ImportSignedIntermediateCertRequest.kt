package org.eclipse.slm.common.vault.model.pki

import com.fasterxml.jackson.annotation.JsonProperty

data class ImportSignedIntermediateCertRequest(

    @field:JsonProperty("certificate")
    val certificate: String

)