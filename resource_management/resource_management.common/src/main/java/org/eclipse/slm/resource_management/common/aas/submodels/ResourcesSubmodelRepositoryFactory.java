package org.eclipse.slm.resource_management.common.aas.submodels;

import org.eclipse.slm.aas.clients.shellregistry.AasRegistryClientFactory;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClientFactory;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClientFactory;
import org.eclipse.slm.aas.clients.submodelrepository.SubmodelRepositoryClientFactory;
import org.eclipse.slm.aas.repositories.submodels.SubmodelRepositoryFactory;
import org.eclipse.slm.resource_management.common.aas.submodels.deviceinfo.DeviceInfoSubmodelServiceFactory;
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
