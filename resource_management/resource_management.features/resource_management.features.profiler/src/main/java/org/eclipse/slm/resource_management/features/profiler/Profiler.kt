package org.eclipse.slm.resource_management.features.profiler

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
class Profiler(id: UUID? = null) {

    constructor() : this(UUID.randomUUID()) {}

    @Id
    @Column(name = "uuid", length = 36, unique = true, nullable = false)
    var id: UUID = id ?: UUID.randomUUID()

    @Column(name = "name")
    var name: String = ""

    @Column(name = "action", columnDefinition = "LONGTEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    var action: Action? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Profiler) return false

        if (id != other.id) return false
        if (name != other.name) return false
        return action == other.action
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (action?.hashCode() ?: 0)
        return result
    }
}
