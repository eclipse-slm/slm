package org.eclipse.slm.resource_management.features.capabilities.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.eclipse.slm.common.consul.model.catalog.CatalogNode
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService
import org.eclipse.slm.resource_management.features.capabilities.model.Capability
import org.eclipse.slm.resource_management.features.capabilities.model.SingleHostCapabilityService
import java.util.*

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "capabilityServiceClass"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SingleHostCapabilityService::class, name = "SingleHostCapabilityService"),
    JsonSubTypes.Type(value = MultiHostCapabilityService::class, name = "MultiHostCapabilityService")
)
open class CapabilityService: CatalogNode.Service {

    companion object {
        const val META_KEY_SERVICE_CLASS = "serviceClass"
        const val META_KEY_CAPABILITY_CLASS = "capabilityClass"
        const val META_KEY_CAPABILITY_ID = "capabilityId"
        const val META_KEY_CONNECTION_TYPE = "connectionType"
        const val META_KEY_STATUS = "status"
        const val META_KEY_MANAGED = "managed"
        const val TAG_CAPABILITY = "Capability"
    }

    var capability : Capability? = null
    open var status : CapabilityServiceStatus = CapabilityServiceStatus.UNKNOWN

    var serviceClass : String = this.javaClass.simpleName
    var customMeta : Map<String, String> = emptyMap()
    var managed: Boolean = false

    constructor() : super()
    constructor(capability: Capability): super() {
        this.capability = capability
    }

    constructor(capability: Capability, status: CapabilityServiceStatus): super() {
        this.capability = capability
        this.status = status
    }
    constructor(capability: Capability, serviceId: UUID): super(serviceId) {
        this.capability = capability
    }

    constructor(capability: Capability, serviceId: UUID, status: CapabilityServiceStatus): super(serviceId) {
        this.capability = capability
        this.status = status
    }

    override var service: String? = null
        get() = getNameLoweredWithoutWhiteSpaces()+"_"+id

    override var tags : List<String> = ArrayList()
        get() = arrayListOf(
            TAG_CAPABILITY,
            capability!!.name,
            capability!!.capabilityClass,
            this.javaClass.simpleName
        )

    override var serviceMeta: Map<String, String> = HashMap()
        get() {
            var defaultMap = hashMapOf<String, String>(
                META_KEY_SERVICE_CLASS to serviceClass,
                META_KEY_CAPABILITY_CLASS to capability!!.capabilityClass,
                META_KEY_CAPABILITY_ID to capability!!.id.toString(),
                META_KEY_CONNECTION_TYPE to capability!!.connection.toString(),
                META_KEY_STATUS to status.name,
                META_KEY_MANAGED to managed.toString()
            )
            return customMeta + defaultMap
        }

    private fun getNameLoweredWithoutWhiteSpaces(): String {
        return capability!!.name.lowercase().replace(" ","_")
    }
}
