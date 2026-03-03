package org.eclipse.slm.common.parent.service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/***
 * Filter class for API key authentication. It checks for the presence of an "X-API-KEY" header in the incoming request and compares its value to a
 * API key value configured in application properties. If the API key is valid, it sets an authentication in the security context with a simple authority
 * "API_KEY". If API key authentication is disabled (via application property "securty.api-key-auth.enabled", the filter will not add any authentication and
 * will allow all requests to pass through for later processing (e.g. OAuth2). The API key value can be configured via the "security.api-key-auth.value"
 * property, and if not set, a random, unknown UUID will be generated and used as the API key value, effectively disabling API key authentication and
 * minimizing misconfiguration.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final static Logger LOG = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    private final boolean apiKeyAuthEnabled;

    private final String apiKeyValue;

    /***
     * Constructor for ApiKeyAuthFilter.
     * @param apiKeyAuthEnabled Whether API key authentication is enabled. If false, the filter will not add any authentication and will allow all requests
     *                          to pass through for later processing (e.g. OAuth2).
     * @param apiKeyValue       The value of the API key to be used for authentication. If empty, a random, unknown UUID will be generated and used as the
     *                          API key value, effectively disabling API key authentication and minimizing misconfiguration.
     */
    public ApiKeyAuthFilter(@Value("${security.api-key-auth.enabled:false}") boolean apiKeyAuthEnabled,
                            @Value("${security.api-key-auth.value:}") String apiKeyValue) {
        this.apiKeyAuthEnabled = apiKeyAuthEnabled;
        if (apiKeyValue.isEmpty()) {
            apiKeyValue = UUID.randomUUID().toString();
        }
        this.apiKeyValue = apiKeyValue;
    }

    /**
     * Filters incoming requests and checks for the presence of an "X-API-KEY" header. If API key authentication is enabled and the header value matches the
     * configured API key value, an authentication with authority "API_KEY" is set in the security context. The filter then continues the filter chain for
     * further processing (e.g. OAuth2 authentication). If API key authentication is disabled, the filter simply continues the filter chain without adding
     * any authentication.
     * @param request           The incoming HTTP request to be filtered.
     * @param response          The HTTP response to be sent back to the client.
     * @param filterChain       The filter chain to be continued after processing the API key authentication.
     * @throws ServletException if an error occurs during filtering.
     * @throws IOException      if an I/O error occurs during filtering.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-KEY");
        if (apiKeyAuthEnabled) {
            if (apiKeyValue.equals(apiKey)) {
                var auth = new UsernamePasswordAuthenticationToken(
                        "apiKeyUser",
                        null,
                        List.of(new SimpleGrantedAuthority("API_KEY")));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Returns whether API key authentication is enabled.
     * @return true if API key authentication is enabled, false otherwise.
     */
    public boolean isApiKeyAuthEnabled() {
        return apiKeyAuthEnabled;
    }
}