package org.eclipse.slm.resource_management.common.aas.submodels;

import org.eclipse.slm.aas.clients.shellregistry.AasRegistryClientFactory;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClientFactory;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClientFactory;
import org.eclipse.slm.aas.clients.submodelrepository.SubmodelRepositoryClientFactory;
import org.eclipse.slm.aas.repositories.submodels.SubmodelRepositoryFactory;
import org.eclipse.slm.resource_management.common.aas.submodels.deviceinfo.DeviceInfoSubmodelServiceFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResourcesSubmodelRepositoryFactory implements SubmodelRepositoryFactory {

    private final AasRegistryClientFactory aasRegistryClientFactory;

    private final AasRepositoryClientFactory aasRepositoryClientFactory;

    private final SubmodelRegistryClientFactory submodelRegistryClientFactory;

    private final SubmodelRepositoryClientFactory submodelRepositoryClientFactory;

    private final DeviceInfoSubmodelServiceFactory deviceInfoSubmodelServiceFactory;

    private final List<ResourceSubmodelContributor> contributors;

    public ResourcesSubmodelRepositoryFactory(AasRegistryClientFactory aasRegistryClientFactory,
                                              AasRepositoryClientFactory aasRepositoryClientFactory,
                                              SubmodelRegistryClientFactory submodelRegistryClientFactory,
                                              SubmodelRepositoryClientFactory submodelRepositoryClientFactory,
                                              DeviceInfoSubmodelServiceFactory deviceInfoSubmodelServiceFactory,
                                              List<ResourceSubmodelContributor> contributors) {
        this.aasRegistryClientFactory = aasRegistryClientFactory;
        this.aasRepositoryClientFactory = aasRepositoryClientFactory;
        this.submodelRegistryClientFactory = submodelRegistryClientFactory;
        this.submodelRepositoryClientFactory = submodelRepositoryClientFactory;
        this.deviceInfoSubmodelServiceFactory = deviceInfoSubmodelServiceFactory;
        this.contributors = contributors;
    }

    public ResourcesSubmodelRepository getSubmodelRepository(String resourceId) {
        return new ResourcesSubmodelRepository(
                resourceId,
                aasRegistryClientFactory, aasRepositoryClientFactory,
                submodelRegistryClientFactory, submodelRepositoryClientFactory,
                deviceInfoSubmodelServiceFactory,
                contributors);
    }

}
