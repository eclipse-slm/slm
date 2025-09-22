package org.eclipse.slm.resource_management.common.aas.submodels.deviceinfo;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.common.aas.repositories.submodels.AbstractSubmodelService;
import org.eclipse.slm.common.keycloak.client.KeycloakServiceClient;
import org.eclipse.slm.resource_management.common.aas.ResourceAas;
import org.eclipse.slm.resource_management.common.resources.ResourcesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import javax.net.ssl.SSLException;
import java.util.UUID;

public class DeviceInfoSubmodelService extends AbstractSubmodelService {

    public final static Logger LOG = LoggerFactory.getLogger(DeviceInfoSubmodelService.class);

    private String aasId;

    private final ResourcesManager resourcesManager;

    private final KeycloakServiceClient keycloakServiceClient;

    public DeviceInfoSubmodelService(String aasId, ResourcesManager resourcesManager, KeycloakServiceClient keycloakServiceClient) {
        this.aasId = aasId;
        this.resourcesManager = resourcesManager;
        this.keycloakServiceClient = keycloakServiceClient;
    }

    @Override
    public Submodel getSubmodel() {

        var aasIdSplitted = aasId.split(ResourceAas.AAS_ID_PREFIX);
        if (aasIdSplitted.length != 2) {
            LOG.error("Invalid AAS ID format: '{}'", aasId);
            return null;
        }
        var resourceIdstring = aasIdSplitted[1];
        var resourceId = UUID.fromString(resourceIdstring);

        JwtAuthenticationToken resourceManagementJwtAuthentication = null;
        try {
            resourceManagementJwtAuthentication = keycloakServiceClient.getJwtAuthentication();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }

        var resource = resourcesManager.getResourceByIdOrThrow(resourceManagementJwtAuthentication, resourceId);

        return new DeviceInfoSubmodel(resource);
    }
}
