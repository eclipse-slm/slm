package org.eclipse.slm.service_management.model.offerings.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class InvalidServiceOfferingDefinitionException(message: String?) : Exception(message)
