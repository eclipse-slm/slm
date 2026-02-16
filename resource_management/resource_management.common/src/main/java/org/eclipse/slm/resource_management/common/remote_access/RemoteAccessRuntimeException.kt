package org.eclipse.slm.resource_management.common.remote_access

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class RemoteAccessRuntimeException(message: String?, cause: Throwable?) : RuntimeException(message, cause)
