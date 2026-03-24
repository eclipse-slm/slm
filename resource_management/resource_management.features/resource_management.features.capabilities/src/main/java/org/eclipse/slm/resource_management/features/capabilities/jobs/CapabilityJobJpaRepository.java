package org.eclipse.slm.resource_management.features.capabilities.jobs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CapabilityJobJpaRepository extends JpaRepository<CapabilityJob, UUID> {

        List<CapabilityJob> findByResourceId(UUID resourceId);

        List<CapabilityJob> findByResourceIdAndCapabilityId(UUID resourceId, UUID capabilityId);

}
