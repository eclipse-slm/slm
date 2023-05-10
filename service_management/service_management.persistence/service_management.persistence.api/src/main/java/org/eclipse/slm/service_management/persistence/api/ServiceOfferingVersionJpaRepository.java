package org.eclipse.slm.service_management.persistence.api;

import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceOfferingVersionJpaRepository extends JpaRepository<ServiceOfferingVersion, UUID>  {

    List<ServiceOfferingVersion> findByServiceOfferingId(UUID serviceOfferingId);

    List<ServiceOfferingVersion> findByVersion(String version);

}
