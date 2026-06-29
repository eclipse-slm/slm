package org.eclipse.slm.service_management.features.service_offerings.impl.servicevendors;

import org.eclipse.slm.service_management.features.service_offerings.api.servicevendors.ServiceVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceVendorJpaRepository extends JpaRepository<ServiceVendor, UUID>  {

}


