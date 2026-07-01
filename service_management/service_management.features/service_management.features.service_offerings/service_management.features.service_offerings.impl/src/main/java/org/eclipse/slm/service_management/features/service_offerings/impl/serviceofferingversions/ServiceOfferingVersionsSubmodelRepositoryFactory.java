package org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferingversions;

import org.eclipse.slm.aas.repositories.submodels.SubmodelRepositoryFactory;
import org.eclipse.slm.service_management.features.service_offerings.impl.requirements.RequirementsSubmodelServiceFactory;
import org.eclipse.slm.service_management.features.service_offerings.impl.aas.SoftwareNameplateSubmodelServiceFactory;
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

