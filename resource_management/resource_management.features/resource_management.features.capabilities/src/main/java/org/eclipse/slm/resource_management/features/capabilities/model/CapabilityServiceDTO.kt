package org.eclipse.slm.resource_management.features.capabilities.model

import com.fasterxml.jackson.annotation.JsonInclude
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CapabilityServiceDTO (

    var id: UUID,

    var capabilityId: UUID,

    var capabilityClass: String,

    var status: CapabilityServiceStatus,

) {

}
