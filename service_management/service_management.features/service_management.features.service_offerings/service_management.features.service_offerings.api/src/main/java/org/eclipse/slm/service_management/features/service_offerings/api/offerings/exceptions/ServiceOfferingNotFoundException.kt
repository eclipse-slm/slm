package org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.*

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ServiceOfferingNotFoundException(serviceOfferingId: UUID) :
    RuntimeException("Service offering with id '$serviceOfferingId' not found")
