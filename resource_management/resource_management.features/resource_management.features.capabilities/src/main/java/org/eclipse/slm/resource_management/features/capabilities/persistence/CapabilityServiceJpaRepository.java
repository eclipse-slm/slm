package org.eclipse.slm.resource_management.features.capabilities.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface CapabilityServiceJpaRepository extends JpaRepository<CapabilityServiceEntity, UUID> {

    List<CapabilityServiceEntity> findByResourceId(UUID resourceId);

    List<CapabilityServiceEntity> findByServiceClass(CapabilityServiceClass serviceClass);

    List<CapabilityServiceEntity> findByIdIn(Set<UUID> ids);

    Optional<CapabilityServiceEntity> findByResourceIdAndCapabilityId(UUID resourceId, UUID capabilityId);

    @Query("SELECT cs FROM CapabilityServiceEntity cs, " +
           "org.eclipse.slm.resource_management.features.capabilities.model.Capability c " +
           "WHERE cs.capabilityId = c.id AND c.capabilityClass = :capabilityClass")
    List<CapabilityServiceEntity> findByCapabilityClass(@Param("capabilityClass") String capabilityClass);
}
