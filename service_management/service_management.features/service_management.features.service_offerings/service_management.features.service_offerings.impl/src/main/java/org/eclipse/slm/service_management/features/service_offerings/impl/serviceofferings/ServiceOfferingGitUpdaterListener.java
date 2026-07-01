package org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferings;

import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.ServiceOffering;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.ServiceOfferingCreateOrUpdateRequest;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions.ServiceOfferingVersionDTOApi;
import org.eclipse.slm.service_management.features.service_offerings.api.servicecategories.ServiceCategoryNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.exceptions.ServiceOfferingVersionCreateException;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.servicevendors.exceptions.ServiceVendorNotFoundException;

import java.util.List;
import java.util.UUID;

public interface ServiceOfferingGitUpdaterListener {

    ServiceOffering onNewServiceOfferingVersionsDetected(Object sender, ServiceOfferingCreateOrUpdateRequest serviceOfferingCreateOrUpdateRequest,
                                                         List<ServiceOfferingVersionDTOApi> newServiceOfferingVersions)
            throws ServiceVendorNotFoundException, ServiceCategoryNotFoundException, ServiceOfferingVersionCreateException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException;

    void onServiceOfferingVersionTagsDeleted(Object sender, UUID serviceOfferingId, List<String> serviceOfferingVersionNames)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, ServiceVendorNotFoundException, ServiceCategoryNotFoundException;
}

