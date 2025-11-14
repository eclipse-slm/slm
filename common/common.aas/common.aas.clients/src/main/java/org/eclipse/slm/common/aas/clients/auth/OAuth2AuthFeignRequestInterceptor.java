package org.eclipse.slm.common.aas.clients.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class OAuth2AuthFeignRequestInterceptor implements RequestInterceptor {
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private String accessToken;
    private long expiresAt;

    public OAuth2AuthFeignRequestInterceptor(String tokenUrl, String clientId, String clientSecret) {
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public void apply(RequestTemplate template) {
        try {
            if (accessToken == null || System.currentTimeMillis() > expiresAt) {
                fetchToken();
            }
            template.header("Authorization", "Bearer " + accessToken);
        } catch (Exception e) {
            throw new RuntimeException("OAuth2 Token konnte nicht abgerufen werden: " + e.getMessage(), e);
        }
    }

    public String fetchToken() throws Exception {
        var url = new URL(tokenUrl);
        var conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String basicAuth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        conn.setRequestProperty("Authorization", "Basic " + basicAuth);
        String body = "grant_type=client_credentials";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        int status = conn.getResponseCode();
        var is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        var mapper = new ObjectMapper();
        var json = mapper.readTree(is);
        if (json.has("access_token")) {
            accessToken = json.get("access_token").asText();
            int expiresIn = json.has("expires_in") ? json.get("expires_in").asInt() : 3600;
            expiresAt = System.currentTimeMillis() + (expiresIn - 30) * 1000; // 30 s security margin

            return accessToken;
        } else {
            throw new RuntimeException("Token response from URL '" + tokenUrl + "' contains not access token: " + json.toString());
        }
    }
}

