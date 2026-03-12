package org.eclipse.slm.resource_management.features.capabilities.providers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService

class ServiceHoster @JsonCreator constructor(
    @JsonProperty("capabilityService") capabilityService: CapabilityService
) : Provider(
    capabilityService, DeploymentCapability::class.java
)
