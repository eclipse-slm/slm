package org.eclipse.slm.resource_management.model.capabilities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.vladmihalcea.hibernate.type.json.JsonStringType
import org.eclipse.slm.resource_management.model.actions.Action
import org.eclipse.slm.resource_management.model.actions.ActionType
import org.eclipse.slm.resource_management.model.cluster.ClusterMemberType
import org.eclipse.slm.resource_management.model.resource.ConnectionType
import org.eclipse.slm.resource_management.model.resource.ExecutionEnvironment
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*
import javax.persistence.*
import kotlin.collections.HashSet

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@TypeDefs(TypeDef(name = "json", typeClass = JsonStringType::class))

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "capabilityClass")
@JsonSubTypes(
    JsonSubTypes.Type(value = DeploymentCapability::class,      name = "DeploymentCapability"),
    JsonSubTypes.Type(value = VirtualizationCapability::class,  name = "VirtualizationCapability"),
    JsonSubTypes.Type(value = BaseConfigurationCapability::class,  name = "BaseConfigurationCapability")
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
abstract class Capability(id: UUID? = null) {

    constructor() : this(UUID.randomUUID()) {}

    @Id
    @Type(type="uuid-char")
    @Column(name = "uuid", length = 36, unique = true, nullable = false)
    open var id: UUID = id ?: UUID.randomUUID()

    @Column(name = "capabilityClass")
    open var capabilityClass: String = ""

    @Column(name = "name")
    open var name: String = ""

    @Column(name = "logo")
    open var logo: String = ""

    @Column(name = "type")
    @Type(type = "json")
    open var type: List<CapabilityType> = ArrayList()

    @Column(name = "actions", columnDefinition = "LONGTEXT")
    @Type(type = "json")
    open var actions: Map<ActionType, Action> = HashMap()

    @Column(name = "health_check")
    @Type(type = "json")
    open var healthCheck: CapabilityHealthCheck? = null

    @Column(name = "cluster_member_types", columnDefinition = "LONGTEXT")
    @Type(type = "json")
    open var clusterMemberTypes: List<ClusterMemberType> = ArrayList()

    @Column(name = "connection")
    open var connection: ConnectionType? = null

    @Column(name = "execution_environment", columnDefinition = "LONGTEXT")
    @Type(type = "json")
    open var executionEnvironment: ExecutionEnvironment? = null

    fun isCluster(): Boolean {
        return clusterMemberTypes.isNotEmpty();
    }
    fun getConnectionTypes() : Set<ConnectionType> {
        var connectionTypes = HashSet<ConnectionType>()
        actions.forEach { entry ->
            connectionTypes = (connectionTypes + entry.value.connectionTypes) as HashSet<ConnectionType>
        }
        return connectionTypes
    }

    override fun toString(): String {
        return "[id='${id}' name='${name}']"
    }

}
