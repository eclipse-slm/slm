package org.eclipse.slm.common.aas.clients;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonMapperFactory;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.SimpleAbstractTypeResolverFactory;
import org.eclipse.digitaltwin.basyx.client.internal.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import javax.net.ssl.*;
import java.net.Socket;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

public class ClientUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ClientUtils.class);

    public static ApiClient getApiClient(String baseUrl, JwtAuthenticationToken jwtAuthenticationToken) {
        try {
            var trustAllCerts = new X509ExtendedTrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
                }
            };

            var sslContext = SSLContext.getInstance("TLS");

            sslContext.init(null, List.of(trustAllCerts).toArray(TrustManager[]::new), new SecureRandom());
            var clientBuilder = HttpClient.newBuilder()
                    .sslContext(sslContext);

            var apiClient = new ApiClient(clientBuilder, (new JsonMapperFactory()).create((new SimpleAbstractTypeResolverFactory()).create()), baseUrl);
            if (jwtAuthenticationToken != null) {
                apiClient.setRequestInterceptor(interceptor -> {
                    interceptor.header("Authorization", "Bearer " + jwtAuthenticationToken.getToken().getTokenValue());
                });
            }

            return apiClient;

        } catch (NoSuchAlgorithmException e) {
            LOG.error("Failed to create API client", e);
            return null;
        } catch (KeyManagementException e) {
            LOG.error("Failed to create API client", e);
            return null;
        }
    }

}
