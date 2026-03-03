package org.eclipse.slm.common.keycloak.testing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse {
    @JsonProperty("access_token")
    public String accessToken;

    @JsonProperty("expires_in")
    public int expiresIn;

    @JsonProperty("refresh_expires_in")
    public int refreshExpiresIn;

    @JsonProperty("refresh_token")
    public String refreshToken;

    @JsonProperty("token_type")
    public String tokenType;

    @JsonProperty("scope")
    public String scope;
}