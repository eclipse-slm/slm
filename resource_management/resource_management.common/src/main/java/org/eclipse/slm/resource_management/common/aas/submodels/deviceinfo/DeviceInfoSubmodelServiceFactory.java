package org.eclipse.slm.resource_management.common.aas.submodels.deviceinfo;

import org.eclipse.digitaltwin.basyx.submodelservice.SubmodelService;
import org.eclipse.slm.common.aas.repositories.submodels.SubmodelServiceFactory;
import org.eclipse.slm.common.keycloak.client.KeycloakServiceClient;
import org.eclipse.slm.resource_management.common.resources.ResourcesManager;
import org.springframework.stereotype.Component;

@Component
public class DeviceInfoSubmodelServiceFactory implements SubmodelServiceFactory {

    private final ResourcesManager resourcesManager;

    private final KeycloakServiceClient keycloakServiceClient;

    public DeviceInfoSubmodelServiceFactory(ResourcesManager resourcesManager, KeycloakServiceClient keycloakServiceClient) {
        this.resourcesManager = resourcesManager;
        this.keycloakServiceClient = keycloakServiceClient;
    }

    @Override
    public SubmodelService getSubmodelService(String aasId) {
        return new DeviceInfoSubmodelService(aasId, this.resourcesManager, this.keycloakServiceClient);
    }
}
