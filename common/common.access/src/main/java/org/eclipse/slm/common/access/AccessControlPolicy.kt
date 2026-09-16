package org.eclipse.slm.common.access

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import java.util.UUID

@Entity
class AccessControlPolicy(id: UUID? = null) {

    constructor() : this(null)

    @Id
    @Column(name = "uuid", length = 36, unique = true, nullable = false)
    var id: UUID = id ?: UUID.randomUUID()

    @Column(name = "name")
    var name: String = ""

    @Column(name = "description")
    var description: String = ""

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "policy_subjects",
        joinColumns = [JoinColumn(name = "policy_id")]
    )
    @Column(name = "subject")
    var subjects: MutableSet<String> = mutableSetOf()

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "policy_objects",
        joinColumns = [JoinColumn(name = "policy_id")]
    )
    var objects: MutableSet<AccessControlObjectRef> = mutableSetOf()
}
