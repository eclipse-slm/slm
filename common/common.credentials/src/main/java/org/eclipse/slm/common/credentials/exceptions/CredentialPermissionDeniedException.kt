package org.eclipse.slm.common.credentials.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN)
class CredentialPermissionDeniedException(message: String) : RuntimeException(message) {

    constructor(message: String, cause: Throwable) : this(message) {
        initCause(cause)
    }

}
