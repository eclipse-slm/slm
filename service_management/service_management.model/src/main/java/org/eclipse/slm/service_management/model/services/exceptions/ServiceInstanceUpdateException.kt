package org.eclipse.slm.service_management.model.services.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class ServiceInstanceUpdateException(message: String?) : Exception(message)
