package org.eclipse.slm.resource_management.common.resource_types.aas.submodels;

import org.eclipse.slm.common.aas.repositories.submodels.SubmodelRepository;
import org.eclipse.slm.common.aas.repositories.submodels.SubmodelRepositoryFactory;
import org.eclipse.slm.resource_management.common.resource_types.ResourceTypesManager;
import org.springframework.stereotype.Component;

@Component
public class ResourceTypeSubmodelRepositoryFactory implements SubmodelRepositoryFactory {

    private final ResourceTypesManager resourceTypesManager;

    public ResourceTypeSubmodelRepositoryFactory(ResourceTypesManager resourceTypesManager) {
        this.resourceTypesManager = resourceTypesManager;
    }

    @Override
    public SubmodelRepository getSubmodelRepository(String aasId) {
        return new ResourceTypesSubmodelRepository(aasId, this.resourceTypesManager);
    }
}