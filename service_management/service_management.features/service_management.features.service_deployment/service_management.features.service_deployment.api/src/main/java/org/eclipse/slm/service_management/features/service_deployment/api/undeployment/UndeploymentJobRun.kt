package org.eclipse.slm.service_management.features.service_deployment.api.undeployment

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.util.UUID

data class UndeploymentJobRun(

    val jwtAuthenticationToken: JwtAuthenticationToken,

    val serviceInstanceId: UUID,

    val resourceId: UUID

    )