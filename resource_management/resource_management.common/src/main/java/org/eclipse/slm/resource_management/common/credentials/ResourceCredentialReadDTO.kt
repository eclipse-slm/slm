package org.eclipse.slm.resource_management.common.credentials

import com.fasterxml.jackson.annotation.JsonIgnore
import org.eclipse.slm.common.credentials.model.CredentialDataReadDTO
import org.eclipse.slm.common.credentials.model.CredentialReadDTO
import java.util.*

class ResourceCredentialReadDTO : CredentialReadDTO {

    constructor(id: UUID, name: String, scopesRaw: List<String>, credentialData: CredentialDataReadDTO) : super(id, name, scopesRaw, credentialData){
        for (scopeRaw in scopesRaw) {
            val scope = ResourceCredentialScope.valueOf(scopeRaw)
            this.scopes += scope
        }
    }

    var scopes: List<ResourceCredentialScope> = mutableListOf()

    @JsonIgnore
    override var scopesRaw: List<String> = emptyList()
}