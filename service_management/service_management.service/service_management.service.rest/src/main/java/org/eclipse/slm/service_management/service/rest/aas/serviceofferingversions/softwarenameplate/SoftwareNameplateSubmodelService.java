package org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.softwarenameplate;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.common.aas.repositories.submodels.AbstractSubmodelService;
import org.eclipse.slm.common.aas.repositories.exceptions.SubmodelNotFoundException;
import org.eclipse.slm.service_management.persistence.api.ServiceOfferingJpaRepository;
import org.eclipse.slm.service_management.persistence.api.ServiceOfferingVersionJpaRepository;

import java.util.UUID;

public class SoftwareNameplateSubmodelService extends AbstractSubmodelService {

    private UUID serviceOfferingVersionId;

    private final ServiceOfferingJpaRepository serviceOfferingRepository;

    private final ServiceOfferingVersionJpaRepository serviceOfferingVersionRepository;

    public SoftwareNameplateSubmodelService(UUID serviceOfferingVersionId, ServiceOfferingJpaRepository serviceOfferingRepository, ServiceOfferingVersionJpaRepository serviceOfferingVersionRepository) {
        this.serviceOfferingVersionId = serviceOfferingVersionId;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.serviceOfferingVersionRepository = serviceOfferingVersionRepository;
    }

    @Override
    public Submodel getSubmodel() {
        var serviceOfferingVersionOptional = this.serviceOfferingVersionRepository.findById(serviceOfferingVersionId);

        if (serviceOfferingVersionOptional.isPresent()) {
            return new SoftwareNameplateSubmodel(serviceOfferingVersionOptional.get());
        } else {
            throw new SubmodelNotFoundException(this.serviceOfferingVersionId.toString(), SoftwareNameplateSubmodel.SUBMODEL_ID_PREFIX);
        }

    }
}
