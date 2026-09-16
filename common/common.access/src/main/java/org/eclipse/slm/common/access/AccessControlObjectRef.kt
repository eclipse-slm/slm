package org.eclipse.slm.common.access

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
data class AccessControlObjectRef(

    @Column(name = "object_type", nullable = false)
    var objectType: String = "",

    @Column(name = "object_id", nullable = false)
    var objectId: UUID = UUID(0L, 0L)
)
