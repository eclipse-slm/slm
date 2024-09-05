package org.eclipse.slm.resource_management.service.rest.aas.resources.consul;

import org.eclipse.digitaltwin.basyx.submodelservice.SubmodelService;
import org.eclipse.slm.common.aas.repositories.SubmodelServiceFactory;

public class ConsulSubmodelServiceFactory implements SubmodelServiceFactory {

    @Override
    public SubmodelService getSubmodelService(String resourceId) {
        return new ConsulSubmodelService(resourceId);
    }
}
