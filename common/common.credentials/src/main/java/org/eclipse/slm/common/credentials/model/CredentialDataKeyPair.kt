package org.eclipse.slm.common.credentials.model

import com.fasterxml.jackson.annotation.JsonProperty

class CredentialDataKeyPair (

    @param:JsonProperty("privateKey")
    val privateKey: String,

    @param:JsonProperty("publicKey")
    val publicKey: String

) : CredentialData (CredentialDataType.KEY_PAIR)