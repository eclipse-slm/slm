package org.eclipse.slm.resource_management.persistence.api;

import org.eclipse.slm.resource_management.model.resource.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LocationJpaRepository extends JpaRepository<Location, UUID> {
}
