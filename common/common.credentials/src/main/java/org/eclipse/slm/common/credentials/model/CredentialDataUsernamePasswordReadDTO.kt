package org.eclipse.slm.common.credentials.model

import com.fasterxml.jackson.annotation.JsonProperty

class CredentialDataUsernamePasswordReadDTO (

    @param:JsonProperty("username")
    val username: String,

    ) : CredentialDataReadDTO (CredentialDataType.USERNAME_PASSWORD)