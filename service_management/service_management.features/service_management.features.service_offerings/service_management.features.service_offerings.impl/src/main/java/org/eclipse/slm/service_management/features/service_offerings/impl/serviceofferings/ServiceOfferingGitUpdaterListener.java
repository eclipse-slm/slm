package org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferings;

import org.eclipse.slm.service_management.features.service_offerings.api.offerings.ServiceOffering;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.ServiceOfferingCreateOrUpdateRequest;
import org.eclipse.slm.service_management.features.service_offerings.api.offeringversions.ServiceOfferingVersionDTOApi;
import org.eclipse.slm.service_management.features.service_offerings.api.categories.ServiceCategoryNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.ServiceOfferingVersionCreateException;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.vendors.exceptions.ServiceVendorNotFoundException;

import java.util.List;
import java.util.UUID;

public interface ServiceOfferingGitUpdaterListener {

    ServiceOffering onNewServiceOfferingVersionsDetected(Object sender, ServiceOfferingCreateOrUpdateRequest serviceOfferingCreateOrUpdateRequest,
                                                         List<ServiceOfferingVersionDTOApi> newServiceOfferingVersions)
            throws ServiceVendorNotFoundException, ServiceCategoryNotFoundException, ServiceOfferingVersionCreateException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException;

    void onServiceOfferingVersionTagsDeleted(Object sender, UUID serviceOfferingId, List<String> serviceOfferingVersionNames)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, ServiceVendorNotFoundException, ServiceCategoryNotFoundException;
}

