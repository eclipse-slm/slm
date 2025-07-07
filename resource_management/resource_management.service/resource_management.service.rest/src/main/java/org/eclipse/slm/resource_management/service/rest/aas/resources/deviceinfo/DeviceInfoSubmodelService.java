package org.eclipse.slm.resource_management.service.rest.aas.resources.deviceinfo;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.common.aas.repositories.AbstractSubmodelService;
import org.eclipse.slm.resource_management.model.resource.ResourceAas;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DeviceInfoSubmodelService extends AbstractSubmodelService {

    public final static Logger LOG = LoggerFactory.getLogger(DeviceInfoSubmodelService.class);

    private String aasId;

    private final ResourcesManager resourcesManager;

    public DeviceInfoSubmodelService(String aasId, ResourcesManager resourcesManager) {
        this.aasId = aasId;
        this.resourcesManager = resourcesManager;
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

        var resource = resourcesManager.getResourceWithoutCredentials(resourceId);

        if (resource.isPresent()) {
            return new DeviceInfoSubmodel(resource.get());
        }
        else {
            LOG.error("Resource with id '{}' not found", resourceId);
            return null;
        }
    }
}
