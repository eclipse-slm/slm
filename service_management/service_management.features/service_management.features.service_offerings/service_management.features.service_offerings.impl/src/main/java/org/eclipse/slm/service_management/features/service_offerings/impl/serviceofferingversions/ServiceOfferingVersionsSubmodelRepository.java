package org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferingversions;

import org.eclipse.slm.aas.repositories.submodels.AbstractSubmodelRepository;
import org.eclipse.slm.service_management.features.service_offerings.impl.requirements.RequirementsSubmodel;
import org.eclipse.slm.service_management.features.service_offerings.impl.requirements.RequirementsSubmodelServiceFactory;
import org.eclipse.slm.service_management.features.service_offerings.impl.aas.SoftwareNameplateSubmodel;
import org.eclipse.slm.service_management.features.service_offerings.impl.aas.SoftwareNameplateSubmodelServiceFactory;

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

