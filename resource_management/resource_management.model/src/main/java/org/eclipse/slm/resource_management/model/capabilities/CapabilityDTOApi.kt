package org.eclipse.slm.resource_management.model.capabilities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.eclipse.slm.resource_management.model.actions.Action
import org.eclipse.slm.resource_management.model.actions.ActionType
import org.eclipse.slm.resource_management.model.cluster.ClusterMemberType
import org.eclipse.slm.resource_management.model.resource.ConnectionType
import java.util.*

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

    open var clusterMemberTypes: List<ClusterMemberType> = ArrayList()

    open var connection: ConnectionType? = null

    override fun toString(): String {
        return "CapabilityDTOApi(id=$id, name='$name', logo='$logo', type=$type, actions=$actions)"
    }
}
