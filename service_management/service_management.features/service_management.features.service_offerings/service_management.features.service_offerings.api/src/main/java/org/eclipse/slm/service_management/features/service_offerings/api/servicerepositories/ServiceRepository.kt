package org.eclipse.slm.service_management.features.service_offerings.api.servicerepositories

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class ServiceRepository(id: UUID? = null) {

    var id: UUID = id ?: UUID.randomUUID()

    var serviceVendorId: UUID? = null

    var address = ""

    var username = ""

    var password = ""

    @JsonProperty("type")
    var serviceRepositoryType: ServiceRepositoryType? = null
}
