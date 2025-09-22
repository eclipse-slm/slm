package org.eclipse.slm.resource_management.common.location;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LocationJpaRepository extends JpaRepository<Location, UUID> {
}
