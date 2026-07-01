package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances;

import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceInstanceGroupJpaRepository extends JpaRepository<ServiceInstanceGroup, UUID>  {

}

