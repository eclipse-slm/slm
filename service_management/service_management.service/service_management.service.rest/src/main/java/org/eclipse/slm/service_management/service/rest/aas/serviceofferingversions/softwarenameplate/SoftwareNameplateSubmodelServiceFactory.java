package org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.softwarenameplate;

import org.eclipse.digitaltwin.basyx.submodelservice.SubmodelService;
import org.eclipse.slm.common.aas.repositories.submodels.SubmodelServiceFactory;
import org.eclipse.slm.service_management.persistence.api.ServiceOfferingJpaRepository;
import org.eclipse.slm.service_management.persistence.api.ServiceOfferingVersionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SoftwareNameplateSubmodelServiceFactory implements SubmodelServiceFactory {

    private final ServiceOfferingJpaRepository serviceOfferingRepository;

    private final ServiceOfferingVersionJpaRepository serviceOfferingVersionRepository;

    public SoftwareNameplateSubmodelServiceFactory(ServiceOfferingJpaRepository serviceOfferingRepository,
                                                   ServiceOfferingVersionJpaRepository serviceOfferingVersionRepository) {
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.serviceOfferingVersionRepository = serviceOfferingVersionRepository;
    }

    @Override
    public SubmodelService getSubmodelService(String serviceOfferingVersionId) {
        return new SoftwareNameplateSubmodelService(UUID.fromString(serviceOfferingVersionId),
                serviceOfferingRepository, serviceOfferingVersionRepository);
    }
}
