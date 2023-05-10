package org.eclipse.slm.service_management.service.rest.service_offerings;

import org.eclipse.slm.service_management.model.offerings.ServiceOffering;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingCreateOrUpdateRequest;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersionDTOApi;
import org.eclipse.slm.service_management.service.rest.service_categories.ServiceCategoryNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingNotFoundException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionCreateException;
import org.eclipse.slm.service_management.model.offerings.exceptions.ServiceOfferingVersionNotFoundException;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;

import java.util.List;
import java.util.UUID;

public interface ServiceOfferingGitUpdaterListener {

    ServiceOffering onNewServiceOfferingVersionsDetected(Object sender, ServiceOfferingCreateOrUpdateRequest serviceOfferingCreateOrUpdateRequest,
                                                         List<ServiceOfferingVersionDTOApi> newServiceOfferingVersions)
            throws ServiceVendorNotFoundException, ServiceCategoryNotFoundException, ServiceOfferingVersionCreateException, ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException;

    void onServiceOfferingVersionTagsDeleted(Object sender, UUID serviceOfferingId, List<String> serviceOfferingVersionNames)
            throws ServiceOfferingNotFoundException, ServiceOfferingVersionNotFoundException, ServiceVendorNotFoundException, ServiceCategoryNotFoundException;
}
