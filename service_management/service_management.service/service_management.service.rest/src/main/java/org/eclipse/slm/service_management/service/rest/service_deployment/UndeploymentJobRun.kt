package org.eclipse.slm.service_management.service.rest.service_deployment

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.util.*

data class UndeploymentJobRun(

    val jwtAuthenticationToken: JwtAuthenticationToken,

    val serviceInstanceId: UUID,

    val resourceId: UUID

    )
