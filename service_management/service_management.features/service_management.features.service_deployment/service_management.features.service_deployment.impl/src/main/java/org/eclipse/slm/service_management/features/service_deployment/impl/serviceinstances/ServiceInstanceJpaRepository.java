package org.eclipse.slm.service_management.features.service_deployment.impl.serviceinstances;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ServiceInstanceJpaRepository extends JpaRepository<ServiceInstanceEntity, UUID> {

    List<ServiceInstanceEntity> findByIdIn(Set<UUID> ids);
}
