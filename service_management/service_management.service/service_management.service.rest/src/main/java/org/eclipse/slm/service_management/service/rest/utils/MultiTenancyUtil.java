package org.eclipse.slm.service_management.service.rest.utils;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;

import java.util.UUID;

public class MultiTenancyUtil {

    public static String getKeycloakUserNameFromKeycloakAuthenticationToken(KeycloakAuthenticationToken keycloakAuthenticationToken)
    {
        var keycloakPrincipal = (KeycloakPrincipal)keycloakAuthenticationToken.getPrincipal();
        return keycloakPrincipal.getName();
    }

    public static UUID getKeycloakUserIdFromKeycloakAuthenticationToken(KeycloakAuthenticationToken keycloakAuthenticationToken)
    {
        var keycloakPrincipal = (KeycloakPrincipal)keycloakAuthenticationToken.getPrincipal();
        return UUID.fromString(keycloakPrincipal.getName());
    }

    public static String getRealmFromKeycloakAuthenticationToken(KeycloakAuthenticationToken keycloakAuthenticationToken)
    {
        var keycloakDeployment = ((RefreshableKeycloakSecurityContext)((KeycloakPrincipal)keycloakAuthenticationToken.getPrincipal())
                .getKeycloakSecurityContext()).getDeployment();

        return keycloakDeployment.getRealm();
    }

    public static UUID generateOrganisationIdFromKeycloakAuthenticationToken(KeycloakAuthenticationToken keycloakAuthenticationToken)
    {
        var keycloakDeployment = ((RefreshableKeycloakSecurityContext)((KeycloakPrincipal)keycloakAuthenticationToken.getPrincipal())
                .getKeycloakSecurityContext()).getDeployment();
        var uuidGenerationString = keycloakDeployment.getAuthUrl().getHost() + keycloakDeployment.getRealm();
        return UUID.nameUUIDFromBytes(uuidGenerationString.getBytes());
    }

    public static UUID generateProjectId(String projectAbbreviation, KeycloakAuthenticationToken keycloakAuthenticationToken)
    {
        var uuidGenerationString = MultiTenancyUtil.generateOrganisationIdFromKeycloakAuthenticationToken(keycloakAuthenticationToken).toString() + projectAbbreviation;
        return UUID.nameUUIDFromBytes(uuidGenerationString.getBytes());
    }

    public static UUID generateProjectId(String projectAbbreviation, UUID organisationId)
    {
        var uuidGenerationString = organisationId.toString() + projectAbbreviation;
        return UUID.nameUUIDFromBytes(uuidGenerationString.getBytes());
    }
}
