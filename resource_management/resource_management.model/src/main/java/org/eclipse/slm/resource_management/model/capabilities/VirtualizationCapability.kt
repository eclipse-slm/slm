package org.eclipse.slm.resource_management.model.capabilities

import com.fasterxml.jackson.annotation.JsonTypeName
import com.vladmihalcea.hibernate.type.json.JsonStringType
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "virtualization_capability")
@JsonTypeName("VirtualizationCapability")
class VirtualizationCapability(id: UUID? = null) : Capability(id) {
}
