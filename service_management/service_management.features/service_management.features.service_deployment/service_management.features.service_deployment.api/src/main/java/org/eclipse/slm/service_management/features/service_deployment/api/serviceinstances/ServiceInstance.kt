package org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID

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
        @JvmField
        val PREDEFINED_TAGS = listOf("service")

        @JvmField
        val PREDEFINED_META_DATA_KEYS = listOf(
            META_DATA_KEY_RESOURCE_ID,
            META_DATA_KEY_SERVICE_INSTANCE_ID,
            META_DATA_KEY_SERVICE_OFFERING_ID,
            META_DATA_KEY_SERVICE_OFFERING_VERSION_ID,
            META_DATA_KEY_CAPABILITY_SERVICE_ID,
            META_DATA_KEY_PORTS,
            META_DATA_KEY_GROUPS)

    }

}
