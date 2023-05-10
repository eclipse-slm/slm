package org.eclipse.slm.service_management.persistence.api;

import org.eclipse.slm.service_management.model.services.ServiceInstanceGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceInstanceGroupJpaRepository extends JpaRepository<ServiceInstanceGroup, UUID>  {

}
