package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
class ServiceInstanceEntity(id: UUID? = null) {

    constructor() : this(null)

    @field:Id
    @field:Column(name = "uuid", length = 36, unique = true, nullable = false)
    var id: UUID = id ?: UUID.randomUUID()

    @field:Column(name = "resource_id")
    var resourceId: UUID? = null

    @field:Column(name = "capability_service_id")
    var capabilityServiceId: UUID? = null

    @field:Column(name = "service_offering_id")
    var serviceOfferingId: UUID? = null

    @field:Column(name = "service_offering_version_id")
    var serviceOfferingVersionId: UUID? = null

    @field:Column(name = "ports", columnDefinition = "LONGTEXT")
    @field:JdbcTypeCode(SqlTypes.JSON)
    var ports: MutableList<Int> = mutableListOf()

    @field:Column(name = "group_ids", columnDefinition = "LONGTEXT")
    @field:JdbcTypeCode(SqlTypes.JSON)
    var groupIds: MutableList<UUID> = mutableListOf()

    @field:Column(name = "custom_tags", columnDefinition = "LONGTEXT")
    @field:JdbcTypeCode(SqlTypes.JSON)
    var customTags: MutableList<String> = mutableListOf()

    @field:Column(name = "custom_meta_data", columnDefinition = "LONGTEXT")
    @field:JdbcTypeCode(SqlTypes.JSON)
    var customMetaData: MutableMap<String, String> = mutableMapOf()
}
