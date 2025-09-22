package org.eclipse.slm.resource_management.features.capabilities.jobs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CapabilityJobStateTransitionJpaRepository extends JpaRepository<CapabilityJobStateTransition, Long> {

        List<CapabilityJobStateTransition> findAllByCapabilityJobId(UUID firmwareUpdateJobId);

}
