package org.eclipse.slm.service_management.persistence.api;

import org.eclipse.slm.service_management.model.offerings.ServiceOffering;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceOfferingJpaRepository extends JpaRepository<ServiceOffering, UUID>  {

    List<ServiceOffering> findByServiceVendorId(UUID serviceVendorId);

}
