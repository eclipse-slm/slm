package org.eclipse.slm.resource_management.common.test_utils;

import org.eclipse.slm.common.utils.keycloak.KeycloakTokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Component
public class AuthorizationHeaderRequestFactory {

    public MockHttpServletRequestBuilder createRequestWithAuthorizationHeader(MockHttpServletRequestBuilder  requestBuilder)
    {
        var keycloakToken = this.getKeycloakAccessToken();

        return requestBuilder.header("Authorization", "Bearer " + keycloakToken);
    }

    private String getKeycloakAccessToken()
    {
        var keycloakAuthUrl = "http://localhost:7080/auth/";
        var keycloakRealm = "fabos";
        var keycloakUser = "maes";
        var keycloakPassword = "password";

        var accessToken = KeycloakTokenUtil.getAccessTokenFromKeycloakInstance(keycloakAuthUrl, keycloakRealm, keycloakUser, keycloakPassword);
        return accessToken;
    }
}
