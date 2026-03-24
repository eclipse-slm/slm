package org.eclipse.slm.resource_management.common.credentials

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.common.credentials.model.Credential
import org.eclipse.slm.common.credentials.model.CredentialData
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ResourceCredential(
    @JsonProperty(value = "id")
    id: UUID? = null,

    @JsonProperty("name")
    name: String,

    @param:JsonProperty(value = "scopes")
    val scopes: List<ResourceCredentialScope>,

    @JsonProperty(value = "data")
    credentialData: CredentialData,
) : Credential(id, name, scopes.map { it.name }, credentialData) {

    override var scopesRaw: List<String> = scopes.map { it.name }
}