package org.eclipse.slm.resource_management.common.access

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.util.UUID

@Embeddable
data class AccessControlObjectRef(

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false)
    var objectType: AccessControlObjectType = AccessControlObjectType.RESOURCE,

    @Column(name = "object_id", nullable = false)
    var objectId: UUID = UUID(0L, 0L)
)
