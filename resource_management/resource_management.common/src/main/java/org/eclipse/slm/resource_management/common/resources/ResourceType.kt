package org.eclipse.slm.resource_management.common.resources

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

class ResourceType(

    @JsonProperty("typeName")
    var typeName: String,

    @JsonProperty("manufacturerName")
    var manufacturerName: String
) {

    var resourceInstanceIds = mutableListOf<UUID>()

    var softwareNameplateIds = mutableListOf<String>()

    fun addResourceInstanceId(id: UUID) {
        resourceInstanceIds.add(id)
    }

}