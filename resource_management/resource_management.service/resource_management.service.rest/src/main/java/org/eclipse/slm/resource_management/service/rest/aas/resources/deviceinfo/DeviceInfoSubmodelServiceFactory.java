package org.eclipse.slm.resource_management.service.rest.aas.resources.deviceinfo;

import org.eclipse.digitaltwin.basyx.submodelservice.SubmodelService;
import org.eclipse.slm.common.aas.repositories.SubmodelServiceFactory;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesManager;
import org.springframework.stereotype.Component;

@Component
public class DeviceInfoSubmodelServiceFactory implements SubmodelServiceFactory {

    private final ResourcesManager resourcesManager;

    public DeviceInfoSubmodelServiceFactory(ResourcesManager resourcesManager) {
        this.resourcesManager = resourcesManager;
    }

    @Override
    public SubmodelService getSubmodelService(String aasId) {
        return new DeviceInfoSubmodelService(aasId, resourcesManager);
    }
}
