package org.eclipse.slm.service_management.model.offerings

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
class ServiceOfferingDTOApi(
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
    private val LOG: Logger = LoggerFactory.getLogger(ServiceOfferingDTOApi::class.java)

    @JsonProperty("id", required = false)
    var id: UUID? = null

    @JsonProperty("coverImage", required = false)
    var coverImage: ByteArray? = null

    @JsonProperty("versions", required = false)
    var versions: MutableList<ServiceOfferingVersionDTOShort> = mutableListOf()

    @JsonIgnore
    var gitRepository: ServiceOfferingGitRepository? = null

    @JsonGetter(value = "scmBased")
    fun isScmBased() : Boolean {
        return gitRepository != null;
    }

    constructor() : this(
        "emptyDefaultConstructor",
        "emptyDefaultConstructor",
        "emptyDefaultConstructor", -1,
        UUID.randomUUID())
}
