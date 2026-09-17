package org.eclipse.slm.resource_management.features.capabilities.aas;

import org.eclipse.slm.aas.repositories.submodels.SubmodelRepository;
import org.eclipse.slm.common.keycloak.client.KeycloakServiceClient;
import org.eclipse.slm.resource_management.common.aas.submodels.ResourceSubmodelContributor;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilitiesConsulClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class DeploymentSubmodelRepositoryFactory implements ResourceSubmodelContributor {

    private final CapabilitiesConsulClient capabilitiesConsulClient;
    private final DeploymentJobManager jobManager;
    private final KeycloakServiceClient keycloakServiceClient;

    public DeploymentSubmodelRepositoryFactory(CapabilitiesConsulClient capabilitiesConsulClient,
                                               DeploymentJobManager jobManager,
                                               KeycloakServiceClient keycloakServiceClient) {
        this.capabilitiesConsulClient = capabilitiesConsulClient;
        this.jobManager = jobManager;
        this.keycloakServiceClient = keycloakServiceClient;
    }

    @Override
    public String getContributorKey() {
        return "Deployment";
    }

    @Override
    public SubmodelRepository getSubmodelRepository(String aasId) {
        return new DeploymentSubmodelRepository(
                aasId, this.capabilitiesConsulClient, this.jobManager, this::currentAccessToken);
    }

    /**
     * Das Token des aufrufenden Nutzers aus dem Request-Kontext. Faellt auf das
     * Service-Token zurueck, wenn der Aufruf nicht aus einem Request stammt.
     */
    private JwtAuthenticationToken currentAccessToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken;
        }
        try {
            return this.keycloakServiceClient.getJwtAuthentication();
        } catch (Exception e) {
            throw new IllegalStateException("No access token available for deployment operation", e);
        }
    }
}
