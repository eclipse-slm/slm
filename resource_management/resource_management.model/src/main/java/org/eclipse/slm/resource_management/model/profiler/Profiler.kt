package org.eclipse.slm.resource_management.model.profiler

import org.eclipse.slm.resource_management.model.actions.Action
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Profiler {

    constructor() {
        this.id = UUID.randomUUID()
    }

    constructor(id: UUID) : this() {
        this.id = id
    }

    @Id
    @Type(type="uuid-char")
    @Column(name = "uuid", length = 36, unique = true, nullable = false)
    var id: UUID

    @Column(name = "name")
    var name: String = ""

    @Column(name = "action", columnDefinition = "LONGTEXT")
    @Type(type = "json")
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
