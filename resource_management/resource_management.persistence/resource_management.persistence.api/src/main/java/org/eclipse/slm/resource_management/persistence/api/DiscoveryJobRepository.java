package org.eclipse.slm.resource_management.persistence.api;

import org.eclipse.slm.resource_management.model.discovery.DiscoveryJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface DiscoveryJobRepository extends JpaRepository<DiscoveryJob, UUID> {
}
