package org.eclipse.slm.common.credentials.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.UUID

@ResponseStatus(HttpStatus.NOT_FOUND)
class CredentialNotFoundException(credentialId: UUID) : RuntimeException("Credential with id $credentialId not found") {

    constructor(credentialId: UUID, cause: Throwable) : this(credentialId) {
        initCause(cause)
    }
}
