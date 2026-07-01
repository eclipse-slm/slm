package org.eclipse.slm.service_management.features.service_offerings.api.serviceofferingversions;

import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.options.ServiceOptionValue;

public class ServiceOptionNotFoundException extends Exception {

    public ServiceOptionNotFoundException(ServiceOptionValue serviceOptionValue, ServiceOfferingVersion serviceOfferingVersion)
    {
        super("Service Option '" + serviceOptionValue.getServiceOptionId() + "' not found in Service Offering '"
                + serviceOfferingVersion.getServiceOffering().getId() + "' version '" + serviceOfferingVersion.getVersion() + "'");
    }

}

