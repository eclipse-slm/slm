package org.eclipse.slm.service_management.service.rest.service_vendors;

import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;
import org.eclipse.slm.service_management.persistence.api.ServiceVendorJpaRepository;
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
