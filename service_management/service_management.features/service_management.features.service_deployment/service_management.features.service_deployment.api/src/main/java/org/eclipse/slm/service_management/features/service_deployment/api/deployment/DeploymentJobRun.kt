package org.eclipse.slm.service_management.features.service_deployment.api.deployment

import org.eclipse.slm.awx.client.observer.AwxJobObserver
import org.eclipse.slm.service_management.features.service_deployment.api.deployment.ServiceOrder
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

data class DeploymentJobRun(

    val awxJobObserver: AwxJobObserver,

    val jwtAuthenticationToken: JwtAuthenticationToken,

    val serviceInstance: ServiceInstance,

    val serviceOrder: ServiceOrder

    )
