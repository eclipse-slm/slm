package org.eclipse.slm.common.credentials.model

import com.fasterxml.jackson.annotation.JsonProperty

data class CredentialEntityLinkCreateDTO (

    @param:JsonProperty("entityType")
    val entityType: String,

    @param:JsonProperty("entityId")
    val entityId: String,
)