package org.eclipse.slm.service_management.features.service_offerings.impl.requirements;

import org.eclipse.digitaltwin.basyx.submodelservice.SubmodelService;
import org.eclipse.slm.aas.repositories.submodels.SubmodelServiceFactory;
import org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferings.ServiceOfferingJpaRepository;
import org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferingversions.ServiceOfferingVersionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RequirementsSubmodelServiceFactory implements SubmodelServiceFactory {

    private final ServiceOfferingJpaRepository serviceOfferingRepository;

    private final ServiceOfferingVersionJpaRepository serviceOfferingVersionRepository;

    public RequirementsSubmodelServiceFactory(ServiceOfferingJpaRepository serviceOfferingRepository,
                                                   ServiceOfferingVersionJpaRepository serviceOfferingVersionRepository) {
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.serviceOfferingVersionRepository = serviceOfferingVersionRepository;
    }

    @Override
    public SubmodelService getSubmodelService(String serviceOfferingVersionId) {
        return new RequirementsSubmodelService(UUID.fromString(serviceOfferingVersionId),
                serviceOfferingRepository, serviceOfferingVersionRepository);
    }
}

