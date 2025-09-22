package org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions;

import org.eclipse.slm.common.aas.repositories.submodels.AbstractSubmodelRepository;
import org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.requirements.RequirementsSubmodel;
import org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.requirements.RequirementsSubmodelService;
import org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.requirements.RequirementsSubmodelServiceFactory;
import org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.softwarenameplate.SoftwareNameplateSubmodel;
import org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.softwarenameplate.SoftwareNameplateSubmodelServiceFactory;

public class ServiceOfferingVersionsSubmodelRepository extends AbstractSubmodelRepository {

    private final SoftwareNameplateSubmodelServiceFactory softwareNameplateSubmodelServiceFactory;

    private final RequirementsSubmodelServiceFactory requirementsSubmodelServiceFactory;

    public ServiceOfferingVersionsSubmodelRepository(String aasId,
                                                     SoftwareNameplateSubmodelServiceFactory softwareNameplateSubmodelServiceFactory,
                                                     RequirementsSubmodelServiceFactory requirementsSubmodelServiceFactory) {
        super(aasId);
        this.softwareNameplateSubmodelServiceFactory = softwareNameplateSubmodelServiceFactory;
        this.requirementsSubmodelServiceFactory = requirementsSubmodelServiceFactory;
        this.addSubmodelServiceFactory(SoftwareNameplateSubmodel.SUBMODEL_ID_PREFIX, this.softwareNameplateSubmodelServiceFactory);
        this.addSubmodelServiceFactory(RequirementsSubmodel.SUBMODEL_ID_PREFIX, this.requirementsSubmodelServiceFactory);
    }
}
