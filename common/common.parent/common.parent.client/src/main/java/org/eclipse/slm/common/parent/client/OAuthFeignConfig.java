package org.eclipse.slm.common.parent.client;

import feign.RequestInterceptor;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OAuthFeignConfig {

    private final MultiTenantKeycloakRegistration keycloakRegistration;

    public OAuthFeignConfig(MultiTenantKeycloakRegistration keycloakRegistration) {
        this.keycloakRegistration = keycloakRegistration;
    }

    @Bean
    public ClientRegistration clientRegistration() {
        var keycloakOidcConfig = keycloakRegistration.getFirstOidcConfig();
        if (keycloakOidcConfig == null) {
            throw new IllegalStateException("No OIDC configuration found in Keycloak registration");
        }

        var tokenUri = keycloakOidcConfig.getAuthServerUrlIncludingRealm();
        if (!tokenUri.endsWith("/")) {
            tokenUri = tokenUri + "/protocol/openid-connect/token";
        } else {
            tokenUri = tokenUri + "protocol/openid-connect/token";
        }

        var keycloakClientId = keycloakRegistration.getFirstOidcConfig().getResource();

        return ClientRegistration.withRegistrationId(keycloakClientId)
                .tokenUri(tokenUri)
                .clientId(keycloakOidcConfig.getResource())
                .clientSecret(keycloakOidcConfig.getCredentials().getSecret())
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("openid")
                .build();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(ClientRegistration clientRegistration) {
        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    @Bean
    @ConditionalOnMissingBean(OAuth2AuthorizedClientService.class)
    public OAuth2AuthorizedClientService oauth2AuthorizedClientService(ClientRegistrationRepository repo) {
        return new InMemoryOAuth2AuthorizedClientService(repo);
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService,
            OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsTokenResponseClient) {

        var clientCredentialsProvider = new ClientCredentialsOAuth2AuthorizedClientProvider();
        clientCredentialsProvider.setAccessTokenResponseClient(clientCredentialsTokenResponseClient);

        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        manager.setAuthorizedClientProvider(clientCredentialsProvider);
        return manager;
    }

    @Bean
    public RequestInterceptor requestInterceptor(OAuth2AuthorizedClientManager authorizedClientManager) {
        var keycloakClientId = keycloakRegistration.getFirstOidcConfig().getResource();

        return requestTemplate -> {
            OAuth2AuthorizeRequest authRequest = OAuth2AuthorizeRequest.withClientRegistrationId(keycloakClientId)
                    .principal("service-to-service")
                    .build();

            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authRequest);
            if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
                throw new IllegalStateException("No access token for client '" + keycloakClientId + "' available");
            }

            String token = authorizedClient.getAccessToken().getTokenValue();
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsTokenResponseClient() {
        return new CustomClientCredentialsTokenResponseClient(restTemplateTrustAll());
    }

    private RestOperations restTemplateTrustAll() {
        try {
            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory();

            var restTemplate = new RestTemplate(requestFactory);

            return restTemplate;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create permissive RestTemplate", e);
        }
    }
}
