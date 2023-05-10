package org.eclipse.slm.service_management.model.offerings.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.*

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ServiceOfferingVersionNotFoundException : Exception {
    constructor(serviceOfferingVersionId: UUID)
            : super("Service offering version with id '$serviceOfferingVersionId' not found") {}
    constructor(serviceOfferingVersionName: String)
            : super("Service offering version with name '$serviceOfferingVersionName' not found") {}
}
