package org.eclipse.slm.resource_management.persistence.api;

import org.eclipse.slm.resource_management.model.profiler.Profiler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProfilerJpaRepository extends JpaRepository<Profiler, UUID> {
}
