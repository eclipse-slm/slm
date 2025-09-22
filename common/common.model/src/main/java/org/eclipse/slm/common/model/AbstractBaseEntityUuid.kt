package org.eclipse.slm.common.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.data.domain.Persistable
import java.util.*
import kotlin.jvm.Transient

@MappedSuperclass
abstract class AbstractBaseEntityUuid(id: UUID? = null) : Persistable<UUID> {

    @Id
    @Column(name = "id", length = 36, unique = true, nullable = false)
    private var id: UUID = id ?: UUID.randomUUID()

    @Version
    @JsonIgnore
    open val entityVersion: Long? = 0

    @Transient
    private var persisted: Boolean = id != null

    override fun getId(): UUID = id

    open fun setId(id: UUID) {
        this.id = id
    }

    @JsonIgnore
    override fun isNew(): Boolean = !persisted

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            else -> false
        }
    }

    override fun hashCode(): Int = id.hashCode()

    @PostPersist
    @PostLoad
    private fun setPersisted() {
        persisted = true
    }

}
