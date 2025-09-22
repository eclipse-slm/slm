package org.eclipse.slm.resource_management.common.remote_access

import java.util.UUID

class RemoteAccessDTO constructor(

    var id: UUID,

    val credential: Credential,

    val connectionPort: Int,

    val connectionType: ConnectionType

) {
}
