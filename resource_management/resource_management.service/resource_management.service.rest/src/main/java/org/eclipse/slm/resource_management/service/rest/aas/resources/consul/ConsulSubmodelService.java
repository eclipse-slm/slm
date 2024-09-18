package org.eclipse.slm.resource_management.service.rest.aas.resources.consul;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.common.aas.repositories.AbstractSubmodelService;

public class ConsulSubmodelService extends AbstractSubmodelService {

    private String resourceId;

    public ConsulSubmodelService(String resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public Submodel getSubmodel() {
        return new ConsulSubmodel(resourceId);
    }
}
