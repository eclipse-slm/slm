package org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.service_management.features.service_offerings.api.offeringversions.ServiceOfferingVersionDTOFileImport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class ServiceOfferingDTOFileImport(
    @JsonProperty("name", required = true)
    var name: String,

    @JsonProperty("description", required = true)
    var description: String,

    @JsonProperty("shortDescription", required = true)
    var shortDescription: String,

    @JsonProperty("serviceCategoryName", required = true)
    var serviceCategoryName : String,

    @JsonProperty("serviceVendorId", required = true)
    var serviceVendorId: UUID
) {
    private val LOG: Logger = LoggerFactory.getLogger(ServiceOfferingDTOFileImport::class.java)

    @JsonProperty("id", required = false)
    var id: UUID? = null

    @JsonProperty("coverImageFilename", required = false)
    var coverImageFilename: String? = null

    @JsonProperty("version", required = false)
    var version: ServiceOfferingVersionDTOFileImport? = null

    constructor() : this(
        "emptyDefaultConstructor",
        "emptyDefaultConstructor",
        "emptyDefaultConstructor",
        "emptyDefaultConstructor",
        UUID.randomUUID())
}
