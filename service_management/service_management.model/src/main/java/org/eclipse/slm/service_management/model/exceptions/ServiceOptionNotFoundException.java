package org.eclipse.slm.service_management.model.exceptions;

import org.eclipse.slm.service_management.model.offerings.ServiceOffering;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionValue;

public class ServiceOptionNotFoundException extends Exception {

    public ServiceOptionNotFoundException(ServiceOptionValue serviceOptionValue, ServiceOfferingVersion serviceOfferingVersion)
    {
        super("Service Option '" + serviceOptionValue.getServiceOptionId() + "' not found in Service Offering '"
                + serviceOfferingVersion.getServiceOffering().getId() + "' version '" + serviceOfferingVersion.getVersion() + "'");
    }

}
