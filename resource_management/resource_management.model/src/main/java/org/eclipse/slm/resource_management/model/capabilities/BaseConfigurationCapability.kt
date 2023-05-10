package org.eclipse.slm.resource_management.model.capabilities

import com.fasterxml.jackson.annotation.JsonTypeName
import com.vladmihalcea.hibernate.type.json.JsonStringType
import org.eclipse.slm.common.model.DeploymentType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "base_configuration_capability")
@TypeDefs(TypeDef(name = "json", typeClass = JsonStringType::class))
@JsonTypeName("BaseConfigurationCapability")
class BaseConfigurationCapability(id: UUID? = null) : Capability(id) {}
