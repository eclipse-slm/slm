package org.eclipse.slm.service_management.features.service_offerings.impl.vendors;

import org.eclipse.slm.service_management.features.service_offerings.api.vendors.ServiceVendor;
import org.eclipse.slm.service_management.features.service_offerings.api.vendors.exceptions.ServiceVendorNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ServiceVendorHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceVendorHandler.class);

    private final ServiceVendorJpaRepository serviceVendorJpaRepository;

    public ServiceVendorHandler(ServiceVendorJpaRepository serviceVendorJpaRepository) {
        this.serviceVendorJpaRepository = serviceVendorJpaRepository;
    }

    public ServiceVendor getServiceVendorById(UUID serviceVendorId) throws ServiceVendorNotFoundException {
        var serviceVendorOptional = this.serviceVendorJpaRepository.findById(serviceVendorId);
        if (serviceVendorOptional.isPresent()) {
            return serviceVendorOptional.get();
        }
        else {
            throw new ServiceVendorNotFoundException(serviceVendorId);
        }
    }
}

