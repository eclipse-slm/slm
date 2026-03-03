package org.eclipse.slm.common.credentials.model

import com.fasterxml.jackson.annotation.JsonProperty

class CredentialDataUsernamePassword (

    @param:JsonProperty("username")
    val username: String,

    @param:JsonProperty("password")
    val password: String,

) : CredentialData (CredentialDataType.USERNAME_PASSWORD)