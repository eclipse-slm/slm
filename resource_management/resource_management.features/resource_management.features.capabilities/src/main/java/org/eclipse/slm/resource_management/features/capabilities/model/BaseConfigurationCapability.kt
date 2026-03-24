package org.eclipse.slm.resource_management.features.capabilities.model

import com.fasterxml.jackson.annotation.JsonTypeName
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "base_configuration_capability")
@JsonTypeName("BaseConfigurationCapability")
class BaseConfigurationCapability(id: UUID? = null) : Capability(id) {}
