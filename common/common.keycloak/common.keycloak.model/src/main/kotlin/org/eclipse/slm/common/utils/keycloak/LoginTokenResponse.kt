package org.eclipse.slm.common.utils.keycloak

import com.fasterxml.jackson.annotation.JsonProperty

class LoginTokenResponse (
    @JsonProperty("access_token")
    var accessToken: String,

    @JsonProperty("expires_in")
    var expiresIn: Int,

    @JsonProperty("refresh_token")
    var refreshToken: String,

    @JsonProperty("refresh_expires_in")
    var refreshExpiresIn: Int,

    @JsonProperty("token_type")
    var tokenType: String,

    @JsonProperty("not-before-policy")
    var notBeforePolicy: Int,

    @JsonProperty("session_state")
    var sessionState: String,

    @JsonProperty("scope")
    var scope: String
)
{

}
