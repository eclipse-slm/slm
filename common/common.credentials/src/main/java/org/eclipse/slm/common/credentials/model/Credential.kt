package org.eclipse.slm.common.credentials.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

open class Credential(
    @JsonProperty("id")
    id: UUID? = null,

    @param:JsonProperty("name")
    val name: String,

    @param:JsonProperty("scopesRaw")
    open val scopesRaw: List<String>,

    @param:JsonProperty("data")
    val data: CredentialData,
) {
    var id: UUID = id ?: UUID.randomUUID()
}
