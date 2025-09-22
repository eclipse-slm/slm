package org.eclipse.slm.resource_management.features.capabilities.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.eclipse.slm.resource_management.common.remote_access.ConnectionType
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityHealthCheck
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityType
import org.eclipse.slm.resource_management.features.capabilities.model.actions.Action
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType
import org.eclipse.slm.resource_management.features.capabilities.model.awx.ExecutionEnvironment
import java.util.ArrayList
import java.util.HashMap
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "capabilityClass")
@JsonSubTypes(
    JsonSubTypes.Type(value = DeploymentCapabilityDTOApi::class, name = "DeploymentCapability"),
    JsonSubTypes.Type(value = VirtualizationCapabilityDTOApi::class, name = "VirtualizationCapability"),
    JsonSubTypes.Type(value = BaseConfigurationCapabilityDTOApi::class, name = "BaseConfigurationCapability")
)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
abstract class CapabilityDTOApi(id: UUID? = null, capabilityClass: String) {

    open var capabilityClass: String = capabilityClass

    open var id: UUID = id ?: UUID.randomUUID()

    open var name: String = ""

    open var logo: String = ""

    open var type: List<CapabilityType> = ArrayList()

    open var actions: Map<ActionType, Action> = HashMap()

    open var healthCheck: CapabilityHealthCheck? = null

//    open var clusterMemberTypes: List<ClusterMemberType> = ArrayList()

    open var connection: ConnectionType? = null

    open var executionEnvironment: ExecutionEnvironment? = null

    override fun toString(): String {
        return "CapabilityDTOApi(id=$id, name='$name', logo='$logo', type=$type, actions=$actions)"
    }
}