package org.eclipse.slm.service_management.model.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*

class ServiceInstance(

    var id: UUID,

    tags: List<String>,

    metaData: Map<String, String>,

    var resourceId: UUID,

    var capabilityServiceId: UUID,

    var serviceOfferingId: UUID,

    var serviceOfferingVersionId: UUID,

    var ports: List<Int> = mutableListOf(),

    var groupIds: List<UUID> = mutableListOf()

    ) {

    private var customTags: List<String> = tags
    var tags: List<String>
        get () {
            var allTags = this.customTags.toMutableList()
            allTags.addAll(PREDEFINED_TAGS)

            return allTags
        }
        set (value) {
            var mutableValue = value.toMutableList()
            mutableValue.removeAll(PREDEFINED_TAGS)
            this.customTags = mutableValue
        }

    private var customMetaData: Map<String, String> = metaData
    var metaData: Map<String, String>
        get () {
            var objectMapper = ObjectMapper()
            var allMetaData = this.customMetaData.toMutableMap()
            allMetaData[META_DATA_KEY_RESOURCE_ID] = this.resourceId.toString()
            allMetaData[META_DATA_KEY_SERVICE_INSTANCE_ID] = this.id.toString()
            allMetaData[META_DATA_KEY_SERVICE_OFFERING_ID] = this.serviceOfferingId.toString()
            allMetaData[META_DATA_KEY_SERVICE_OFFERING_VERSION_ID] = this.serviceOfferingVersionId.toString()
            allMetaData[META_DATA_KEY_CAPABILITY_SERVICE_ID] = this.capabilityServiceId.toString()
            allMetaData[META_DATA_KEY_PORTS] = objectMapper.writeValueAsString(this.ports)
            allMetaData[META_DATA_KEY_GROUPS] = objectMapper.writeValueAsString(this.groupIds)

            return allMetaData
        }
        set(value) {
            var mutableValue = value.toMutableMap()
            PREDEFINED_META_DATA_KEYS.forEach {
                mutableValue.remove(it)
            }
        }

    companion object {
        const val META_DATA_KEY_RESOURCE_ID = "resource_id"
        const val META_DATA_KEY_SERVICE_INSTANCE_ID = "service_id"
        const val META_DATA_KEY_SERVICE_OFFERING_ID = "service_offering_id"
        const val META_DATA_KEY_SERVICE_OFFERING_VERSION_ID = "service_offering_version_id"
        const val META_DATA_KEY_CAPABILITY_SERVICE_ID = "capability_service_id"
        const val META_DATA_KEY_PORTS = "ports"
        const val META_DATA_KEY_GROUPS = "groups"
        val PREDEFINED_TAGS = listOf("service")
        val PREDEFINED_META_DATA_KEYS = listOf(
            META_DATA_KEY_RESOURCE_ID,
            META_DATA_KEY_SERVICE_INSTANCE_ID,
            META_DATA_KEY_SERVICE_OFFERING_ID,
            META_DATA_KEY_SERVICE_OFFERING_VERSION_ID,
            META_DATA_KEY_CAPABILITY_SERVICE_ID,
            META_DATA_KEY_PORTS,
            META_DATA_KEY_GROUPS)

        fun ofMetaDataAndTags(metaData: MutableMap<String, String>, tags: MutableList<String>): ServiceInstance {
            var mutableTags = tags.toMutableList();

            val JSON = jacksonObjectMapper()
            var resourceId = UUID.fromString(metaData[META_DATA_KEY_RESOURCE_ID])
            metaData.remove(META_DATA_KEY_RESOURCE_ID)
            var id = UUID.fromString(metaData[META_DATA_KEY_SERVICE_INSTANCE_ID])
            metaData.remove(META_DATA_KEY_SERVICE_INSTANCE_ID)
            var serviceOfferingId = UUID.fromString(metaData[META_DATA_KEY_SERVICE_OFFERING_ID])
            metaData.remove(META_DATA_KEY_SERVICE_OFFERING_ID)
            var serviceOfferingVersionId = UUID.fromString(metaData[META_DATA_KEY_SERVICE_OFFERING_VERSION_ID])
            metaData.remove(META_DATA_KEY_SERVICE_OFFERING_VERSION_ID)
            var capabilityServiceId = UUID.fromString(metaData[META_DATA_KEY_CAPABILITY_SERVICE_ID])
            metaData.remove(META_DATA_KEY_CAPABILITY_SERVICE_ID)
            var ports = JSON.readValue(metaData[META_DATA_KEY_PORTS], object: TypeReference<List<Int>>() {})
            metaData.remove(META_DATA_KEY_PORTS)
            var groupsIds = JSON.readValue(metaData[META_DATA_KEY_GROUPS], object: TypeReference<List<UUID>>() {})
            metaData.remove(META_DATA_KEY_GROUPS)

            mutableTags.remove("service")

            return ServiceInstance(id, mutableTags, metaData, resourceId, capabilityServiceId, serviceOfferingId,
            serviceOfferingVersionId, ports, groupsIds)
        }
    }

}
