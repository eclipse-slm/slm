package org.eclipse.slm.common.credentials.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
open class CredentialReadDTO(

    @param:JsonProperty(value = "id")
    val id: UUID,

    @param:JsonProperty("name")
    val name: String,

    @param:JsonProperty(value = "scopesRaw")
    open val scopesRaw: List<String>,

    @param:JsonProperty(value = "data")
    val data: CredentialDataReadDTO,

    @param:JsonProperty(value = "entityLinks")
    var entityLinks: List<CredentialEntityLinkReadDTO>? = null
)
