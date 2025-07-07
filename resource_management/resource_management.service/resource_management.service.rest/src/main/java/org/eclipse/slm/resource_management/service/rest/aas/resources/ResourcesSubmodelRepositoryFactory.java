package org.eclipse.slm.resource_management.service.rest.aas.resources;

import org.eclipse.slm.common.aas.clients.*;
import org.eclipse.slm.common.aas.repositories.SubmodelRepositoryFactory;
import org.eclipse.slm.resource_management.service.rest.aas.resources.deviceinfo.DeviceInfoSubmodelServiceFactory;
import org.springframework.stereotype.Component;

@Component
public class ResourcesSubmodelRepositoryFactory implements SubmodelRepositoryFactory {

    private final AasRegistryClientFactory aasRegistryClientFactory;

    private final AasRepositoryClientFactory aasRepositoryClientFactory;

    private final SubmodelRegistryClientFactory submodelRegistryClientFactory;

    private final SubmodelRepositoryClientFactory submodelRepositoryClientFactory;

    private final DeviceInfoSubmodelServiceFactory deviceInfoSubmodelServiceFactory;

    public ResourcesSubmodelRepositoryFactory(AasRegistryClientFactory aasRegistryClientFactory,
                                              AasRepositoryClientFactory aasRepositoryClientFactory,
                                              SubmodelRegistryClientFactory submodelRegistryClientFactory,
                                              SubmodelRepositoryClientFactory submodelRepositoryClientFactory,
                                              DeviceInfoSubmodelServiceFactory deviceInfoSubmodelServiceFactory) {
        this.aasRegistryClientFactory = aasRegistryClientFactory;
        this.aasRepositoryClientFactory = aasRepositoryClientFactory;
        this.submodelRegistryClientFactory = submodelRegistryClientFactory;
        this.submodelRepositoryClientFactory = submodelRepositoryClientFactory;
        this.deviceInfoSubmodelServiceFactory = deviceInfoSubmodelServiceFactory;
    }

    public ResourcesSubmodelRepository getSubmodelRepository(String resourceId) {
        return new ResourcesSubmodelRepository(
                resourceId,
                aasRegistryClientFactory, aasRepositoryClientFactory,
                submodelRegistryClientFactory, submodelRepositoryClientFactory,
                deviceInfoSubmodelServiceFactory);
    }

}
