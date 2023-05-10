package org.eclipse.slm.service_management.model.service_repositories

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class ServiceRepositoryDTOApiRead(id: UUID? = null) {

    var id: UUID = id ?: UUID.randomUUID()

    var serviceVendorId: UUID? = null

    var address = ""

    var username = ""

    @JsonProperty("type")
    var serviceRepositoryType: ServiceRepositoryType? = null;
}
