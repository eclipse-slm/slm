package org.eclipse.slm.common.parent.service_rest.config;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.eclipse.slm.common.keycloak.config.jwt.JwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.eclipse.slm.common.keycloak.config.jwt.IssuerProperties;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class WebSecurityConfig {

    private final MultiTenantKeycloakRegistration multiTenantKeycloakRegistration;

    private static final String[] STATIC_AUTH_WHITELIST = {
            // -- Swagger UI v3 (OpenAPI)
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/actuator/health"
    };

    public WebSecurityConfig(MultiTenantKeycloakRegistration multiTenantKeycloakRegistration) {
        this.multiTenantKeycloakRegistration = multiTenantKeycloakRegistration;
    }

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            ServerProperties serverProperties,
            @Value("${security.origins:[]}") String[] origins,
            @Value("${security.auth-white-list:[]}") String[] configuredAuthWhiteList,
            AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver)
            throws Exception {

        http.oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(authenticationManagerResolver));

        // Enable and configure CORS
        http.cors(cors -> cors.configurationSource(corsConfigurationSource(origins)));

        // State-less session (state in access-token only)
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Disable CSRF because of state-less session-management
        http.csrf(csrf -> csrf.disable());

        // Disable 'X-Frame-Options' response header
        http.headers((headers) -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        // Return 401 (unauthorized) instead of 302 (redirect to login) when
        // authorization is missing or invalid
        http.exceptionHandling(eh -> eh.authenticationEntryPoint((request, response, authException) -> {
            response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"Restricted Content\"");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }));

        // If SSL enabled, disable http (https only)
        if (serverProperties.getSsl() != null && serverProperties.getSsl().isEnabled()) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        // @formatter:off
        var authWhiteList = (String[])ArrayUtils.addAll(configuredAuthWhiteList, STATIC_AUTH_WHITELIST);

        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(Stream.of(authWhiteList).map(AntPathRequestMatcher::new).toArray(AntPathRequestMatcher[]::new)).permitAll()
                .anyRequest().authenticated());
        // @formatter:on

        return http.build();
    }

    private UrlBasedCorsConfigurationSource corsConfigurationSource(String[] origins) {
        final var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(origins));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));

        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    AuthenticationManagerResolver<HttpServletRequest>
    authenticationManagerResolver(JwtAuthenticationConverter authenticationConverter) {
        final Map<String, AuthenticationManager> authenticationProviders =
                multiTenantKeycloakRegistration.getIssuers().stream().map(IssuerProperties::getUri).map(URL::toString)
                        .collect(Collectors.toMap(issuer -> issuer, issuer -> authenticationProvider(issuer, authenticationConverter)::authenticate));
        return new JwtIssuerAuthenticationManagerResolver(authenticationProviders::get);
    }

    JwtAuthenticationProvider authenticationProvider(String issuer, JwtAuthenticationConverter authenticationConverter) {
        var decoder = CustomJwtDecoder.fromIssuer(issuer);
        var provider = new JwtAuthenticationProvider(decoder);
        provider.setJwtAuthenticationConverter(authenticationConverter);
        return provider;
    }
}
