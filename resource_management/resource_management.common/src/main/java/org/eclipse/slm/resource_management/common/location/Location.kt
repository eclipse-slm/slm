package org.eclipse.slm.resource_management.common.location

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.UUID

@Entity
data class Location(
    @Id
    @Column(name = "uuid", length = 36, unique = true, nullable = false)
    var id : UUID,

    @Column(name="name", unique = true, nullable = false)
    var name: String
) {
    constructor(name: String) : this(UUID.randomUUID(), name) {
    }

    constructor(): this(UUID.randomUUID(), "")
}