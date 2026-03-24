package org.eclipse.slm.resource_management.common.resource_types.aas.shells;

import org.eclipse.slm.common.aas.repositories.shells.AasFactory;
import org.eclipse.slm.resource_management.common.resource_types.ResourceTypesManager;
import org.springframework.stereotype.Component;

@Component
public class ResourceTypesAasFactory implements AasFactory {

    private final ResourceTypesManager resourceTypesManager;

    public ResourceTypesAasFactory(ResourceTypesManager resourceTypesManager) {
        this.resourceTypesManager = resourceTypesManager;
    }

    @Override
    public ResourceTypesAas createAas(String aasId) {
        return new ResourceTypesAas(resourceTypesManager);
    }
}
