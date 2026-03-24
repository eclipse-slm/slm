package org.eclipse.slm.resource_management.common.adapters

import org.eclipse.slm.common.consul.model.catalog.Node
import org.eclipse.slm.common.consul.model.catalog.TaggedAddresses
import org.eclipse.slm.resource_management.common.resources.BasicResource
import java.util.*
import kotlin.toString

class ResourceConsulNode (
    val resource: BasicResource,
) : Node(resource.id, resource.id.toString()) {

    companion object {
        const val META_KEY_LOCATION: String = "locationId"
        const val META_KEY_RESOURCE_ID: String = "resourceId"
        const val META_KEY_HOSTNAME: String = "hostname"
        const val META_KEY_ASSET_ID: String = "assetId"
        const val META_KEY_FIRMWARE_VERSION: String = "firmwareVersion"
        const val META_KEY_DRIVER_ID: String = "driverId"
        val STATIC_META_DATA = mapOf("external-node" to "true", "external-probe" to "true")

        @JvmStatic
        fun convertConsulNodeToBasicResource(node: Node): BasicResource {
            val resource = BasicResource(UUID.fromString(node.id.toString()))
            resource.ip = node.address
            if (node.meta!!.containsKey(ResourceConsulNode.META_KEY_HOSTNAME)) {
                resource.hostname = node.meta!!.get(ResourceConsulNode.META_KEY_HOSTNAME)
            }
            if (node.meta!!.containsKey(ResourceConsulNode.META_KEY_ASSET_ID)) {
                resource.assetId = node.meta!!.get(ResourceConsulNode.META_KEY_ASSET_ID)
            }
            if (node.meta!!.containsKey(ResourceConsulNode.META_KEY_FIRMWARE_VERSION)) {
                resource.firmwareVersion = node.meta!!.get(ResourceConsulNode.META_KEY_FIRMWARE_VERSION)
            }
            if (node.meta!!.containsKey(ResourceConsulNode.META_KEY_DRIVER_ID)) {
                resource.driverId = node.meta!!.get(ResourceConsulNode.META_KEY_DRIVER_ID)
            }
            if (node.meta!!.containsKey(ResourceConsulNode.META_KEY_LOCATION)) {
                resource.locationId = UUID.fromString(node.meta!!.get(ResourceConsulNode.META_KEY_LOCATION))
            }

            return resource
        }
    }

    override var id : UUID? = resource.id

    override var nodeName : String = resource.id.toString()

    override var address : String? = resource.ip

    override var taggedAddresses: TaggedAddresses? = null
        get() {
        val taggedAddresses = TaggedAddresses()
        taggedAddresses.lan = resource.ip
        return taggedAddresses
    }

    override var meta: Map<String, String>?= HashMap()
        get() {
            val meta = hashMapOf<String, String>()

            resource.locationId?.toString()?.let { meta[META_KEY_LOCATION] = it }
            meta[META_KEY_RESOURCE_ID] = resource.id.toString()
            resource.hostname?.let { meta[META_KEY_HOSTNAME] = it }
            resource.assetId?.let { meta[META_KEY_ASSET_ID] = it }
            resource.firmwareVersion?.let { meta[META_KEY_FIRMWARE_VERSION] = it }
            resource.driverId?.let { meta[META_KEY_DRIVER_ID] = it }

            meta.putAll(STATIC_META_DATA)
            return meta
        }
}