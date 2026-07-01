package org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferings;

import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceOfferingJpaRepository extends JpaRepository<ServiceOffering, UUID>  {

    List<ServiceOffering> findByServiceVendorId(UUID serviceVendorId);

}


