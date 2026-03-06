package org.eclipse.slm.resource_management.features.capabilities.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.eclipse.slm.common.consul.model.catalog.NodeService
import org.eclipse.slm.common.consul.model.catalog.Service
import org.eclipse.slm.resource_management.features.capabilities.CapabilityUtil
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService
import java.util.*
import kotlin.text.toBoolean

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "capabilityServiceClass")
@JsonSubTypes(
    JsonSubTypes.Type(value = SingleHostCapabilityService::class, name = "SingleHostCapabilityService"),
    JsonSubTypes.Type(value = MultiHostCapabilityService::class, name = "MultiHostCapabilityService")
)
open class CapabilityService(
    val resourceId: UUID,
    serviceId: UUID,
    val capability: Capability,
    var status: CapabilityServiceStatus = CapabilityServiceStatus.UNKNOWN,
    var managed: Boolean = false,
    var customMeta: Map<String, String> = emptyMap()
) : NodeService(
    serviceId.toString(),
    capability.name.lowercase().replace(" ", "_") + "_" + serviceId
) {

    var serviceClass: String = this.javaClass.simpleName

    companion object {
        const val META_KEY_CAPABILITY_SERVICE_CLASS = "capabilityServiceClass"
        const val META_KEY_CAPABILITY_CLASS = "capabilityClass"
        const val META_KEY_CAPABILITY_ID = "capabilityId"
        const val META_KEY_CONNECTION_TYPE = "connectionType"
        const val META_KEY_STATUS = "status"
        const val META_KEY_MANAGED = "managed"
        const val TAG_CAPABILITY = "Capability"

        @JvmStatic
        fun builder(resourceId: UUID,
                    serviceId: UUID,
                    capability: Capability): Builder = Builder(resourceId, serviceId, capability)

        @JvmStatic
        fun createFromCatalogService(catalogService: Service, capability: Capability): CapabilityService {
            var capabilityService = builder(catalogService.nodeId!!, catalogService.serviceId!!, capability)
                                        .status(CapabilityServiceStatus.valueOf(catalogService.serviceMeta.get(META_KEY_STATUS)!!))
                                        .managed(catalogService.serviceMeta?.get(META_KEY_MANAGED).toBoolean())
                                        .customMeta(CapabilityUtil.getCustomMeta(catalogService.serviceMeta)).build()
            capabilityService.port = catalogService.servicePort
            return capabilityService
        }

        @JvmStatic
        fun createFromNodeService(nodeService: NodeService, nodeId: UUID, capability: Capability): CapabilityService {
            var capabilityService = builder(nodeId, UUID.fromString(nodeService.id), capability)
                .status(CapabilityServiceStatus.valueOf(nodeService.meta?.get(META_KEY_STATUS)!!))
                .managed(nodeService.meta?.get(META_KEY_MANAGED).toBoolean())
                .customMeta(CapabilityUtil.getCustomMeta(nodeService.meta)).build()
            capabilityService.port = nodeService.port
            return capabilityService
        }

    }

    var serviceId: UUID = UUID.fromString(super.id)
        get() = UUID.fromString(super.id)
        set(value) {
            super.id = value.toString()
            field = value
        }

    override var tags: List<String> = ArrayList()
        get() = arrayListOf(
            TAG_CAPABILITY,
            capability.name,
            capability.capabilityClass,
            this.javaClass.simpleName
        )

    override var meta: Map<String, String> = HashMap()
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

    class Builder(
        var resourceId: UUID,
        var serviceId: UUID,
        var capability: Capability
    ) {
        private var status: CapabilityServiceStatus = CapabilityServiceStatus.UNKNOWN
        private var managed: Boolean = false
        private var customMeta: Map<String, String> = emptyMap()

        fun status(status: CapabilityServiceStatus) = apply { this.status = status }
        fun managed(managed: Boolean) = apply { this.managed = managed }
        fun customMeta(customMeta: Map<String, String>) = apply { this.customMeta = customMeta }

        fun build(): CapabilityService {
            return CapabilityService(
                resourceId,
                serviceId,
                capability,
                status,
                managed,
                customMeta
            )
        }
    }

}
