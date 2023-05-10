package org.eclipse.slm.service_management.model.offerings

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
class ServiceOfferingCreateOrUpdateRequest(

    @JsonProperty("name", required = true)
    var name: String,

    @JsonProperty("description", required = true)
    var description: String,

    @JsonProperty("shortDescription", required = true)
    var shortDescription: String,

    @JsonProperty("serviceCategoryId", required = true)
    var serviceCategoryId : Long,

    @JsonProperty("serviceVendorId", required = true)
    var serviceVendorId: UUID
) {
    @JsonProperty("id", required = false)
    var id: UUID? = null

    @JsonProperty("coverImage", required = false)
    var coverImage: ByteArray? = null

    @JsonIgnore
    var gitRepository: ServiceOfferingGitRepository? = null

    constructor() : this("", "", "", -1, UUID.randomUUID())
}
