package org.eclipse.slm.common.credentials.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class CredentialRuntimeException(message: String) : RuntimeException(message) {

    constructor(message: String, cause: Throwable) : this(message) {
        initCause(cause)
    }

}
