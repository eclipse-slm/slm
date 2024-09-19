package org.eclipse.slm.common.keycloak.config.jwt;

import com.jayway.jsonpath.JsonPath;
import org.eclipse.slm.common.keycloak.config.MultiTenantKeycloakRegistration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    private final MultiTenantKeycloakRegistration multiTenantKeycloakRegistration;

    public JwtAuthenticationConverter(MultiTenantKeycloakRegistration multiTenantKeycloakRegistration) {
        this.multiTenantKeycloakRegistration = multiTenantKeycloakRegistration;
    }

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        final var issuerProperties = multiTenantKeycloakRegistration.getIssuerProperties(jwt.getIssuer());
        final var authorities = new JwtGrantedAuthoritiesConverter(issuerProperties).convert(jwt);
        final String username = JsonPath.read(jwt.getClaims(), issuerProperties.getUsernameJsonPath());
        return new JwtAuthenticationToken(jwt, authorities, username);
    }
}
