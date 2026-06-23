package org.eclipse.slm.resource_management.common.remote_access

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "remote_access")
class RemoteAccessEntity(id: UUID? = null) {

    constructor() : this(null)

    @field:Id
    @field:Column(name = "uuid", length = 36, unique = true, nullable = false)
    var id: UUID = id ?: UUID.randomUUID()

    @field:Column(name = "resource_id", nullable = false)
    var resourceId: UUID? = null

    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "connection_type", nullable = false)
    var connectionType: ConnectionType = ConnectionType.ssh

    @field:Column(name = "credential_id", nullable = false)
    var credentialId: UUID? = null

    @field:Column(name = "username")
    var username: String? = null

    @field:Column(name = "connection_port", nullable = false)
    var connectionPort: Int = 0
}
