package org.eclipse.slm.service_management.persistence.api;

import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceVendorJpaRepository extends JpaRepository<ServiceVendor, UUID>  {

}
