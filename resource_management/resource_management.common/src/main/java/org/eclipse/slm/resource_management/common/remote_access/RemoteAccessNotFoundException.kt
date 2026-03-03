package org.eclipse.slm.resource_management.common.remote_access

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.*

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class RemoteAccessNotFoundException(remoteAccessId: UUID?, resourceId: UUID?) :
    RuntimeException("Remote access with ID '$remoteAccessId' not found for resource: $resourceId") {

    constructor(remoteAccessId: UUID?, resourceId: UUID?, cause: Throwable) : this(remoteAccessId, resourceId) {
        initCause(cause)
    }
}
