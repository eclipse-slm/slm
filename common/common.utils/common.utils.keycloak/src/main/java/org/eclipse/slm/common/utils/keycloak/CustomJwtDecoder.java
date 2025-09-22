package org.eclipse.slm.common.utils.keycloak;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class CustomJwtDecoder {

    public static JwtDecoder fromIssuer(String issuer) {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.custom()
                    .setConnectionManager(
                            PoolingHttpClientConnectionManagerBuilder.create()
                                    .setSSLSocketFactory(
                                            SSLConnectionSocketFactoryBuilder.create()
                                                    .setSslContext(
                                                            SSLContexts.custom()
                                                                    .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                                                                    .build())
                                                    .setHostnameVerifier((s, sslSession) -> true)
                                                    .build())
                                    .build())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        var restTemplate = new RestTemplate(requestFactory);

        Assert.hasText(issuer, "issuer cannot be empty");
        var jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuer).restOperations(restTemplate).build();
        OAuth2TokenValidator<Jwt> jwtValidator = JwtValidators.createDefaultWithIssuer(issuer);
        jwtDecoder.setJwtValidator(jwtValidator);

        return jwtDecoder;
    }

}
