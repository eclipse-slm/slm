package org.eclipse.slm.service_management.model.services

import org.eclipse.slm.service_management.model.offerings.ServiceOrder
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionWithCurrentValue
import java.util.*


data class ServiceInstanceDetails(

    val id: UUID,

    val serviceOfferingId: UUID,

    val serviceOfferingVersionId: UUID,

    var initialCreation: Date,

    var lastUpdate: Date,

    val tags: List<String>,

    val groupIds: List<UUID>,

    val metaData: Map<String, String>,

    val serviceOptions: List<ServiceOptionWithCurrentValue>,

    val deploymentCapabilityServiceId: UUID,

    val orderHistory: List<ServiceOrder>
) {

}
