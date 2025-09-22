package org.eclipse.slm.resource_management.features.providers

import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityService

class ServiceHoster(@JsonProperty capabilityService: CapabilityService) : Provider(
    capabilityService,
    DeploymentCapability::class.java
)
