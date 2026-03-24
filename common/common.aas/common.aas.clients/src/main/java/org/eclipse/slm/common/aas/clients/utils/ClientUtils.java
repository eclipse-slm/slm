package org.eclipse.slm.common.aas.clients.utils;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonMapperFactory;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.SimpleAbstractTypeResolverFactory;
import org.eclipse.digitaltwin.basyx.client.internal.ApiClient;
import org.eclipse.slm.common.aas.clients.auth.AuthRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static final X509ExtendedTrustManager INSECURE_TRUST_MANAGER = new X509ExtendedTrustManager() {
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

    public static HttpClient.Builder getHttpClientBuilder() throws NoSuchAlgorithmException, KeyManagementException {
        var sslContext = SSLContext.getInstance("TLS");

        var sslParams = new SSLParameters(); sslParams.setEndpointIdentificationAlgorithm("");

        sslContext.init(null, List.of(ClientUtils.INSECURE_TRUST_MANAGER).toArray(TrustManager[]::new), new SecureRandom());
        var clientBuilder = HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(sslParams);

        return clientBuilder;
    }

    public static ApiClient getApiClient(String baseUrl, AuthRequestInterceptor authRequestInterceptor) {
        try {
            var apiClient = new ApiClient(ClientUtils.getHttpClientBuilder(), (new JsonMapperFactory()).create((new SimpleAbstractTypeResolverFactory()).create()), baseUrl);
            if (authRequestInterceptor != null) {
                apiClient.setRequestInterceptor(interceptor -> {
                    interceptor.header("Authorization", authRequestInterceptor.getAuthorizationHeaderValue());
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
