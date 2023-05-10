package org.eclipse.slm.common.vault.client;

import org.eclipse.slm.common.vault.model.Response;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class VaultConfiguration extends AbstractVaultConfiguration {

    public final static Logger LOG = LoggerFactory.getLogger(VaultConfiguration.class);

    @Value("${vault.host}")
    String host;
    @Value("${vault.scheme}")
    String scheme;
    @Value("${vault.port}")
    String port;
    @Value("${vault.app-role.role-id:}")
    String roleId;
    @Value("${vault.app-role.secret-id:}")
    String secretId;
    @Value("${vault.app-role.app-role-path:}")
    String path;
    @Value("${vault.token:}")
    String token;

    /**
     * Specify an endpoint for connecting to Vault.
     */
    @Override
    public VaultEndpoint vaultEndpoint() {
        var vaultEndpoint = new VaultEndpoint();

        vaultEndpoint.setHost(host);
        vaultEndpoint.setPort(Integer.valueOf(port));
        vaultEndpoint.setScheme(scheme);

        return vaultEndpoint;
    }

    /**
     * Configure a client authentication.
     * Please consider a more secure authentication method
     * for production use.
     */
    @Override
    public ClientAuthentication clientAuthentication() {

        if(!this.token.isEmpty()) {
            return new TokenAuthentication(this.token);
        }

        String url = scheme + "://" + host + ":" + port + "/v1/auth/" + path + "/login";

        try {
            SslContext sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

        TcpClient tcpClient = TcpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        HttpClient httpClient = HttpClient.from(tcpClient);

        WebClient webClient = WebClient.builder()
                .baseUrl(url)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        Map<String, String> body = new HashMap<>();
        body.put("role_id", roleId);
        body.put("secret_id", secretId);

        Response response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(Response.class)
                .block();

            return new TokenAuthentication(response.auth.client_token);

        } catch (SSLException e) {
            e.printStackTrace();
        } catch(WebClientResponseException e) {
            LOG.error(e.getResponseBodyAsString());
        }

        return null;
    }
}
