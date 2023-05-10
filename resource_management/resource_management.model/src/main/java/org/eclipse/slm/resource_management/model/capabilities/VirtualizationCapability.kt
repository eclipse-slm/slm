package org.eclipse.slm.resource_management.model.capabilities

import com.fasterxml.jackson.annotation.JsonTypeName
import com.vladmihalcea.hibernate.type.json.JsonStringType
import org.eclipse.slm.resource_management.model.capabilities.Capability
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "virtualization_capability")
@TypeDefs(TypeDef(name = "json", typeClass = JsonStringType::class))
@JsonTypeName("VirtualizationCapability")
class VirtualizationCapability(id: UUID? = null) : Capability(id) {
}
