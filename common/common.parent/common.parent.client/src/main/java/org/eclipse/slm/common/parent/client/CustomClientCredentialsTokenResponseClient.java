package org.eclipse.slm.common.parent.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import java.util.*;

public class CustomClientCredentialsTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(CustomClientCredentialsTokenResponseClient.class);

    private final RestOperations restOperations;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomClientCredentialsTokenResponseClient(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2ClientCredentialsGrantRequest grantRequest) {
        String tokenUri = grantRequest.getClientRegistration().getProviderDetails().getTokenUri();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");

        if (!grantRequest.getClientRegistration().getScopes().isEmpty()) {
            form.add("scope", String.join(" ", grantRequest.getClientRegistration().getScopes()));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String clientId = grantRequest.getClientRegistration().getClientId();
        String clientSecret = grantRequest.getClientRegistration().getClientSecret();
        headers.setBasicAuth(clientId, clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        ResponseEntity<String> response = restOperations.exchange(tokenUri, HttpMethod.POST, request, String.class);

        String body = response.getBody();
        LOG.debug("Raw token response body: {}", body);

        try {
            Map<String, Object> map = objectMapper.readValue(body == null ? "{}" : body, new TypeReference<>() {});
            String accessToken = map.containsKey("access_token") ? Objects.toString(map.get("access_token"), null) : null;
            String tokenType = map.containsKey("token_type") ? Objects.toString(map.get("token_type"), null) : "Bearer";
            Long expiresIn = null;
            if (map.containsKey("expires_in")) {
                expiresIn = Long.valueOf(map.get("expires_in").toString());
            }

            if (accessToken == null) {
                throw new IllegalStateException("No access_token in token response: additionalParameters=" + map);
            }

            Set<String> scopes = new HashSet<>();
            if (map.containsKey("scope")) {
                String scopeStr = Objects.toString(map.get("scope"), "");
                if (!scopeStr.isBlank()) {
                    scopes.addAll(Arrays.asList(scopeStr.split(" ")));
                }
            } else {
                scopes = grantRequest.getClientRegistration().getScopes();
            }

            OAuth2AccessToken.TokenType tt = OAuth2AccessToken.TokenType.BEARER;

            OAuth2AccessTokenResponse.Builder builder = OAuth2AccessTokenResponse.withToken(accessToken)
                    .tokenType(tt)
                    .scopes(scopes);

            if (expiresIn != null) {
                builder.expiresIn(expiresIn);
            }

            builder.additionalParameters(map);
            return builder.build();

        } catch (Exception e) {
            throw new IllegalStateException("Error parsing token response", e);
        }
    }
}