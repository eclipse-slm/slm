package org.eclipse.slm.service_management.model.offerings

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionCategory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
open class ServiceOfferingVersionDTOFileImport {

    private val LOG: Logger = LoggerFactory.getLogger(ServiceOfferingVersionDTOFileImport::class.java)

    @JsonProperty("id", required = false)
    var id: UUID? = null

    @JsonProperty("version", required = true)
    var version: String? = null

    @JsonProperty("serviceOptionCategories", required = false)
    var serviceOptionCategories: List<ServiceOptionCategory> = mutableListOf()

    @JsonProperty("serviceRequirements", required = false)
    var serviceRequirements: List<ServiceRequirement> = mutableListOf()

    @JsonProperty("serviceRepositories", required = false)
    var serviceRepositories: List<UUID> = mutableListOf()

    @JsonProperty("deploymentDefinition", required = true)
    open var deploymentDefinition: DeploymentDefinitionDTOFileImport? = null

    @JsonProperty("servicePorts", required = false)
    var servicePorts: List<Int> = mutableListOf()
}
