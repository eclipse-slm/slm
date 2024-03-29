package org.eclipse.slm.service_management.model.offerings

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import com.vladmihalcea.hibernate.type.json.JsonStringType
import org.eclipse.slm.common.model.DeploymentType
import org.eclipse.slm.service_management.model.AbstractBaseEntityUuid
import org.eclipse.slm.service_management.model.exceptions.ServiceOptionNotFoundException
import org.eclipse.slm.service_management.model.offerings.options.ServiceOption
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionCategory
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionType
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionValue
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.persistence.*
import kotlin.jvm.Transient

@Entity
@Table(name = "service_offering_version", uniqueConstraints = [
    UniqueConstraint(
        name = "UniqueVersionNamePerServiceOffering",
        columnNames = ["version", "service_offering_id"]
    )])
@TypeDefs(TypeDef(name = "json", typeClass = JsonStringType::class), TypeDef(name = "jsonb", typeClass = JsonBinaryType::class))
@DiscriminatorColumn(name = "deployment_type", discriminatorType = DiscriminatorType.STRING)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ServiceOfferingVersion(
    id: UUID? = null,
    serviceOffering: ServiceOffering?,
    version: String,
    deploymentDefinition: DeploymentDefinition?
)
    : AbstractBaseEntityUuid(id) {
    constructor() : this(null, null, "", null) {
    }

    @Transient
    private val LOG: Logger = LoggerFactory.getLogger(ServiceOfferingVersion::class.java)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="service_offering_id", nullable = false)
    var serviceOffering: ServiceOffering? = serviceOffering

    @Column(name = "version", nullable = false)
    var version = version

    @Column(name = "created", nullable = false)
    var created: Date = Calendar.getInstance().time

    @Type(type = "json")
    @Column(name = "service_option_categories", columnDefinition = "LONGTEXT")
    var serviceOptionCategories: List<ServiceOptionCategory> = mutableListOf()

    @Type(type = "json")
    @Column(name = "service_requirements", columnDefinition = "LONGTEXT")
    var serviceRequirements: List<ServiceRequirement> = mutableListOf()

    @Type(type = "json")
    @JsonProperty("serviceRepositories")
    @Column(name = "service_repositories", columnDefinition = "LONGTEXT")
    var serviceRepositories: List<UUID> = mutableListOf()

    @Type(type = "json")
    @Column(name = "deployment_definition", columnDefinition = "LONGTEXT", nullable = false)
    var deploymentDefinition: DeploymentDefinition? = deploymentDefinition

    @Type(type = "json")
    @JsonProperty("servicePorts")
    @Column(name = "service_ports", columnDefinition = "LONGTEXT")
    var servicePorts: List<Integer> = mutableListOf()

    @Throws(ServiceOptionNotFoundException::class)
    fun getServiceOptionOfValue(serviceOptionValue: ServiceOptionValue): ServiceOption
    {
        for (serviceOptionCategory in serviceOptionCategories)
        {
            for (serviceOption in serviceOptionCategory.serviceOptions)
            {
                if (serviceOption.id == serviceOptionValue.serviceOptionId)
                {
                    return serviceOption
                }
            }
        }

        throw ServiceOptionNotFoundException(serviceOptionValue, this)
    }

    fun filterServiceOptionValuesByOptionType(
        serviceOptionValues: Collection<ServiceOptionValue>,
        serviceOptionType: ServiceOptionType
    )
    : List<ServiceOptionValue> {

        var filteredServiceOptions = serviceOptionValues.filter { serviceOptionValue ->
            var serviceOption = this.getServiceOptionOfValue(serviceOptionValue)
            return@filter serviceOption.optionType == serviceOptionType
        }

        return filteredServiceOptions
    }

    @JsonIgnore
    @Column(name = "deployment_type", nullable = false)
    fun getDeploymentType(): DeploymentType? {
        return this.deploymentDefinition?.deploymentType;
    }

    override fun toString(): String {
        return "[id=${this.id} version='${this.version}']"
    }
}
