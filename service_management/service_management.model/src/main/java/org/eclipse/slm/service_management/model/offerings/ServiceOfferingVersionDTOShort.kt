package org.eclipse.slm.service_management.model.offerings

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
class ServiceOfferingVersionDTOShort {

    private val LOG: Logger = LoggerFactory.getLogger(ServiceOfferingVersionDTOShort::class.java)

    @JsonProperty("id", required = false)
    var id: UUID? = null

    @JsonProperty("version", required = true)
    var version: String? = null

    @JsonProperty("created", required = false)
    var created: Date = Calendar.getInstance().time

}
