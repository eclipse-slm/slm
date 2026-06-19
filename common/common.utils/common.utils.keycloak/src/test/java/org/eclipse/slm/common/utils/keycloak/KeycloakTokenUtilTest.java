package org.eclipse.slm.common.utils.keycloak;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class KeycloakTokenUtilTest {

    private JwtAuthenticationToken tokenWith(Map<String, Object> claims, String... roles) {
        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                claims.isEmpty() ? Map.of("sub", "user") : claims);
        var authorities = java.util.Arrays.stream(roles)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .map(a -> (org.springframework.security.core.GrantedAuthority) a)
                .toList();
        return new JwtAuthenticationToken(jwt, authorities);
    }

    @Test
    public void getGroupsReturnsGroupsClaim() {
        var token = tokenWith(Map.of("sub", "user", "groups", List.of("/Org/CustomerA", "/Org/CustomerB")));

        Set<String> groups = KeycloakTokenUtil.getGroups(token);

        assertEquals(Set.of("/Org/CustomerA", "/Org/CustomerB"), groups);
    }

    @Test
    public void getGroupsReturnsEmptySetWhenClaimMissing() {
        var token = tokenWith(Map.of("sub", "user"));

        assertTrue(KeycloakTokenUtil.getGroups(token).isEmpty());
    }

    @Test
    public void isAdminTrueWhenSlmAdminRolePresent() {
        var token = tokenWith(Map.of("sub", "user"), "slm-admin");

        assertTrue(KeycloakTokenUtil.isAdmin(token));
    }

    @Test
    public void isAdminFalseWhenOnlyUserRole() {
        var token = tokenWith(Map.of("sub", "user"), "slm-user");

        assertFalse(KeycloakTokenUtil.isAdmin(token));
    }
}
