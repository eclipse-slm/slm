package org.eclipse.slm.platform_management.service.app.users

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/** Exception thrown when a user is not found. */
@ResponseStatus(HttpStatus.NOT_FOUND)
class UserNotFoundException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}