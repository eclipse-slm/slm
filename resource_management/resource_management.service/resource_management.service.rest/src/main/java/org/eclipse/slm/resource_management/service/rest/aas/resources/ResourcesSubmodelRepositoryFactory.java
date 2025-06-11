package org.eclipse.slm.resource_management.service.rest.aas.resources;

import org.eclipse.slm.common.aas.clients.AasRegistryClient;
import org.eclipse.slm.common.aas.clients.AasRepositoryClient;
import org.eclipse.slm.common.aas.clients.SubmodelRegistryClient;
import org.eclipse.slm.common.aas.clients.SubmodelRepositoryClient;
import org.eclipse.slm.common.aas.repositories.SubmodelRepositoryFactory;
import org.eclipse.slm.resource_management.service.rest.aas.resources.deviceinfo.DeviceInfoSubmodelServiceFactory;
import org.springframework.stereotype.Component;

@Component
public class ResourcesSubmodelRepositoryFactory implements SubmodelRepositoryFactory {

    private final AasRegistryClient aasRegistryClient;

    private final AasRepositoryClient aasRepositoryClient;

    private final SubmodelRegistryClient submodelRegistryClient;

    private final SubmodelRepositoryClient submodelRepositoryClient;

    private final DeviceInfoSubmodelServiceFactory deviceInfoSubmodelServiceFactory;

    public ResourcesSubmodelRepositoryFactory(AasRegistryClient aasRegistryClient, AasRepositoryClient aasRepositoryClient, SubmodelRegistryClient submodelRegistryClient, SubmodelRepositoryClient submodelRepositoryClient, DeviceInfoSubmodelServiceFactory deviceInfoSubmodelServiceFactory) {
        this.aasRegistryClient = aasRegistryClient;
        this.aasRepositoryClient = aasRepositoryClient;
        this.submodelRegistryClient = submodelRegistryClient;
        this.submodelRepositoryClient = submodelRepositoryClient;
        this.deviceInfoSubmodelServiceFactory = deviceInfoSubmodelServiceFactory;
    }

    public ResourcesSubmodelRepository getSubmodelRepository(String resourceId) {
        return new ResourcesSubmodelRepository(
                resourceId,
                aasRegistryClient, aasRepositoryClient,
                submodelRegistryClient, submodelRepositoryClient,
                deviceInfoSubmodelServiceFactory);
    }

}
