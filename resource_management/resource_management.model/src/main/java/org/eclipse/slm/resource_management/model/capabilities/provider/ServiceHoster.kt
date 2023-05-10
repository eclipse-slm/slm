package org.eclipse.slm.resource_management.model.capabilities.provider

import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.resource_management.model.capabilities.DeploymentCapability
import org.eclipse.slm.resource_management.model.consul.capability.CapabilityService

class ServiceHoster(@JsonProperty capabilityService: CapabilityService) : Provider(
    capabilityService,
    DeploymentCapability::class.java
)
