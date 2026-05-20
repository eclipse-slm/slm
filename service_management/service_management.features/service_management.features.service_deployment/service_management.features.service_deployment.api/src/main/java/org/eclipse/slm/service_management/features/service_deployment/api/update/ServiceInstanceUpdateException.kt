package org.eclipse.slm.service_management.features.service_deployment.api.update

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class ServiceInstanceUpdateException(message: String?) : Exception(message)