package org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions;

import org.eclipse.slm.common.aas.repositories.submodels.SubmodelRepositoryFactory;
import org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.requirements.RequirementsSubmodelServiceFactory;
import org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.softwarenameplate.SoftwareNameplateSubmodelServiceFactory;
import org.springframework.stereotype.Component;

@Component
public class ServiceOfferingVersionsSubmodelRepositoryFactory implements SubmodelRepositoryFactory {

    private final SoftwareNameplateSubmodelServiceFactory softwareNameplateSubmodelServiceFactory;

    private final RequirementsSubmodelServiceFactory requirementsSubmodelServiceFactory;

    public ServiceOfferingVersionsSubmodelRepositoryFactory(SoftwareNameplateSubmodelServiceFactory softwareNameplateSubmodelServiceFactory,
                                                            RequirementsSubmodelServiceFactory requirementsSubmodelServiceFactory) {
        this.softwareNameplateSubmodelServiceFactory = softwareNameplateSubmodelServiceFactory;
        this.requirementsSubmodelServiceFactory = requirementsSubmodelServiceFactory;
    }

    public ServiceOfferingVersionsSubmodelRepository getSubmodelRepository(String serviceOfferingVersionId) {
        var cleanedServiceOfferingVersionId = serviceOfferingVersionId.replace(ServiceOfferingVersionAas.AAS_ID_PREFIX, "");
        return new ServiceOfferingVersionsSubmodelRepository(cleanedServiceOfferingVersionId,
                softwareNameplateSubmodelServiceFactory, requirementsSubmodelServiceFactory);
    }

}
