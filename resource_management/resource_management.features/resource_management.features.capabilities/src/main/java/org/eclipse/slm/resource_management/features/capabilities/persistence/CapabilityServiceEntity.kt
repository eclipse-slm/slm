package org.eclipse.slm.resource_management.features.capabilities.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityServiceStatus
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
class CapabilityServiceEntity(id: UUID? = null) {

    constructor() : this(null)

    @field:Id
    @field:Column(name = "uuid", length = 36, unique = true, nullable = false)
    var id: UUID = id ?: UUID.randomUUID()

    @field:Column(name = "resource_id")
    var resourceId: UUID? = null

    @field:Column(name = "capability_id")
    var capabilityId: UUID? = null

    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "status")
    var status: CapabilityServiceStatus = CapabilityServiceStatus.UNKNOWN

    @field:Column(name = "managed")
    var managed: Boolean = false

    @field:Column(name = "port")
    var port: Int? = null

    @field:Column(name = "custom_meta", columnDefinition = "LONGTEXT")
    @field:JdbcTypeCode(SqlTypes.JSON)
    var customMeta: MutableMap<String, String> = mutableMapOf()

    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "service_class")
    var serviceClass: CapabilityServiceClass = CapabilityServiceClass.SINGLE_HOST

    @field:Column(name = "member_mapping", columnDefinition = "LONGTEXT")
    @field:JdbcTypeCode(SqlTypes.JSON)
    var memberMapping: MutableMap<UUID, String>? = null
}
