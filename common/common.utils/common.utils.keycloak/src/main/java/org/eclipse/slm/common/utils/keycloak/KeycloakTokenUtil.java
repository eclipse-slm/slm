package org.eclipse.slm.common.utils.keycloak;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

public class KeycloakTokenUtil {

    private final static Logger LOG = LoggerFactory.getLogger(KeycloakTokenUtil.class);

    public static String getUserUuid(KeycloakPrincipal keycloakPrincipal) {
        AccessToken accessToken = keycloakPrincipal.getKeycloakSecurityContext().getToken();
        String userUuid = accessToken.getSubject();

        return userUuid;
    }

    public static String getToken(KeycloakPrincipal keycloakPrincipal)
    {
        var tokenString = keycloakPrincipal.getKeycloakSecurityContext().getTokenString();
        return tokenString;
    }

    public static String getAccessTokenFromKeycloakInstance(String keycloakAuthUrl, String realm, String username, String password)
    {
        var url = keycloakAuthUrl + "/realms/" + realm + "/protocol/openid-connect/token";

       var client = WebClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", "ui");
        formData.add("grant_type", "password");
        formData.add("username", username);
        formData.add("password", password);
        var keycloakTokenLoginResponse = WebClient.create()
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .block()
                .bodyToFlux(LoginTokenResponse.class).blockFirst();

        return keycloakTokenLoginResponse.getAccessToken();
    }

    public static String getRealm(KeycloakPrincipal keycloakPrincipal) {
        return keycloakPrincipal.getKeycloakSecurityContext().getRealm();
    }
}
