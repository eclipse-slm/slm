package org.eclipse.slm.resource_management.features.profiler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProfilerJpaRepository extends JpaRepository<Profiler, UUID> {
}
