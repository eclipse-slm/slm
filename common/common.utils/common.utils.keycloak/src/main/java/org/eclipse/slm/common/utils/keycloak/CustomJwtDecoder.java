package org.eclipse.slm.common.utils.keycloak;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

public class CustomJwtDecoder {

    public static JwtDecoder fromIssuer(String issuer) {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        var restTemplate = new RestTemplate(requestFactory);

        Assert.hasText(issuer, "issuer cannot be empty");
        var jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuer).restOperations(restTemplate).build();
        OAuth2TokenValidator<Jwt> jwtValidator = JwtValidators.createDefaultWithIssuer(issuer);
        jwtDecoder.setJwtValidator(jwtValidator);

        return jwtDecoder;
    }

}
