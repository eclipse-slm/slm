package org.eclipse.slm.service_management.features.service_deployment.impl.update

import org.eclipse.slm.awx.client.observer.AwxJobObserver
import org.eclipse.slm.service_management.features.service_offerings.api.offeringversions.ServiceOfferingVersion
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.util.UUID

data class UpdateJobRun(

    val awxJobObserver: AwxJobObserver,

    val jwtAuthenticationToken: JwtAuthenticationToken,

    val serviceInstanceId: UUID,

    val serviceOrder: ServiceOrder,

    val resourceId: UUID,

    val serviceOfferingVersion: ServiceOfferingVersion,

    )