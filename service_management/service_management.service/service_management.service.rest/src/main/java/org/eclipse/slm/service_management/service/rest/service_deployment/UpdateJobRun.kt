package org.eclipse.slm.service_management.service.rest.service_deployment

import org.eclipse.slm.common.awx.client.observer.AwxJobObserver
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion
import org.eclipse.slm.service_management.model.offerings.ServiceOrder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.util.*

data class UpdateJobRun(

    val awxJobObserver: AwxJobObserver,

    val jwtAuthenticationToken: JwtAuthenticationToken,

    val serviceInstanceId: UUID,

    val serviceOrder: ServiceOrder,

    val resourceId: UUID,

    val serviceOfferingVersion: ServiceOfferingVersion,

    )
