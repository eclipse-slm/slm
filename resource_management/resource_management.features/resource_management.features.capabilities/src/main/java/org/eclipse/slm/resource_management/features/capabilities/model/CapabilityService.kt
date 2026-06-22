package org.eclipse.slm.resource_management.features.capabilities.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "capabilityServiceClass")
@JsonSubTypes(
    JsonSubTypes.Type(value = SingleHostCapabilityService::class, name = "SingleHostCapabilityService"),
    JsonSubTypes.Type(value = MultiHostCapabilityService::class, name = "MultiHostCapabilityService")
)
open class CapabilityService(

    @param:JsonProperty("resourceId")
    val resourceId: UUID,

    @JsonProperty("serviceId")
    var serviceId: UUID,

    @param:JsonProperty("capability")
    val capability: Capability,

    @param:JsonProperty("status")
    var status: CapabilityServiceStatus = CapabilityServiceStatus.UNKNOWN,

    @param:JsonProperty("managed")
    var managed: Boolean = false,

    @param:JsonProperty("customMeta")
    var customMeta: Map<String, String> = emptyMap()
) {

    var serviceClass: String = this.javaClass.simpleName

    var port: Int? = null

    val serviceName: String
        get() = capability.name.lowercase().replace(" ", "_") + "_" + serviceId

    val tags: List<String>
        get() = arrayListOf(
            TAG_CAPABILITY,
            capability.name,
            capability.capabilityClass,
            this.javaClass.simpleName
        )

    val meta: Map<String, String>
        get() {
            val defaultMap = hashMapOf(
                META_KEY_CAPABILITY_SERVICE_CLASS to serviceClass,
                META_KEY_CAPABILITY_CLASS to capability.capabilityClass,
                META_KEY_CAPABILITY_ID to capability.id.toString(),
                META_KEY_CONNECTION_TYPE to capability.connection.toString(),
                META_KEY_STATUS to status.name,
                META_KEY_MANAGED to managed.toString(),
            )
            return customMeta + defaultMap
        }

    companion object {
        const val META_KEY_CAPABILITY_SERVICE_CLASS = "capabilityServiceClass"
        const val META_KEY_CAPABILITY_CLASS = "capabilityClass"
        const val META_KEY_CAPABILITY_ID = "capabilityId"
        const val META_KEY_CONNECTION_TYPE = "connectionType"
        const val META_KEY_STATUS = "status"
        const val META_KEY_MANAGED = "managed"
        const val TAG_CAPABILITY = "Capability"
    }
}
