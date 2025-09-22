package org.eclipse.slm.resource_management.features.capabilities.model

import com.fasterxml.jackson.annotation.JsonTypeName
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "virtualization_capability")
@JsonTypeName("VirtualizationCapability")
class VirtualizationCapability(id: UUID? = null) : Capability(id) {
}
