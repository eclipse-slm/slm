package org.eclipse.slm.platform_management.features.user_management.impl

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class LastAdminDeletionNotAllowedException(message: String) : RuntimeException(message)

