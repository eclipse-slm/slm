package org.eclipse.slm.common.utils.keycloak;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLException;
import java.util.regex.Pattern;

public class KeycloakTokenUtil {

    private final static Logger LOG = LoggerFactory.getLogger(KeycloakTokenUtil.class);

    public static String getUserUuid(JwtAuthenticationToken jwtAuthenticationToken) {
        var userUuid = jwtAuthenticationToken.getToken().getSubject();

        return userUuid;
    }

    public static String getToken(JwtAuthenticationToken jwtAuthenticationToken) {
        var token = jwtAuthenticationToken.getToken().getTokenValue();

        return token;
    }

    public static String getAccessTokenFromKeycloakInstance(String keycloakAuthUrl, String realm, String username, String password)
    {
        var url = keycloakAuthUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        TcpClient tcpClient = null;
        try {
            var sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            tcpClient = TcpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        } catch (SSLException e) {
            LOG.error("Error creating SSL context", e);
        }

        HttpClient httpClient = HttpClient.from(tcpClient);
        var webClientBuilder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));


        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", "ui");
        formData.add("grant_type", "password");
        formData.add("username", username);
        formData.add("password", password);
        var keycloakTokenLoginResponse = webClientBuilder.build()
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .block()
                .bodyToFlux(LoginTokenResponse.class).blockFirst();

        return keycloakTokenLoginResponse.getAccessToken();
    }

    public static String getRealm(JwtAuthenticationToken jwtAuthenticationToken) {
        var issuer = jwtAuthenticationToken.getToken().getIssuer();

        var regex = "realms\\/([A-Za-z0-9]+)";
        var pattern = Pattern.compile(regex);

        var matcher = pattern.matcher(issuer.toString());
        if (matcher.find()) {
            var realmName = matcher.group(1);

            return realmName;
        }

        throw new RuntimeException("Could not extract realm from issuer URL");
    }
}
