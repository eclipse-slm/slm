package org.eclipse.slm.resource_management.persistence.api;

import org.eclipse.slm.resource_management.model.capabilities.Capability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CapabilityJpaRepository extends JpaRepository<Capability, UUID> {
}
