package org.eclipse.slm.common.keycloak.testing;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface KeycloakTokenClient {

    // Public Client (without client_secret)
    @RequestLine("POST /realms/{realm}/protocol/openid-connect/token")
    @Headers({
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: application/json"
    })
    @Body("grant_type=password&client_id={client_id}&username={username}&password={password}")
    TokenResponse passwordGrant(
        @Param("realm") String realm,
        @Param("client_id") String clientId,
        @Param("username") String username,
        @Param("password") String password
    );

    // Confidential Client (with client_secret)
    @RequestLine("POST /realms/{realm}/protocol/openid-connect/token")
    @Headers({
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: application/json"
    })
    @Body("grant_type=password&client_id={client_id}&client_secret={client_secret}&username={username}&password={password}")
    TokenResponse passwordGrantWithSecret(
        @Param("realm") String realm,
        @Param("client_id") String clientId,
        @Param("client_secret") String clientSecret,
        @Param("username") String username,
        @Param("password") String password
    );
}