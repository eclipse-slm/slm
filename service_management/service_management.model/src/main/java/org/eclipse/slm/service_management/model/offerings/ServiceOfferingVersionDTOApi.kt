package org.eclipse.slm.service_management.model.offerings

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionCategory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
open class ServiceOfferingVersionDTOApi {

    private val LOG: Logger = LoggerFactory.getLogger(ServiceOfferingVersionDTOApi::class.java)

    @JsonProperty("id", required = false)
    var id: UUID? = null

    @JsonProperty("serviceOfferingId", required = true)
    var serviceOfferingId: UUID? = null

    @JsonProperty("version", required = true)
    var version: String? = null

    @JsonProperty("created", required = false, access = JsonProperty.Access.READ_ONLY)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var created: Date? = Calendar.getInstance().time

    @JsonProperty("serviceOptionCategories", required = false)
    var serviceOptionCategories: List<ServiceOptionCategory> = mutableListOf()

    @JsonProperty("serviceRequirements", required = false)
    var serviceRequirements: List<ServiceRequirement> = mutableListOf()

    @JsonProperty("serviceRepositories", required = false)
    var serviceRepositories: List<UUID> = mutableListOf()

    @JsonProperty("deploymentDefinition", required = true)
    open var deploymentDefinition: DeploymentDefinition? = null

    @JsonProperty("servicePorts", required = false)
    var servicePorts: List<Int> = mutableListOf()
}
