package org.eclipse.slm.service_management.features.service_offerings.api.offeringversions;

import org.eclipse.slm.service_management.features.service_offerings.api.offerings.options.ServiceOptionValue;

public class ServiceOptionNotFoundException extends Exception {

    public ServiceOptionNotFoundException(ServiceOptionValue serviceOptionValue, ServiceOfferingVersion serviceOfferingVersion)
    {
        super("Service Option '" + serviceOptionValue.getServiceOptionId() + "' not found in Service Offering '"
                + serviceOfferingVersion.getServiceOffering().getId() + "' version '" + serviceOfferingVersion.getVersion() + "'");
    }

}

