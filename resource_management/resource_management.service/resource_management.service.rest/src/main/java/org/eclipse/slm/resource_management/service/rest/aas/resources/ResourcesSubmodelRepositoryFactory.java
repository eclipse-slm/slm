package org.eclipse.slm.resource_management.service.rest.aas.resources;

import org.eclipse.slm.common.aas.repositories.SubmodelRepositoryFactory;
import org.springframework.stereotype.Component;

@Component
public class ResourcesSubmodelRepositoryFactory implements SubmodelRepositoryFactory {

    public ResourcesSubmodelRepository getSubmodelRepository(String resourceId) {
        return new ResourcesSubmodelRepository(resourceId);
    }

}
