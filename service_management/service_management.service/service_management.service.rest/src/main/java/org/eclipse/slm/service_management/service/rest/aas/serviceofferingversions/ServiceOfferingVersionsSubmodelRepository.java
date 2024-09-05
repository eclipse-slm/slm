package org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions;

import org.eclipse.slm.common.aas.repositories.AbstractSubmodelRepository;
import org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.softwarenameplate.SoftwareNameplateSubmodel;
import org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.softwarenameplate.SoftwareNameplateSubmodelServiceFactory;

public class ServiceOfferingVersionsSubmodelRepository extends AbstractSubmodelRepository {

    private final SoftwareNameplateSubmodelServiceFactory softwareNameplateSubmodelServiceFactory;

    public ServiceOfferingVersionsSubmodelRepository(String aasId, SoftwareNameplateSubmodelServiceFactory softwareNameplateSubmodelServiceFactory) {
        super(aasId);
        this.softwareNameplateSubmodelServiceFactory = softwareNameplateSubmodelServiceFactory;
        this.addSubmodelServiceFactory(SoftwareNameplateSubmodel.SUBMODEL_ID_PREFIX, this.softwareNameplateSubmodelServiceFactory);
    }
}
