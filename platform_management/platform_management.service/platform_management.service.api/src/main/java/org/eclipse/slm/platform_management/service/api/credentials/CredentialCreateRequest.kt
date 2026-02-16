package org.eclipse.slm.platform_management.service.api

import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.common.credentials.model.Credential
import org.eclipse.slm.common.credentials.model.CredentialEntityLinkCreateDTO

data class CredentialCreateRequest(

    @param:JsonProperty("entityLinks")
    var entityLinks: List<CredentialEntityLinkCreateDTO>,

    @param:JsonProperty("credential")
    var credential: Credential,

    @param:JsonProperty("fullPathOwnerGroupId")
    val fullPathOwnerGroupId: String
) {
}