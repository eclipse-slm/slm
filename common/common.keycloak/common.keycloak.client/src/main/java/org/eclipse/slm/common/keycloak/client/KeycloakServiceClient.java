package org.eclipse.slm.common.keycloak.client;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.eclipse.slm.common.utils.keycloak.CustomJwtDecoder;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLException;

@Component
public class KeycloakServiceClient {

    private final MultiTenantKeycloakRegistration multiTenantKeycloakRegistration;

    public KeycloakServiceClient(MultiTenantKeycloakRegistration multiTenantKeycloakRegistration) {
        this.multiTenantKeycloakRegistration = multiTenantKeycloakRegistration;
    }
    
    public JwtAuthenticationToken getJwtAuthentication() throws SSLException {
        var authUrl = this.multiTenantKeycloakRegistration.getFirstOidcConfig().getAuthServerUrl();
        var realm = this.multiTenantKeycloakRegistration.getFirstOidcConfig().getRealm();
        var clientId = this.multiTenantKeycloakRegistration.getFirstOidcConfig().getResource();
        var clientSecret = this.multiTenantKeycloakRegistration.getFirstOidcConfig().getCredentials().getSecret();

        var tokenUrl = authUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        TcpClient tcpClient = TcpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        HttpClient httpClient = HttpClient.from(tcpClient);
        var webClientBuilder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
        var webClient = webClientBuilder.build();

        var accessToken = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&grant_type=client_credentials")
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .map(TokenResponse::getAccessToken)
            .block();

        var issuerUrl = authUrl + "realms/" + realm;
        var jwtDecoder = CustomJwtDecoder.fromIssuer(issuerUrl);
        var jwt = jwtDecoder.decode(accessToken);
        return new JwtAuthenticationToken(jwt);
    }

    private static class TokenResponse {
        private String access_token;

        public String getAccessToken() { return access_token; }

        public void setAccess_token(String access_token) { this.access_token = access_token; }
    }
}
