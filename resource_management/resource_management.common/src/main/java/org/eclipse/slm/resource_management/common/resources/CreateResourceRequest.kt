package org.eclipse.slm.resource_management.common.resources

import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3

class CreateResourceRequest(

    @param:JsonProperty("resourceHostname")
    val resourceHostname: String,

    @param:JsonProperty("resourceIp")
    val resourceIp: String,

    @param:JsonProperty("digitalNameplateV3")
    val digitalNameplateV3: DigitalNameplateV3?  = null,

    @param:JsonProperty("fullPathOwnerGroupId")
    val fullPathOwnerGroupId: String

) {
}