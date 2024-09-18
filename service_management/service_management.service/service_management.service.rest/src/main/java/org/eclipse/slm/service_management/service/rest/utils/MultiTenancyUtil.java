package org.eclipse.slm.service_management.service.rest.utils;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

public class MultiTenancyUtil {

    public static String getKeycloakUserNameFromAuthenticationToken(JwtAuthenticationToken jwtAuthenticationToken)
    {
        var username = jwtAuthenticationToken.getToken().getClaims().get("preferred_username").toString();
        return username;
    }

    public static UUID getKeycloakUserIdFromAuthenticationToken(JwtAuthenticationToken jwtAuthenticationToken)
    {
        var subject = jwtAuthenticationToken.getToken().getSubject();
        return UUID.fromString(subject);
    }
}
