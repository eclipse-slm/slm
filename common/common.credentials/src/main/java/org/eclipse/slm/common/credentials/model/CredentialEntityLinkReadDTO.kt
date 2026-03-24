package org.eclipse.slm.common.credentials.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import java.util.*

data class CredentialEntityLinkReadDTO (

    @param:JsonProperty("entityType")
    val entityType: String,

    @param:JsonProperty("entityId")
    val entityId: String,

)