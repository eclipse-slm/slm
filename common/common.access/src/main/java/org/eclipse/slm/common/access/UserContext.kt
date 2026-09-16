package org.eclipse.slm.common.access

import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

data class UserContext @JvmOverloads constructor(
    val groups: Set<String>,
    val isAdmin: Boolean,
    val accessToken: String = ""
) {
    companion object {
        @JvmStatic
        fun fromJwt(jwt: JwtAuthenticationToken): UserContext = UserContext(
            KeycloakTokenUtil.getGroups(jwt),
            KeycloakTokenUtil.isAdmin(jwt),
            KeycloakTokenUtil.getToken(jwt)
        )
    }
}
