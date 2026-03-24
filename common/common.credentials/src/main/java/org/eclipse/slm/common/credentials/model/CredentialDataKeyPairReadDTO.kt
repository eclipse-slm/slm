package org.eclipse.slm.common.credentials.model

import com.fasterxml.jackson.annotation.JsonProperty

class CredentialDataKeyPairReadDTO (

    @param:JsonProperty("publicKey")
    val publicKey: String

) : CredentialDataReadDTO (CredentialDataType.KEY_PAIR)