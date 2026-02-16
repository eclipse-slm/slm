package org.eclipse.slm.common.credentials.persistence

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "credential_entity_link", uniqueConstraints = [UniqueConstraint(columnNames = ["credential_id", "entity_type", "entity_id"])])
data class CredentialEntityLink (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "credential_id", nullable = false, length = 36)
    val credentialId: UUID? = null,

    @Column(name = "entity_type", nullable = false, length = 50)
    val entityType: String? = null,

    @Column(name = "entity_id", nullable = false, length = 200)
    val entityId: String? = null,
)